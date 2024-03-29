package es.um.uschema.documents.injectors.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import es.um.uschema.documents.injectors.util.DbType;

public class Companies2Db extends Source2Db
{
    private int MAX_LINES_BEFORE_STORE = 2000;

    public Companies2Db(DbType db, String ip)
    {
        super(db, ip);
    }

    @Override
    public void run(File jsonRoute, String dbName)
    {
        long startTime = System.currentTimeMillis();

        System.out.println("Reading json file " + jsonRoute + "...");
        storeJSONContent(jsonRoute, dbName);
        System.out.println(dbName + ":" + jsonRoute.getName() + " table created in "
                + (System.currentTimeMillis() - startTime) + " ms");
    }

    /*
     * private void storeJSONContent(String jsonRoute, String dbName) { File
     * jsonFile = new File(jsonRoute); MappingIterator<?> mappingIterator = null;
     * ObjectMapper mapper = new
     * ObjectMapper().setSerializationInclusion(Include.NON_EMPTY); String
     * collectionName = null;
     * 
     * SimpleModule module = new SimpleModule();
     * module.addDeserializer(Integer.class, new NumberToNumberDeserializer());
     * module.addDeserializer(String.class, new StringToStringDeserializer());
     * mapper.registerModule(module);
     * 
     * try { mappingIterator = mapper.reader(Company.class).readValues(jsonFile);
     * collectionName = "companies";
     * 
     * int numLines = 0; int totalLines = 1; ArrayNode jsonArray =
     * mapper.createArrayNode();
     * 
     * while (mappingIterator.hasNext()) {
     * jsonArray.add(mapper.readTree(mapper.writeValueAsString(mappingIterator.next(
     * ))));
     * 
     * if (++numLines == MAX_LINES_BEFORE_STORE) { getClient().insert(dbName,
     * collectionName, jsonArray.toString()); jsonArray.removeAll(); numLines = 0;
     * System.out.println("Line count: " + totalLines); }
     * 
     * totalLines++; }
     * 
     * if (jsonArray.size() > 0) { System.out.println("Storing remaining files...");
     * getClient().insert(dbName, collectionName, jsonArray.toString()); } } catch
     * (Exception e) { e.printStackTrace(); } }
     */
    private void storeJSONContent(File jsonRoute, String dbName)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonRoute)))
        {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode jsonArray = mapper.createArrayNode();
            String collectionName = "companies";
            int numLines = 0;
            int totalLines = 1;
            System.out.println("Storing each " + MAX_LINES_BEFORE_STORE);

            for (String line; (line = reader.readLine()) != null; totalLines++)
            {
                jsonArray.add(mapper.readTree(line));

                if (++numLines == MAX_LINES_BEFORE_STORE)
                {
                    getClient().insert(dbName, collectionName, jsonArray.toString());
                    jsonArray.removeAll();
                    numLines = 0;
                    System.out.println("Line count: " + totalLines);
                }
            }

            if (jsonArray.size() > 0)
            {
                System.out.println("Storing remaining files...");
                getClient().insert(dbName, collectionName, jsonArray.toString());
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}