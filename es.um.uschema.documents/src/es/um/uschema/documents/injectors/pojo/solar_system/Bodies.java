package es.um.uschema.documents.injectors.pojo.solar_system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"_id", "alternativeName", "aphelion", "aroundPlanet", "axialTilt", "density", "dimension", "discoveredBy", "discoveryDate", "eccentricity", "englishName", "equaRadius",
  "escape", "flattening", "gravity", "inclination", "isPlanet", "mass", "meanRadius", "moons", "name", "perihelion", "polarRadius", "rel", "semimajorAxis", "sideralOrbit", "sideralRotation", "vol"})
public class Bodies
{
  private String _id;

  private String alternativeName;

  private Long aphelion;

  private Aroundplanet aroundPlanet;

  private Double axialTilt;

  private Double density;

  private String dimension;

  private String discoveredBy;

  private String discoveryDate;

  private Double eccentricity;

  private String englishName;

  private Double equaRadius;

  private Double escape;

  private Double flattening;

  private Double gravity;

  private Double inclination;

  private Boolean isPlanet;

  private Mass mass;

  private Double meanRadius;

  private Moon[] moons;

  private String name;

  private Long perihelion;

  private Double polarRadius;

  private String rel;

  private Double semimajorAxis;

  private Double sideralOrbit;

  private Double sideralRotation;

  private Vol vol;

  @JsonProperty("_id")
  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
  }

  @JsonProperty("alternativeName")
  public String getAlternativeName() {
    return alternativeName;
  }

  public void setAlternativeName(String alternativeName) {
    this.alternativeName = alternativeName;
  }

  @JsonProperty("aphelion")
  public Long getAphelion() {
    return aphelion;
  }

  public void setAphelion(Long aphelion) {
    this.aphelion = aphelion;
  }

  @JsonProperty("aroundPlanet")
  public Aroundplanet getAroundPlanet() {
    return aroundPlanet;
  }

  public void setAroundPlanet(Aroundplanet aroundPlanet) {
    this.aroundPlanet = aroundPlanet;
  }

  @JsonProperty("axialTilt")
  public Double getAxialTilt() {
    return axialTilt;
  }

  public void setAxialTilt(Double axialTilt) {
    this.axialTilt = axialTilt;
  }

  @JsonProperty("density")
  public Double getDensity() {
    return density;
  }

  public void setDensity(Double density) {
    this.density = density;
  }

  @JsonProperty("dimension")
  public String getDimension() {
    return dimension;
  }

  public void setDimension(String dimension) {
    this.dimension = dimension;
  }

  @JsonProperty("discoveredBy")
  public String getDiscoveredBy() {
    return discoveredBy;
  }

  public void setDiscoveredBy(String discoveredBy) {
    this.discoveredBy = discoveredBy;
  }

  @JsonProperty("discoveryDate")
  public String getDiscoveryDate() {
    return discoveryDate;
  }

  public void setDiscoveryDate(String discoveryDate) {
    this.discoveryDate = discoveryDate;
  }

  @JsonProperty("eccentricity")
  public Double getEccentricity() {
    return eccentricity;
  }

  public void setEccentricity(Double eccentricity) {
    this.eccentricity = eccentricity;
  }

  @JsonProperty("englishName")
  public String getEnglishName() {
    return englishName;
  }

  public void setEnglishName(String englishName) {
    this.englishName = englishName;
  }

  @JsonProperty("equaRadius")
  public Double getEquaRadius() {
    return equaRadius;
  }

  public void setEquaRadius(Double equaRadius) {
    this.equaRadius = equaRadius;
  }

  @JsonProperty("escape")
  public Double getEscape() {
    return escape;
  }

  public void setEscape(Double escape) {
    this.escape = escape;
  }

  @JsonProperty("flattening")
  public Double getFlattening() {
    return flattening;
  }

  public void setFlattening(Double flattening) {
    this.flattening = flattening;
  }

  @JsonProperty("gravity")
  public Double getGravity() {
    return gravity;
  }

  public void setGravity(Double gravity) {
    this.gravity = gravity;
  }

  @JsonProperty("inclination")
  public Double getInclination() {
    return inclination;
  }

  public void setInclination(Double inclination) {
    this.inclination = inclination;
  }

  @JsonProperty("isPlanet")
  public Boolean getIsPlanet() {
    return isPlanet;
  }

  public void setIsPlanet(Boolean isPlanet) {
    this.isPlanet = isPlanet;
  }

  @JsonProperty("mass")
  public Mass getMass() {
    return mass;
  }

  public void setMass(Mass mass) {
    this.mass = mass;
  }

  @JsonProperty("meanRadius")
  public Double getMeanRadius() {
    return meanRadius;
  }

  public void setMeanRadius(Double meanRadius) {
    this.meanRadius = meanRadius;
  }

  @JsonProperty("moons")
  public Moon[] getMoons() {
    return moons;
  }

  public void setMoons(Moon[] moons) {
    this.moons = moons;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("perihelion")
  public Long getPerihelion() {
    return perihelion;
  }

  public void setPerihelion(Long perihelion) {
    this.perihelion = perihelion;
  }

  @JsonProperty("polarRadius")
  public Double getPolarRadius() {
    return polarRadius;
  }

  public void setPolarRadius(Double polarRadius) {
    this.polarRadius = polarRadius;
  }

  @JsonProperty("rel")
  public String getRel() {
    return rel;
  }

  public void setRel(String rel) {
    this.rel = rel;
  }

  @JsonProperty("semimajorAxis")
  public Double getSemimajorAxis() {
    return semimajorAxis;
  }

  public void setSemimajorAxis(Double semimajorAxis) {
    this.semimajorAxis = semimajorAxis;
  }

  @JsonProperty("sideralOrbit")
  public Double getSideralOrbit() {
    return sideralOrbit;
  }

  public void setSideralOrbit(Double sideralOrbit) {
    this.sideralOrbit = sideralOrbit;
  }

  @JsonProperty("sideralRotation")
  public Double getSideralRotation() {
    return sideralRotation;
  }

  public void setSideralRotation(Double sideralRotation) {
    this.sideralRotation = sideralRotation;
  }

  @JsonProperty("vol")
  public Vol getVol() {
    return vol;
  }

  public void setVol(Vol vol) {
    this.vol = vol;
  }
}
