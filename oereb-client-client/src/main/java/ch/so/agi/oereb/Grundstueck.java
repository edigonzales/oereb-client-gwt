package ch.so.agi.oereb;

public class Grundstueck {
    private String nummer;
    private String nbident;
    private String egrid;
    private String art;
    private String canton;
    private ol.geom.Geometry geometrie;
    
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
}
