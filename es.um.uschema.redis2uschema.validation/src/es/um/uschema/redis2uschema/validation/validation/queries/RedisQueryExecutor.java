package es.um.uschema.redis2uschema.validation.validation.queries;

import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.ASTERISK;
import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.COLON;
import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.DOT;
import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.LEFT_SQUARE_BRACKET;
import static es.um.uschema.redis2uschema.validation.queries.constants.Constants.RIGHT_SQUARE_BRACKET;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import es.um.uschema.USchema.Aggregate;
import es.um.uschema.USchema.Attribute;
import es.um.uschema.USchema.DataType;
import es.um.uschema.USchema.PList;
import es.um.uschema.USchema.PSet;
import es.um.uschema.USchema.PrimitiveType;
import es.um.uschema.USchema.StructuralFeature;
import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.redis2uschema.validation.queries.utils.TypeUtils;
import redis.clients.jedis.Jedis;

public class RedisQueryExecutor {

	private Jedis jedis;

	public RedisQueryExecutor(Jedis jedis) {
		this.jedis = jedis;
	}

	public int getAll(StructuralVariation structuralVariation) {
		int results = 0;

		String entityName = structuralVariation.getContainer().getName();

		List<Attribute> attributes = structuralVariation.getStructuralFeatures().stream()
				.filter(Attribute.class::isInstance).map(Attribute.class::cast).collect(Collectors.toList());

		Optional<Attribute> attributeOptional = attributes.stream().filter(a -> a.getType() instanceof PrimitiveType)
				.findFirst();
		if (attributeOptional.isPresent()) {
			Set<String> idKeys = jedis.keys(entityName + COLON + ASTERISK + COLON + attributeOptional.get().getName());
			idKeys = idKeys.stream()
					.map(k -> entityName + COLON + k.substring(k.indexOf(COLON) + 1, k.lastIndexOf(COLON)) + COLON)
					.collect(Collectors.toSet());

			for (String idKey : idKeys) {
				Set<String> objectKeys = jedis.keys(idKey + ASTERISK);
				addAggregateFields(structuralVariation, idKey, objectKeys);

				int keysFound = objectKeys.size();
				int structuralFeaturesCount = countStructuralVariationKeys(structuralVariation);

				results = processStructuralVariationFound(structuralVariation, results, objectKeys, keysFound,
						structuralFeaturesCount);
			}

		}

		return results;
	}

	private int processStructuralVariationFound(StructuralVariation structuralVariation, int results,
			Set<String> objectKeys, int keysFound, int structuralFeaturesCount) {
		if (keysFound == structuralFeaturesCount) {
			boolean found = checkKeyAsVariationFeatures(structuralVariation, objectKeys);

			if (found) {
				boolean typesMatched = checkTypes(structuralVariation, objectKeys);
				if (typesMatched) {
					results++;
				}
			}
		}
		return results;
	}

	private void addAggregateFields(StructuralVariation structuralVariation, String idKey, Set<String> objectKeys) {
		List<Aggregate> aggregates = structuralVariation.getStructuralFeatures().stream()
				.filter(Aggregate.class::isInstance).map(Aggregate.class::cast).collect(Collectors.toList());
		for (Aggregate aggregate : aggregates) {
			String hIdKey = idKey + aggregate.getName();
			Set<String> hashKeys = jedis.hkeys(hIdKey);
			if (hashKeys.size() > 0) {
				objectKeys.remove(hIdKey);
				for (String hashKey : hashKeys) {
					objectKeys.add(hIdKey + LEFT_SQUARE_BRACKET + hashKey + RIGHT_SQUARE_BRACKET);
				}
			}
		}
	}

	private boolean checkTypes(StructuralVariation structuralVariation, Set<String> objectKeys) {
		for (StructuralFeature structuralFeature : structuralVariation.getStructuralFeatures()) {
			boolean keyTypeCorrect = checkKeyType(objectKeys, structuralFeature);
			if (!keyTypeCorrect) {
				return true;
			}
		}

		return true;
	}

	private boolean checkKeyType(Set<String> objectKeys, StructuralFeature structuralFeature) {
		for (String objectKey : objectKeys) {
			if (structuralFeature instanceof Attribute) {
				if (processAttributeType(structuralFeature, objectKey)) {
					return true;
				}
			} else if (structuralFeature instanceof Aggregate) {
				if (processAggregateTypes(structuralFeature, objectKey)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean processAggregateTypes(StructuralFeature structuralFeature, String objectKey) {
		Aggregate aggregate = (Aggregate) structuralFeature;

		if (objectKey.contains(LEFT_SQUARE_BRACKET) && objectKey.contains(RIGHT_SQUARE_BRACKET)) {
			String propertyName = objectKey.substring(objectKey.lastIndexOf(COLON) + 1,
					objectKey.lastIndexOf(LEFT_SQUARE_BRACKET));
			String subpropertyName = objectKey.substring(objectKey.lastIndexOf(LEFT_SQUARE_BRACKET) + 1,
					objectKey.lastIndexOf(RIGHT_SQUARE_BRACKET));

			if (propertyName.equals(aggregate.getName())) {
				for (StructuralVariation aggregatedStructuralVariation : aggregate.getAggregates()) {
					for (Attribute aggregatedAttribute : aggregatedStructuralVariation.getStructuralFeatures().stream()
							.filter(Attribute.class::isInstance).map(Attribute.class::cast)
							.collect(Collectors.toList())) {
						if (subpropertyName.equals(aggregatedAttribute.getName())) {
							boolean ifTypeMatching = checkStructuralFeatureType(objectKey, aggregatedAttribute,
									subpropertyName);
							if (ifTypeMatching) {
								return true;
							}
						}
					}
				}
			}
		} else if (objectKey.contains(DOT)) {
			String propertyName = objectKey.substring(objectKey.lastIndexOf(COLON) + 1, objectKey.lastIndexOf(DOT));
			String subpropertyName = objectKey.substring(objectKey.lastIndexOf(DOT) + 1);

			if (propertyName.equals(aggregate.getName())) {
				for (StructuralVariation aggregatedStructuralVariation : aggregate.getAggregates()) {
					for (Attribute aggregatedAttribute : aggregatedStructuralVariation.getStructuralFeatures().stream()
							.filter(Attribute.class::isInstance).map(Attribute.class::cast)
							.collect(Collectors.toList())) {
						if (subpropertyName.equals(aggregatedAttribute.getName())) {
							boolean ifTypeMatching = checkStructuralFeatureType(objectKey, aggregatedAttribute,
									subpropertyName);
							if (ifTypeMatching) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	private boolean processAttributeType(StructuralFeature structuralFeature, String objectKey) {
		Attribute attribute = (Attribute) structuralFeature;
		String propertyName = objectKey.substring(objectKey.lastIndexOf(COLON) + 1);

		boolean ifTypeMatching = checkStructuralFeatureType(objectKey, attribute, propertyName);
		if (ifTypeMatching) {
			return true;
		}

		return false;
	}

	private boolean checkStructuralFeatureType(String objectKey, Attribute attribute, String propertyName) {
		if (propertyName.equals(attribute.getName())) {
			DataType type = attribute.getType();
			if (type instanceof PrimitiveType) {
				PrimitiveType primitiveType = (PrimitiveType) type;
				if (objectKey.contains(LEFT_SQUARE_BRACKET) && objectKey.contains(RIGHT_SQUARE_BRACKET)) {
					String newObjectKey = objectKey.substring(0, objectKey.indexOf(LEFT_SQUARE_BRACKET));
					String propertyKey = objectKey.substring(objectKey.indexOf(LEFT_SQUARE_BRACKET) + 1,
							objectKey.lastIndexOf(RIGHT_SQUARE_BRACKET));

					String value = jedis.hget(newObjectKey, propertyKey);
					String valueType = TypeUtils.obtainSingleValueType(value);
					if (valueType.equals(primitiveType.getName())) {
						return true;
					}
				} else {
					String value = jedis.get(objectKey);
					String valueType = TypeUtils.obtainSingleValueType(value);
					if (valueType.equals(primitiveType.getName())) {
						return true;
					}
				}
			} else if (type instanceof PList) {
				PList pList = (PList) type;
				PrimitiveType primitiveType = (PrimitiveType) pList.getElementType();
				List<String> value = jedis.lrange(objectKey, 0, -1);

				String valueType = TypeUtils.getCollectionType(value);
				if (valueType.equals(primitiveType.getName())) {
					return true;
				}
			} else if (type instanceof PSet) {
				PSet pSet = (PSet) type;
				PrimitiveType primitiveType = (PrimitiveType) pSet.getElementType();
				try {
					Set<String> value = jedis.zrevrange(objectKey, 0, -1);
					if (value.size() > 0) {
						String valueType = TypeUtils.getCollectionType(value);
						if (valueType.equals(primitiveType.getName())) {
							return true;
						}
					}
				} catch (Exception e) {
					Set<String> value = jedis.smembers(objectKey);
					if (value.size() > 0) {
						String valueType = TypeUtils.getCollectionType(value);
						if (valueType.equals(primitiveType.getName())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private boolean checkKeyAsVariationFeatures(StructuralVariation structuralVariation, Set<String> objectKeys) {
		for (StructuralFeature structuralFeature : structuralVariation.getStructuralFeatures()) {
			boolean found = checkKeyAsVariationFeature(objectKeys, structuralFeature);
			if (found) {
				return true;
			}
		}

		return false;
	}

	private boolean checkKeyAsVariationFeature(Set<String> objectKeys, StructuralFeature structuralFeature) {
		for (String objectKey : objectKeys) {
			if (structuralFeature instanceof Attribute) {
				Attribute attribute = (Attribute) structuralFeature;
				String propertyName = objectKey.substring(objectKey.lastIndexOf(COLON) + 1);

				if (propertyName.equals(attribute.getName())) {
					return true;
				}
			} else if (structuralFeature instanceof Aggregate) {
				Aggregate aggregate = (Aggregate) structuralFeature;

				if (objectKey.contains(LEFT_SQUARE_BRACKET) && objectKey.contains(RIGHT_SQUARE_BRACKET)) {
					String propertyName = objectKey.substring(objectKey.lastIndexOf(COLON) + 1,
							objectKey.lastIndexOf(LEFT_SQUARE_BRACKET));
					String subpropertyName = objectKey.substring(objectKey.lastIndexOf(LEFT_SQUARE_BRACKET) + 1,
							objectKey.lastIndexOf(RIGHT_SQUARE_BRACKET));

					boolean checkAggregate = checkAggregate(aggregate, propertyName, subpropertyName);
					if (checkAggregate)
						return true;
				} else if (objectKey.contains(DOT)) {
					String propertyName = objectKey.substring(objectKey.lastIndexOf(COLON) + 1,
							objectKey.lastIndexOf(DOT));
					String subpropertyName = objectKey.substring(objectKey.lastIndexOf(DOT) + 1);

					boolean checkAggregate = checkAggregate(aggregate, propertyName, subpropertyName);
					if (checkAggregate)
						return true;
				}
			}
		}

		return false;
	}

	private boolean checkAggregate(Aggregate aggregate, String propertyName, String subpropertyName) {
		if (propertyName.equals(aggregate.getName())) {
			for (StructuralVariation aggregatedStructuralVariation : aggregate.getAggregates()) {
				for (StructuralFeature aggregatedStructuralFeature : aggregatedStructuralVariation
						.getStructuralFeatures()) {
					if (subpropertyName.equals(aggregatedStructuralFeature.getName())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private int countStructuralVariationKeys(StructuralVariation structuralVariation) {
		int structuralFeaturesCount = 0;

		for (StructuralFeature structuralFeature : structuralVariation.getStructuralFeatures()) {
			if (structuralFeature instanceof Attribute) {
				structuralFeaturesCount++;
			} else if (structuralFeature instanceof Aggregate) {
				Aggregate aggregate = (Aggregate) structuralFeature;
				for (StructuralVariation aggregatedStructuralVariation : aggregate.getAggregates()) {
					structuralFeaturesCount += aggregatedStructuralVariation.getStructuralFeatures().stream()
							.filter(Attribute.class::isInstance).count();
				}
			}
		}
		return structuralFeaturesCount;
	}

}
