package es.um.uschema.documents.extractors.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class PathUtils
{
    public static Path get(URI uri)
    {
        try
        {
            Path p = Paths.get(uri);
            return p;
        } catch (FileSystemNotFoundException ex)
        {
            FileSystem fs;
            try
            {
                fs = FileSystems.newFileSystem(
                        uri, Collections.<String, Object>emptyMap());
                return fs.provider().getPath(uri);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }
}
