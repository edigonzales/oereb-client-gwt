package ch.so.agi.oereb.model;

public class TypeTriple {
    private String typeCode;
    private String typeCodeList;
    private String lawStatus;

    public TypeTriple() {}
    
    public TypeTriple(String typeCode, String typeCodeList, String lawStatus) {
        this.typeCode = typeCode;
        this.typeCodeList = typeCodeList;
        this.lawStatus = lawStatus;
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
        result = prime * result + ((typeCode == null) ? 0 : typeCode.hashCode());
        result = prime * result + ((typeCodeList == null) ? 0 : typeCodeList.hashCode());
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
        if (lawStatus == null) {
            if (other.lawStatus != null)
                return false;
        } else if (!lawStatus.equals(other.lawStatus))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TypeTriple [typeCode=" + typeCode + ", typeCodeList=" + typeCodeList + ", lawStatus=" + lawStatus + "]";
    }
}