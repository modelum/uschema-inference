package es.um.uschema.documents.extractors.util;

import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CouchDBStreamAdapter extends AbstractStreamAdapter
{
  public Stream<JsonObject> adaptStream(List<JsonObject> itemList)
  {
    return itemList.stream().map(jsonObject ->
    {
      JsonObject result = JsonParser.parseString(jsonObject.get("key").getAsString()).getAsJsonObject();
//      result.remove("_rev");
//      result.remove("_id");

      return result;
    });
  }
}
