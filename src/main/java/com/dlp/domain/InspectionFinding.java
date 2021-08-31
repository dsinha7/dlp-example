package com.dlp.domain;

import com.google.privacy.dlp.v2.Likelihood;

public class InspectionFinding {
    private String columnName;
    private String infoType;
    private Likelihood likelyHood;
    private String dataElement;

    public InspectionFinding(){
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getInfoType() {
        return infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public Likelihood getLikelyHood() {
        return likelyHood;
    }

    public void setLikelyHood(Likelihood likelyHood) {
        this.likelyHood = likelyHood;
    }

    public String getDataElement() {
        return dataElement;
    }

    public void setDataElement(String dataElement) {
        this.dataElement = dataElement;
    }

    public InspectionFinding(String columnName, String infoType, Likelihood likelyHood, String dataElement) {
        this.columnName = columnName;
        this.infoType = infoType;
        this.likelyHood = likelyHood;
        this.dataElement = dataElement;
    }

    @Override
    public String toString() {
        return "InspectionFindings{" +
                "columnName='" + columnName + '\'' +
                ", infoType='" + infoType + '\'' +
                ", likelyHood='" + likelyHood + '\'' +
                ", dataElement='" + dataElement + '\'' +
                '}';
    }

    public String toCSV() {
        return columnName + ","
                + infoType + ","
                + likelyHood + ","
                + dataElement + ",";
    }
}
