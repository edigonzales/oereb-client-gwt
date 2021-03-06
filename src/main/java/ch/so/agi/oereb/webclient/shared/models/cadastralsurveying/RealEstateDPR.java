package ch.so.agi.oereb.webclient.shared.models.cadastralsurveying;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RealEstateDPR implements IsSerializable {
    private String realEstateType;
    
    private String number;
    
    private String identND;
    
    private String egrid;
    
    private String canton;
    
    private String municipality;
    
    private String subunitOfLandRegister;
    
    private int fosnNr;
    
    private int landRegistryArea;
    
    private Office surveyorOffice;
    
    private Office landRegisterOffice;
    
    private List<String> localNames;
    
    private Map<String, Integer> landCoverShares;
    
    private List<Building> buildings;

    public String getRealEstateType() {
        return realEstateType;
    }

    public void setRealEstateType(String realEstateType) {
        this.realEstateType = realEstateType;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIdentND() {
        return identND;
    }

    public void setIdentND(String identND) {
        this.identND = identND;
    }

    public String getEgrid() {
        return egrid;
    }

    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getSubunitOfLandRegister() {
        return subunitOfLandRegister;
    }

    public void setSubunitOfLandRegister(String subunitOfLandRegister) {
        this.subunitOfLandRegister = subunitOfLandRegister;
    }

    public int getFosnNr() {
        return fosnNr;
    }

    public void setFosnNr(int fosnNr) {
        this.fosnNr = fosnNr;
    }

    public int getLandRegistryArea() {
        return landRegistryArea;
    }

    public void setLandRegistryArea(int landRegistryArea) {
        this.landRegistryArea = landRegistryArea;
    }

    public Office getSurveyorOffice() {
        return surveyorOffice;
    }

    public void setSurveyorOffice(Office surveyorOffice) {
        this.surveyorOffice = surveyorOffice;
    }

    public Office getLandRegisterOffice() {
        return landRegisterOffice;
    }

    public void setLandRegisterOffice(Office landRegisterOffice) {
        this.landRegisterOffice = landRegisterOffice;
    }

    public List<String> getLocalNames() {
        return localNames;
    }

    public void setLocalNames(List<String> localNames) {
        this.localNames = localNames;
    }

    public Map<String, Integer> getLandCoverShares() {
        return landCoverShares;
    }

    public void setLandCoverShares(Map<String, Integer> landCoverShares) {
        this.landCoverShares = landCoverShares;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }
}
