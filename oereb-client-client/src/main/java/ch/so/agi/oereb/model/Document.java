package ch.so.agi.oereb.model;

import java.util.Objects;

public class Document {
    private String type;
    
    private String typeText;
    
    private int idx;
        
    private String title;
    
    private String officialTitle;
    
    private String officialNumber;
    
    private String abbreviation;
    
    private String textAtWeb;
    
    private String lawStatus;
    
    private String lawStatusText;
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getTextAtWeb() {
        return textAtWeb;
    }

    public void setTextAtWeb(String textAtWeb) {
        this.textAtWeb = textAtWeb;
    }

    public String getOfficialTitle() {
        return officialTitle;
    }

    public void setOfficialTitle(String officialTitle) {
        this.officialTitle = officialTitle;
    }

    public String getOfficialNumber() {
        return officialNumber;
    }

    public void setOfficialNumber(String officialNumber) {
        this.officialNumber = officialNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Document document = (Document) o;
        return Objects.equals(textAtWeb, document.textAtWeb);
    }
    
    @Override
    public int hashCode() {
        return textAtWeb.hashCode();
    }
}
