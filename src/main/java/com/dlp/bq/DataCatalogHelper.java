package com.dlp.bq;

import com.dlp.domain.ColumnTag;
import com.google.cloud.datacatalog.v1.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataCatalogHelper {


    private static TagTemplate getTagTemplate(DataCatalogClient dataCatalogClient, String tagTemplateId, String location, String projectId) {
        TagTemplate tagTemplate = null;
        String tagTemplateName =
                String.format("projects/%s/locations/%s/tagTemplates/%s", projectId, location, tagTemplateId);
        // try to load the template
        try {

            tagTemplate = dataCatalogClient.getTagTemplate(tagTemplateName);

        } catch (Exception e) {
            System.out.println("Template not found " + tagTemplateName);
            e.printStackTrace();
        }
        return tagTemplate;
    }
    public static void updateColumnTagTemplate(String projectId, String templateId, String bqDataSetId, String tagRegion, String csvFilePath) throws Exception {

        //read the CSV
        CSVHelper csv = new CSVHelper();
        csv.readCsv(csvFilePath);
        List<ColumnTag> rows = csv.getRows();
        //TODO :: group the CSV data by table name in to a Map. Assuming only one table for now in case Ford wants to upload multiple tables in same CSV.
        String tableName = rows.get(0).getTableName();

        String qualifiedTablePath = String.format(
                "//bigquery.googleapis.com/projects/%s/datasets/%s/tables/%s",
                projectId, bqDataSetId, tableName);

        System.out.println("Table to tag [" + qualifiedTablePath + "]");


        try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {

            TagTemplate tagTemplate = getTagTemplate(dataCatalogClient, templateId, tagRegion, projectId);

            if (tagTemplate == null) {
                throw new Exception("Could not get template ");
            }

            for (ColumnTag tag : rows) {

                Tag.Builder tagBuilder =
                        Tag.newBuilder()
                                .setTemplate(tagTemplate.getName());

                for(String infoType : csv.getHeaders()){
                    //tablename,columnname,riskCategory,DataDomain,Sensitivity,should_encrypt
                    if( infoType.toLowerCase().equalsIgnoreCase("riskcategory") ){

                        TagField tagField = TagField.newBuilder().setEnumValue(
                                TagField.EnumValue.newBuilder().setDisplayName(tag.getRiskCategory()).build()).build();
                        tagBuilder.putFields("riskcategory", tagField);

                    }else if(infoType.toLowerCase().equalsIgnoreCase("datadomain")){
                        TagField tagField = TagField.newBuilder().setEnumValue(
                                TagField.EnumValue.newBuilder().setDisplayName(tag.getDataDomain()).build()).build();
                        tagBuilder.putFields("datadomain", tagField);

                    }
                    else if(infoType.toLowerCase().equalsIgnoreCase("sensitivity")){
                        TagField tagField = TagField.newBuilder().setEnumValue(
                                TagField.EnumValue.newBuilder().setDisplayName(tag.getSensitivity()).build()).build();
                        tagBuilder.putFields("sensitivity", tagField);

                    }else if (infoType.toLowerCase().equalsIgnoreCase("shouldencrypt")) {
                        TagField tagField = TagField.newBuilder().setBoolValue(tag.getShouldEncrypt()).build();
                        tagBuilder.putFields("shouldencrypt", tagField);
                    }else if(infoType.toLowerCase().equalsIgnoreCase("tablename") ||
                            infoType.toLowerCase().equalsIgnoreCase("columnname") ){
                        //ignore since they are not used as tag attribute
                    }
                    else {
                        System.out.println(" Attribute " + infoType + " not supported for the tag template " + templateId);
                    }

                }
                //TODO: Handle failure due to tag already attached
                LookupEntryRequest lookupEntryRequest =
                        LookupEntryRequest.newBuilder().setLinkedResource(qualifiedTablePath).build();
                Entry tableEntry = dataCatalogClient.lookupEntry(lookupEntryRequest);

                CreateTagRequest createTagRequest =
                        CreateTagRequest.newBuilder().setParent(tableEntry.getName())
                                //set the column name to make it column level tag.
                                .setTag(tagBuilder.setColumn(tag.getColumnName()).build()).build();
                dataCatalogClient.createTag(createTagRequest);

            }// for each column

            System.out.println(String.format("Successfully update table [%s] columns with tag template [%s] !!", tableName, templateId));

        } catch (IOException e) {
            //TODO:: handle error
            e.printStackTrace();
        }
    }


}
