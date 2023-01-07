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
    
    private Map<TypeTriple, Restriction> restrictions = new HashMap<TypeTriple, Restriction>(); 
        
    private Set<Document> documents = new HashSet<>(); 
    
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

    public Map<TypeTriple, Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Map<TypeTriple, Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

    public List<Office> getResponsibleOffice() {
        return responsibleOffice;
    }

    public void setResponsibleOffice(List<Office> responsibleOffice) {
        this.responsibleOffice = responsibleOffice;
    }
}
