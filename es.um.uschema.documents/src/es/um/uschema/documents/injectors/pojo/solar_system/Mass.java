package es.um.uschema.documents.injectors.pojo.solar_system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"massExponent", "massValue"})
public class Mass
{
  private Integer massExponent;

  private Double massValue;

  @JsonProperty("massExponent")
  public Integer getMassExponent() {
    return massExponent;
  }

  public void setMassExponent(Integer massExponent) {
    this.massExponent = massExponent;
  }

  @JsonProperty("massValue")
  public Double getMassValue() {
    return massValue;
  }

  public void setMassValue(Double massValue) {
    this.massValue = massValue;
  }
}
