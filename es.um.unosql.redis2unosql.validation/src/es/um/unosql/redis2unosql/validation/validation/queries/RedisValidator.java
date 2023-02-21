package es.um.unosql.redis2unosql.validation.validation.queries;

import es.um.unosql.redis2unosql.validation.queries.statistics.StatisticsMeter;
import es.um.unosql.uNoSQLSchema.StructuralVariation;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;
import redis.clients.jedis.Jedis;

public class RedisValidator implements AutoCloseable {

	private StatisticsMeter statisticsMeter;
	private Jedis jedis;
	
	public RedisValidator(String databaseUri, int port) {
		this.jedis = new Jedis("localhost", port);
		this.statisticsMeter = new StatisticsMeter();
	}

	public void validate(uNoSQLSchema uNoSQLSchema) {
		uNoSQLSchema.getEntities().stream().filter(e -> e.isRoot()).forEach(et -> {
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