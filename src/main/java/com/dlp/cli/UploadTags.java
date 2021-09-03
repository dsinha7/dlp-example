package com.dlp.cli;

import com.dlp.bq.DLPHelper;
import com.dlp.bq.DataCatalogHelper;

public class UploadTags {


    public static void main(String[] args) throws Exception{

        java.util.Date startDate = new java.util.Date();
        System.out.println("[Start: " + startDate.toString() + "]");

        UploadTagsArg parseArgs = new UploadTagsArg(args).invoke();
        if (parseArgs.isError()) {
            return;
        }

        DataCatalogHelper.updateColumnTagTemplate(parseArgs.getProjectId()
                ,parseArgs.getTagTemplateName()
                , parseArgs.getBqDatasetName()
                , parseArgs.getTagRegion()
        ,parseArgs.getInputCSVPath());

    }

}
