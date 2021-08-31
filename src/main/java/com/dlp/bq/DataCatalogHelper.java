package com.dlp.bq;

import com.google.cloud.datacatalog.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static void populateColumnTempate(String projectId, String templateId, String qualifiedTablePath, Map<String, Map<String,Object>> columnTagValue) throws Exception{

        try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {



            TagTemplate tagTemplate = getTagTemplate(dataCatalogClient, templateId, "us-central1", projectId);

            if(tagTemplate == null ){
                throw new Exception("Could not get template ");
            }



            //refactor this code to make generic
            for( String columnName : columnTagValue.keySet()){
                Map<String,Object> innerMap = columnTagValue.get(columnName);
                Tag.Builder tagBuilder =
                        Tag.newBuilder()
                                .setTemplate(tagTemplate.getName());

                for(String attributeName: innerMap.keySet()){
                    Object value = innerMap.get(attributeName);
                    if("INFO_TYPE".equalsIgnoreCase(attributeName)){
                        //TagField infoType = TagField.newBuilder().setStringValue((String)value).build();
                        TagField infoType = TagField.newBuilder().setEnumValue(
                                TagField.EnumValue.newBuilder().setDisplayName((String)value).build()).build();
                        tagBuilder.putFields("info_type", infoType);
                    } else if ("IS_PII".equalsIgnoreCase(attributeName)) {

                        TagField isPii = TagField.newBuilder().setBoolValue((Boolean)value).build();
                        tagBuilder.putFields("is_pii", isPii);
                    }else{
                        System.out.println(" Attribute " + attributeName + " not supported for the tag template "  + templateId);
                    }

                }
                LookupEntryRequest lookupEntryRequest =
                        LookupEntryRequest.newBuilder().setLinkedResource(qualifiedTablePath).build();
                Entry tableEntry = dataCatalogClient.lookupEntry(lookupEntryRequest);

                CreateTagRequest createTagRequest =
                        CreateTagRequest.newBuilder().setParent(tableEntry.getName())
                                //set the column name to make it column level tag.
                                .setTag(tagBuilder.setColumn(columnName).build()).build();

                dataCatalogClient.createTag(createTagRequest);
            }
            //



        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static Map<String, Object> createInnerMap(String infoType, Boolean isPii){

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("INFO_TYPE", infoType);
        innerMap.put("IS_PII", isPii);
        return innerMap;
    }

    public static void main(String[] args) throws Exception{
        String projectId = "data-catalog-examples-321520";
        String dataSetId = "example_dataset";
        String tableName = "customer_vin";
        String templateName = "fordexampletagtemplate";

        String tablePath =
                String.format(
                        "//bigquery.googleapis.com/projects/%s/datasets/%s/tables/%s",
                        projectId, dataSetId, tableName);

        //create a list
        /*
            credit_card_no
                 infoTYpe=ssn, is_pii=true
            VIN
                infoTYpe=VIN, is_pii=False
         */

        Map<String,Map<String,Object>> columnMap = new HashMap<>();
        //credit_card_no, info_type credit_card_no, is_pii fal
        columnMap.put("credit_card_no", createInnerMap("SSN", Boolean.TRUE));
        columnMap.put("VIN", createInnerMap("VIN", Boolean.FALSE));


        populateColumnTempate(projectId, templateName,tablePath, columnMap);



    }
}
