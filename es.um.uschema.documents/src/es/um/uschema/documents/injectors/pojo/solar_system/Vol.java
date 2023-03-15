package es.um.uschema.documents.injectors.pojo.solar_system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"volExponent", "volValue"})
public class Vol
{
  private Integer volExponent;

  private Double volValue;

  @JsonProperty("volExponent")
  public Integer getVolExponent() {
    return volExponent;
  }

  public void setVolExponent(Integer volExponent) {
    this.volExponent = volExponent;
  }

  @JsonProperty("volValue")
  public Double getVolValue() {
    return volValue;
  }

  public void setVolValue(Double volValue) {
    this.volValue = volValue;
  }
}
