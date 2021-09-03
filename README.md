## Overview 

This project includes various GCP DLP and Data Catalog examples 

### Install Simba Drive

You will need this drive for code to compile the code and want to use RunInspection.inspectInlineAPI method.

    curl -o SimbaJDBCDriverforGoogleBigQuery.zip https://storage.googleapis.com/simba-bq-release/jdbc/SimbaJDBCDriverforGoogleBigQuery42_1.2.2.1004.zip && \
    unzip -qu SimbaJDBCDriverforGoogleBigQuery.zip -d lib && \
    mvn install:install-file  \
    -Dfile=lib/GoogleBigQueryJDBC42.jar \
    -DgroupId=com.simba \
    -DartifactId=simba-jdbc \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true && \
    rm -rf lib


### Set the gcloud etc. 
Before starting make sure you have gcloud installed and configured
    
    gcloud config set project myProject
    gcloud config set compute/zone us-east1-a
    gcloud auth list
    gcloud auth login 


### Create Service Account & Credential

create a service account "dlp_catalog_bq_admin" with BQAdmin, DLPAdmin, DataCatelogAdmin roles
and create service account key.

    export PROJECT_ID=data-catalog-examples-321520
    
    gcloud iam service-accounts keys create dlp-catalog-bq-admin.json \
    --iam-account=dlp-catalog-bq-admin@$PROJECT_ID.iam.gserviceaccount.com

Export the credential file this is required for API (e.g java to work ) 

    export GOOGLE_APPLICATION_CREDENTIALS=dlp-catalog-bq-admin.json

### activate service account this is required for REST API to work
    
    gcloud auth activate-service-account --key-file [KEY_FILE]
    gcloud auth print-access-token

###GCP REST/POST API's to achieve common DLP inspect tasks
Below section shows how to using GCP REST/POST API's to achieve common  DLP inspect tasks

#### activate service account this is required for REST API to work

    gcloud auth activate-service-account --key-file [KEY_FILE]
    gcloud auth print-access-token
Above will print a key in terminal, copy that as use in below commends

#### Create Inspection Template with POST 

    export $API_KEY=<API_KEY From last step> 
    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer $API_KEY' \
    -d @templates/inspection_template.json \
    https://dlp.googleapis.com/v2/projects/[PROJECT_ID]/inspectTemplates
    
#### Create & Trigger Inspection JOB  
    
   
    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer  ya29.c.KqYBCwiu6Zmj6-kQSt6NDNK6PLnMTW9ZM5FlJYD3sNLBOgHVj1W5PeGZZgK6n28yKf_NoCwjnq63cvChVfNg4n5wQPP1gxclb2ttJ9eXgMYJtCQqrLSj_uBFvYgXioDTEvjOULSLaxG38mUfjegmDzU7KbpgO1hMWe5agAI_cMlt2S80ENz5SpjrbliCsj6gmyUQFPOTHKHmm5TOq7VXYG9bQeWrbrL-nw' \
    -d @templates/meta_data_ispection.json \
    https://dlp.googleapis.com/v2/projects/data-catalog-examples-321520/content:inspect

#### Inspect inline 
    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer $API_KEY' \
    -d @templates/test_dlp_regex.json \
    https://dlp.googleapis.com/v2/projects/data-catalog-examples-321520/content:inspect


### Build and Run CLI code 

#### BUild 
    mvn clean package -DskipTests
#### Run inspection job 
 Below java code runs the inspection job

    java -cp target/dlp-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.dlp.cli.RunInspection \
    -dbType "bigquery" \
    -limitMax 1000 \
    -dbName demo_dataset \
    -tableName employee2 \
    -projectId data-catalog-examples \
    -threadPoolSize 5 \
    -inspectTemplate projects/data-catalog-examples/locations/global/inspectTemplates/1951959072704471819 \
    -minThreshold 1


#### SQL queries to get result out of BQ inspection result table 

    with falatten_inspection_results as
    (SELECT
    locations.record_location.record_key.big_query_key.table_reference.dataset_id as data_set_name,
    locations.record_location.record_key.big_query_key.table_reference.table_id as table_name,
    locations.record_location.field_id.name AS column_name,
    info_type.name as GCP_INFO_TYPE,
    quote as sample
    FROM
    data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650,
    UNNEST(location.content_locations) AS locations
    WHERE
    (likelihood = 'LIKELY'
    OR likelihood = 'VERY_LIKELY'
    OR likelihood = 'POSSIBLE')
    )
    , aggregated_data as (
    select data_set_name,table_name, column_name, GCP_INFO_TYPE, count(1) as total_findings,
    STRING_AGG( distinct sample ,"," LIMIT 5) as samples
    from falatten_inspection_results
    group by data_set_name,table_name, column_name, GCP_INFO_TYPE
    )
    select data_set_name, table_name, column_name, GCP_INFO_TYPE, samples as examples from aggregated_data

#### Upload Tag Template CLI

This CLI takes input in csv file format and attaches the tags 
see the sample_tag_data.csv file for example.


java -cp target/dlp-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.dlp.cli.UploadTags \
-projectId data-catalog-examples-321520 \
-tagRegion us-central1 \
-bqDatasetName example_dataset \
-tagTemplateName fordexampletagtemplate \
-inputCSVPath sample_tag_data.csv

