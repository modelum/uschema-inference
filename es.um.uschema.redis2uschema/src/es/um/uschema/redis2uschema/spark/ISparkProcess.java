package es.um.uschema.redis2uschema.spark;

import java.util.Map;

public interface ISparkProcess {

	Map<String, Long> process(String databaseUrl, int databasePort, String databaseName);

}