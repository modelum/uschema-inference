package es.um.unosql.neo4j2unosql.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import es.um.unosql.neo4j2unosql.model.Json2NoSQLSchemaModel;

public class Json2uSchemaMain
{
	private static final String DATABASE_NAME = "UserProfile";
	private static final String OUTPUTS_FOLDER = "./outputs/";

	private static final String XMI_EXTENSION = ".xmi";
	private static final String JSON_EXTENSION = ".json";

	public static void main(String[] args) throws IOException
	{
		long start = System.currentTimeMillis();
		
		String jsonUri = OUTPUTS_FOLDER + DATABASE_NAME + JSON_EXTENSION;
		String outputUri = OUTPUTS_FOLDER + DATABASE_NAME + XMI_EXTENSION;
		
		Map<String, Long> schemaCounts = readJSONFile(jsonUri);
		generateModel(schemaCounts, outputUri);

		long executionTime = System.currentTimeMillis();
		
		showTime(start, executionTime);
	}

	private static void showTime(long start, long executionTime) {
		System.out.println("Total Time: " + String.format("%d min, %02d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(executionTime - start),
			    TimeUnit.MILLISECONDS.toSeconds(executionTime - start) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime - start))
			)
		);
	}

	private static Map<String, Long> readJSONFile(String jsonUri) throws FileNotFoundException, IOException
	{
		Map<String, Long> schemaCounts = new HashMap<String, Long>();
		
		FileReader fileReader = new FileReader(new File(jsonUri));
		try (BufferedReader br = new BufferedReader(fileReader))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				String key = line;
				Long value = Long.parseLong(br.readLine());
				schemaCounts.put(key, value);
			}
		}
		fileReader.close();
		
		return schemaCounts;
	}

	private static void generateModel(Map<String, Long> schemaCounts, String outputUri)
	{
		Json2NoSQLSchemaModel modelBuilder = new Json2NoSQLSchemaModel(DATABASE_NAME);
		modelBuilder.processArchetypes(schemaCounts);
		modelBuilder.toXMI(outputUri);
	}

}
