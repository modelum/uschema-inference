package es.um.unosql.neo4j.queries.inserts.cypher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.parquet.Strings;

import es.um.unosql.neo4j.queries.constants.Constants;

public class Neo4jCypherStatementsStore
{
	private List<String> cyphers;
	
	public Neo4jCypherStatementsStore()
	{
		cyphers = new LinkedList<String>();
	}
	
	public void add(String cypher)
	{
		cyphers.add(cypher);
	}
	
	public void print()
	{
		cyphers.stream().forEach(System.out::println);
	}
	
	public void printToFile(String fileUri)
	{
		File directories = new File(fileUri.substring(0, fileUri.lastIndexOf('/')));
		directories.mkdirs();
		
		File file = new File(fileUri);
		FileWriter fileWriter;
		try
		{
			fileWriter = new FileWriter(file);

			fileWriter.write(Strings.join(cyphers, Constants.LINE));
			fileWriter.write(Constants.LINE);
			
			fileWriter.close();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
