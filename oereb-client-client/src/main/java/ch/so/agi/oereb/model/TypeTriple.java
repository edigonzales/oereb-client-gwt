package ch.so.agi.oereb.model;

public class TypeTriple {
    private String themeCode;
    private String typeCode;
    private String typeCodeList;
    private String symbolRef;
    private String lawStatus;

    public TypeTriple() {}
    
    public TypeTriple(String themeCode, String typeCode, String typeCodeList, String symbolRef, String lawStatus) {
        this.themeCode = themeCode;
        this.typeCode = typeCode;
        this.typeCodeList = typeCodeList;
        this.symbolRef = symbolRef;
        this.lawStatus = lawStatus;
    }
    
    public String getThemeCode() {
        return themeCode;
    }

    public void setThemeCode(String themeCode) {
        this.themeCode = themeCode;
    }
    
    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeCodeList() {
        return typeCodeList;
    }

    public void setTypeCodeList(String typeCodeList) {
        this.typeCodeList = typeCodeList;
    }
    
    public String getSymbolRef() {
        return symbolRef;
    }

    public void setSymbolRef(String symbolRef) {
        this.symbolRef = symbolRef;
    }

    public String getLawStatus() {
        return lawStatus;
    }

    public void setLawStatus(String lawStatus) {
        this.lawStatus = lawStatus;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((themeCode == null) ? 0 : themeCode.hashCode());
        result = prime * result + ((typeCode == null) ? 0 : typeCode.hashCode());
        result = prime * result + ((typeCodeList == null) ? 0 : typeCodeList.hashCode());
        result = prime * result + ((symbolRef == null) ? 0 : symbolRef.hashCode());
        result = prime * result + ((lawStatus == null) ? 0 : lawStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeTriple other = (TypeTriple) obj;
        if (themeCode == null) {
            if (other.themeCode != null)
                return false;
        } else if (!themeCode.equals(other.themeCode))
            return false;
        if (typeCode == null) {
            if (other.typeCode != null)
                return false;
        } else if (!typeCode.equals(other.typeCode))
            return false;
        if (typeCodeList == null) {
            if (other.typeCodeList != null)
                return false;
        } else if (!typeCodeList.equals(other.typeCodeList))
            return false;
        if (symbolRef == null) {
            if (other.symbolRef != null)
                return false;
        } else if (!symbolRef.equals(other.symbolRef))
            return false;
        if (lawStatus == null) {
            if (other.lawStatus != null)
                return false;
        } else if (!lawStatus.equals(other.lawStatus))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TypeTriple [themeCode=" + themeCode + ", typeCode=" + typeCode + ", typeCodeList=" + typeCodeList + ", symbolRef="+symbolRef+ ", lawStatus=" + lawStatus + "]";
    }
}