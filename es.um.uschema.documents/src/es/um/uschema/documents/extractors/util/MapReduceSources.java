package es.um.uschema.documents.extractors.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class MapReduceSources
{
    private String mapJSCode;
    private String reduceJSCode;

    public MapReduceSources(Path mapRedDir)
    {
        try
        {
            // Read the map file
            Path mapFile = mapRedDir.resolve("map.js");
            mapJSCode = new String(Files.readAllBytes(mapFile));

            // Read the reduce file
            Path reduceFile = mapRedDir.resolve("reduce.js");
            reduceJSCode = new String(Files.readAllBytes(reduceFile));
        } catch (Exception e)
        {
            throw new IllegalArgumentException("MapReduce could not be found in " + mapRedDir);
        }
    }

    public String getMapJSCode()
    {
        return mapJSCode;
    }

    public String getReduceJSCode()
    {
        return reduceJSCode;
    }
}
