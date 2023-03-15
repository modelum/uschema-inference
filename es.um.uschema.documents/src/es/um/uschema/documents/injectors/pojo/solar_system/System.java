package es.um.uschema.documents.injectors.pojo.solar_system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"bodies"})
public class System
{
  private Bodies[] bodies;

  @JsonProperty("bodies")
  public Bodies[] getBodies() {
    return bodies;
  }

  public void setBodies(Bodies[] bodies) {
    this.bodies = bodies;
  }
}
