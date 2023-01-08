package ch.so.agi.oereb;

public class Grundstueck {
    private String nummer;
    private String nbident;
    private String egrid;
    private String art;
    private String canton;
    private ol.geom.Geometry geometrie;
    private String municipalityName;
    private String municipalityNumber;
    private String subunitOfLandRegister;
    private String subunitOfLandRegisterDesignation;
    private int landRegistryArea;
    
    public String getNummer() {
        return nummer;
    }
    public void setNummer(String nummer) {
        this.nummer = nummer;
    }
    public String getNbident() {
        return nbident;
    }
    public void setNbident(String nbident) {
        this.nbident = nbident;
    }
    public String getEgrid() {
        return egrid;
    }
    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }
    public String getArt() {
        return art;
    }
    public void setArt(String art) {
        this.art = art;
    }
    public String getCanton() {
        return canton;
    }
    public void setCanton(String canton) {
        this.canton = canton;
    }
    public ol.geom.Geometry getGeometrie() {
        return geometrie;
    }
    public void setGeometrie(ol.geom.Geometry geometrie) {
        this.geometrie = geometrie;
    }
    public String getMunicipalityName() {
        return municipalityName;
    }
    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }
    public String getMunicipalityNumber() {
        return municipalityNumber;
    }
    public void setMunicipalityNumber(String municipalityNumber) {
        this.municipalityNumber = municipalityNumber;
    }
    public String getSubunitOfLandRegister() {
        return subunitOfLandRegister;
    }
    public void setSubunitOfLandRegister(String subunitOfLandRegister) {
        this.subunitOfLandRegister = subunitOfLandRegister;
    }
    public String getSubunitOfLandRegisterDesignation() {
        return subunitOfLandRegisterDesignation;
    }
    public void setSubunitOfLandRegisterDesignation(String subunitOfLandRegisterDesignation) {
        this.subunitOfLandRegisterDesignation = subunitOfLandRegisterDesignation;
    }
    public int getLandRegistryArea() {
        return landRegistryArea;
    }
    public void setLandRegistryArea(int landRegistryArea) {
        this.landRegistryArea = landRegistryArea;
    }
}
