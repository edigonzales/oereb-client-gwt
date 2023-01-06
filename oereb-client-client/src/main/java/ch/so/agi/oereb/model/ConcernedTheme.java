package ch.so.agi.oereb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConcernedTheme {
    private String code;
    
    private String name;
    
    private String subtheme;
    
    private ReferenceWMS referenceWMS;
    
    private Map<TypeTuple, Restriction> restrictions = new HashMap<TypeTuple, Restriction>(); 
        
    private Set<Document> legalProvisions = new HashSet<>(); 
    
    private Set<Document> laws = new HashSet<>();
    
    private Set<Document> hints = new HashSet<>();
    
    private List<Office> responsibleOffice = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtheme() {
        return subtheme;
    }

    public void setSubtheme(String subtheme) {
        this.subtheme = subtheme;
    }

    public ReferenceWMS getReferenceWMS() {
        return referenceWMS;
    }

    public void setReferenceWMS(ReferenceWMS referenceWMS) {
        this.referenceWMS = referenceWMS;
    }

    public Map<TypeTuple, Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Map<TypeTuple, Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public Set<Document> getLegalProvisions() {
        return legalProvisions;
    }

    public void setLegalProvisions(Set<Document> legalProvisions) {
        this.legalProvisions = legalProvisions;
    }

    public Set<Document> getLaws() {
        return laws;
    }

    public void setLaws(Set<Document> laws) {
        this.laws = laws;
    }

    public Set<Document> getHints() {
        return hints;
    }

    public void setHints(Set<Document> hints) {
        this.hints = hints;
    }

    public List<Office> getResponsibleOffice() {
        return responsibleOffice;
    }

    public void setResponsibleOffice(List<Office> responsibleOffice) {
        this.responsibleOffice = responsibleOffice;
    }
}
