### Set the gcloud etc 
gcloud config set project myProject
gcloud config set compute/zone us-east1-a
gcloud auth list 

### Build the code 
    mvn clean package -DskipTests
    
    java -cp target/dlp-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.dlp.cli.RunInspection \
    -dbType "bigquery" \
    -limitMax 1000 \
    -dbName demo_dataset \
    -tableName employee2 \
    -projectId data-catalog-examples \
    -threadPoolSize 5 \
    -inspectTemplate projects/data-catalog-examples/locations/global/inspectTemplates/1951959072704471819 \
    -minThreshold 1


### Create Service Account & Credential 
create a service account "dlp_catalog_bq_admin" with BQAdmin, DLPAdmin, DataCatelogAdmin

    export PROJECT_ID=data-catalog-examples-321520
    
    gcloud iam service-accounts keys create dlp-catalog-bq-admin.json \
    --iam-account=dlp-catalog-bq-admin@data-catalog-examples-321520.iam.gserviceaccount.com

### export the credential file this is required for API (e.g java to work ) 
    export GOOGLE_APPLICATION_CREDENTIALS=dlp-catalog-bq-admin.json

### activate service account this is required for REST API to work
    
    gcloud auth activate-service-account --key-file [KEY_FILE]
    gcloud auth print-access-token

    
### Create Inspection Template with POST 
    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer '[access-token]' \
    -d @templates/inspection_template.json \
    https://dlp.googleapis.com/v2/projects/[PROJECT_ID]/inspectTemplates
    
### Create & Trigger Inspection JOB  

    export PROJECT_ID=data-catalog-examples-321520
    export GOOGLE_APPLICATION_CREDENTIALS=dlp-catalog-bq-admin.json
    

    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer $API_KEY' \
    -d @templates/inspection_template.json \
    https://dlp.googleapis.com/v2/projects/data-catalog-examples-321520/inspectTemplates


    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer $API_KEY' \
    -d @templates/test_dlp_regex.json \
    https://dlp.googleapis.com/v2/projects/data-catalog-examples-321520/content:inspect

    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer $API_KEY' \
    -d @templates/inspection_job.json \
    https://dlp.googleapis.com/v2/projects/data-catalog-examples-321520/dlpJobs
    
    curl -X POST -H "Content-Type: application/json" \
    -H 'Authorization: Bearer  ya29.c.KqYBCwiu6Zmj6-kQSt6NDNK6PLnMTW9ZM5FlJYD3sNLBOgHVj1W5PeGZZgK6n28yKf_NoCwjnq63cvChVfNg4n5wQPP1gxclb2ttJ9eXgMYJtCQqrLSj_uBFvYgXioDTEvjOULSLaxG38mUfjegmDzU7KbpgO1hMWe5agAI_cMlt2S80ENz5SpjrbliCsj6gmyUQFPOTHKHmm5TOq7VXYG9bQeWrbrL-nw' \
    -d @templates/meta_data_ispection.json \
    https://dlp.googleapis.com/v2/projects/data-catalog-examples-321520/content:inspect
    


#### SQL queries 
    SELECT count(1), info_type.name
    FROM `data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650`
    group by info_type.name
    
    
    SELECT info_type.name, cast(TIMESTAMP_SECONDS(create_time.seconds) as date) as day,
    COUNT(locations.container_name) AS count
    FROM `data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650`,
    UNNEST(location.content_locations) AS locations
    GROUP BY info_type.name, day
    ORDER BY count DESC;
    
    
    
    SELECT
    table_counts.field_name,
    STRING_AGG( CONCAT(" ",table_counts.name," [count: ",CAST(table_counts.count_total AS String),"]")
    ORDER BY
    table_counts.count_total DESC) AS infoTypes
    FROM (
    SELECT
    locations.record_location.field_id.name AS field_name,
    locations.record_location.record_key.big_query_key.table_reference.table_id as table_name,
    info_type.name, count(*) as count_total
    FROM
    `data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650`,
    UNNEST(location.content_locations) AS locations
    WHERE
    (likelihood = 'LIKELY'
    OR likelihood = 'VERY_LIKELY'
    OR likelihood = 'POSSIBLE')
    GROUP BY
    locations.record_location.field_id.name,locations.record_location.record_key.big_query_key.table_reference.table_id,
    info_type.name
    HAVING
    count_total>200 ) AS table_counts
    GROUP BY
    table_counts.field_name
    ORDER BY
    table_counts.field_name
    
    SELECT
    locations.record_location.field_id.name AS field_name,
    locations.record_location.record_key.big_query_key.table_reference.table_id as table_name,
    info_type.name, count(*) as count_total
    FROM
    `data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650`,
    UNNEST(location.content_locations) AS locations
    WHERE
    (likelihood = 'LIKELY'
    OR likelihood = 'VERY_LIKELY'
    OR likelihood = 'POSSIBLE')
    and locations.record_location.record_key.big_query_key.table_reference.table_id = 'customer_ip'

java -cp target/dlp-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.dlp.bq.DataCatalogHelper
java -cp target/dlp-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.dlp.bq.CreateTagTemplate