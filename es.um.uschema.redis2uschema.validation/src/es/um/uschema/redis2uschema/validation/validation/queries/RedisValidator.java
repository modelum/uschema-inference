package es.um.uschema.redis2uschema.validation.validation.queries;

import es.um.uschema.USchema.StructuralVariation;
import es.um.uschema.USchema.USchema;
import es.um.uschema.redis2uschema.validation.queries.statistics.StatisticsMeter;
import redis.clients.jedis.Jedis;

public class RedisValidator implements AutoCloseable {

	private StatisticsMeter statisticsMeter;
	private Jedis jedis;
	
	public RedisValidator(String databaseUri, int port) {
		this.jedis = new Jedis("localhost", port);
		this.statisticsMeter = new StatisticsMeter();
	}

	public void validate(USchema uSchema) {
	  uSchema.getEntities().stream().filter(e -> e.isRoot()).forEach(et -> {
			et.getVariations().forEach(sv -> {
				performQuery(sv);
			});
		});
	}

	private int performQuery(StructuralVariation structuralVariation) {
		RedisQueryExecutor redisQueryExecutor = new RedisQueryExecutor(jedis);
		int results = redisQueryExecutor.getAll(structuralVariation);
		
		statisticsMeter.saveStructuralVariationStatistics(structuralVariation, results);

		return results;
	}

	public String getStatisticsAsString() {
		if (statisticsMeter == null) {
			return null;
		}

		return statisticsMeter.getStatisticsAsString();
	}

	@Override
	public void close() {
		if (jedis != null)
			jedis.close();
	}

}