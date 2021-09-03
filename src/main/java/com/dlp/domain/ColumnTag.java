package com.dlp.domain;

public class ColumnTag {

    //Risk Category
    private final String riskCategory;
    //Data Domain
    private final String dataDomain;
    //Sensitivity
    private final String sensitivity;
    //should_encrypt
    private final Boolean shouldEncrypt;
    private final String tableName;
    private final String columnName;

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }


    public ColumnTag(String tableName, String columnName, String riskCategory, String dataDomain, String sensitivity, Boolean shouldEncrypt) {
        this.riskCategory = riskCategory;
        this.dataDomain = dataDomain;
        this.sensitivity = sensitivity;
        this.shouldEncrypt = shouldEncrypt;
        this.tableName = tableName;
        this.columnName = columnName;
    }
    //standardization_rules


    public String getRiskCategory() {
        return riskCategory;
    }

    public String getDataDomain() {
        return dataDomain;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public Boolean getShouldEncrypt() {
        return shouldEncrypt;
    }
}
