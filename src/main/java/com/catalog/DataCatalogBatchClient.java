package com.catalog;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.datacatalog.v1.DataCatalogSettings;
import com.google.cloud.datacatalog.v1.Datacatalog;;


import java.util.Collections;

public class DataCatalogBatchClient {
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final NetHttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();
    private static final String SCOPES = "https://www.googleapis.com/auth/cloud-platform";

    //private static Datacatalog datacatalogClient = createDataCatalogClient();
    private static Datacatalog createDataCatalogClient(){
        String url = "https://us-datacatalog.googleapis.com";
        DataCatalogSettings.newBuilder();
//        return new   NET_HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
//                .setApplicationName("DataCatalogBatchClient")
//                .setRootUrl(url)
//                .build();
        return null;
    }
    private static GoogleCredentials generateCredential() {
        try {
            // Credentials could be downloaded after creating service account
            // set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable, for example:
            // export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/key.json
            //return GoogleCredential.getApplicationDefault(NET_HTTP_TRANSPORT, JSON_FACTORY)
                    //.createScoped(Collections.singleton(SCOPES));
            return ServiceAccountCredentials.getApplicationDefault();

        } catch (Exception e) {
            System.out.print("Error in generating credential");
            throw new RuntimeException(e);
        }
    }



}
