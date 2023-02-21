package es.um.unosql.redis2unosql.spark;

import java.util.Map;

public interface ISparkProcess {

	Map<String, Long> process(String databaseUrl, int databasePort, String databaseName);

}