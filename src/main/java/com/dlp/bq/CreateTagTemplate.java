package com.dlp.bq;

import com.google.cloud.datacatalog.v1.*;


public class CreateTagTemplate {



    public static void createTagTemplate(String projectId) {
        // Currently, Data Catalog stores metadata in the us-central1 region.
        String location = "us-central1";

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {

            // -------------------------------
            // Create a Tag Template.
            // -------------------------------
            TagTemplateField sourceField =
                    TagTemplateField.newBuilder()
                            .setDisplayName("Source of data asset")
                            .setType(FieldType.newBuilder().setPrimitiveType(FieldType.PrimitiveType.STRING).build())
                            .build();

            TagTemplateField numRowsField =
                    TagTemplateField.newBuilder()
                            .setDisplayName("Number of rows in data asset")
                            .setType(FieldType.newBuilder().setPrimitiveType(FieldType.PrimitiveType.DOUBLE).build())
                            .build();

            TagTemplateField hasPiiField =
                    TagTemplateField.newBuilder()
                            .setDisplayName("Has PII")
                            .setType(FieldType.newBuilder().setPrimitiveType(FieldType.PrimitiveType.BOOL).build())
                            .build();


            TagTemplate tagTemplate =
                    TagTemplate.newBuilder()
                            .setDisplayName("Demo Tag Template")
                            .putFields("source", sourceField)
                            .putFields("num_rows", numRowsField)
                            .putFields("has_pii", hasPiiField)
                            .build();

            CreateTagTemplateRequest createTagTemplateRequest =
                    CreateTagTemplateRequest.newBuilder()
                            .setParent(
                                    LocationName.newBuilder()
                                            .setProject(projectId)
                                            .setLocation(location)
                                            .build()
                                            .toString())
                            .setTagTemplateId("demo_tag_template")
                            .setTagTemplate(tagTemplate)
                            .build();

            String expectedTemplateName =
                    TagTemplateName.newBuilder()
                            .setProject(projectId)
                            .setLocation(location)
                            .setTagTemplate("demo_tag_template")
                            .build()
                            .toString();
            System.out.println("Expected Template Name => " + expectedTemplateName);
            // Delete any pre-existing Template with the same name.
//            try {
//                dataCatalogClient.deleteTagTemplate(
//                        DeleteTagTemplateRequest.newBuilder()
//                                .setName(expectedTemplateName)
//                                .setForce(true)
//                                .build());
//
//                System.out.println(String.format("Deleted template: %s", expectedTemplateName));
//            } catch (Exception e) {
//                System.out.println(String.format("Cannot delete template: %s", expectedTemplateName));
//            }

            // Create the Tag Template.
            tagTemplate = dataCatalogClient.createTagTemplate(createTagTemplateRequest);
                System.out.println(String.format("Template created with name: %s", tagTemplate.getName()));


        }catch (Exception e) {
        System.out.print("Error during CreateTags:\n" + e.toString());
        e.printStackTrace();
    }
    }

    public static void main(String[] args) {
        createTagTemplate("data-catalog-examples-321520");
    }
}
