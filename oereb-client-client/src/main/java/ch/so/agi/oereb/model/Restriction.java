package ch.so.agi.oereb.model;

import static elemental2.dom.DomGlobal.console;

public class Restriction {
    private String legendText;
    
    private String symbol;
    
    private String symbolRef;
    
    private String typeCode;
    
    private String typeCodelist;
    
    private Integer areaShare;
    
    private Integer lengthShare;
    
    private Integer nrOfPoints;
    
    private Double partInPercent;
    
    private String multiPolygonGeometry;
    
    private String multiLineStringGeometry;
    
    private String multiPointGeometry;
    
    private String lawStatus;
    
    private String lawStatusText;

    public String getLegendText() {
        return legendText;
    }

    public void setLegendText(String legendText) {
        this.legendText = legendText;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbolRef() {
        return symbolRef;
    }

    public void setSymbolRef(String symbolRef) {
        this.symbolRef = symbolRef;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeCodelist() {
        return typeCodelist;
    }

    public void setTypeCodelist(String typeCodelist) {
        this.typeCodelist = typeCodelist;
    }

    public Integer getAreaShare() {
        return areaShare;
    }

    public void setAreaShare(Integer areaShare) {
        this.areaShare = areaShare;
    }
    
    public void updateAreaShare(Integer areaShare) {
        this.areaShare += areaShare;
    }

    public Integer getLengthShare() {
        return lengthShare;
    }

    public void setLengthShare(Integer lengthShare) {
        this.lengthShare = lengthShare;
    }
    
    public void updateLengthShare(Integer lengthShare) {
        this.lengthShare += lengthShare;
    }

    public Integer getNrOfPoints() {
        return nrOfPoints;
    }

    public void setNrOfPoints(Integer nrOfPoints) {
        this.nrOfPoints = nrOfPoints;
    }
    
    public void updateNrOfPoints(Integer nrOfPoints) {
        this.nrOfPoints += nrOfPoints;
    }

    public Double getPartInPercent() {
        return partInPercent;
    }

    public void setPartInPercent(Double partInPercent) {
        this.partInPercent = partInPercent;
    }
    
    public void updatePartInPercent(Double partInPercent) {
        this.partInPercent += partInPercent;
    }

    public String getMultiPolygonGeometry() {
        return multiPolygonGeometry;
    }

    public void setMultiPolygonGeometry(String multiPolygonGeometry) {
        this.multiPolygonGeometry = multiPolygonGeometry;
    }

    public String getMultiLineStringGeometry() {
        return multiLineStringGeometry;
    }

    public void setMultiLineStringGeometry(String multiLineStringGeometry) {
        this.multiLineStringGeometry = multiLineStringGeometry;
    }

    public String getMultiPointGeometry() {
        return multiPointGeometry;
    }

    public void setMultiPointGeometry(String multiPointGeometry) {
        this.multiPointGeometry = multiPointGeometry;
    }

    public String getLawStatus() {
        return lawStatus;
    }

    public void setLawStatus(String lawStatus) {
        this.lawStatus = lawStatus;
    }

    public String getLawStatusText() {
        return lawStatusText;
    }

    public void setLawStatusText(String lawStatusText) {
        this.lawStatusText = lawStatusText;
    }
}
