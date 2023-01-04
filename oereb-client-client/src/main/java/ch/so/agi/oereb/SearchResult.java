package ch.so.agi.oereb;

import java.io.Serializable;

public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String label;
    
    private String origin;
    
    private ol.Coordinate coordinate;
    
    private String canton;
    
    private String searchText;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public ol.Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(ol.Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    } 
}
