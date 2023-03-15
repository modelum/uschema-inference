package es.um.uschema.documents.injectors.pojo.solar_system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"moon", "rel"})
public class Moon
{
  private String moon;

  private String rel;

  @JsonProperty("moon")
  public String getMoon() {
    return moon;
  }

  public void setMoon(String moon) {
    this.moon = moon;
  }

  @JsonProperty("rel")
  public String getRel() {
    return rel;
  }

  public void setRel(String rel) {
    this.rel = rel;
  }
}
