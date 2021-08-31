#/bin/bash
if [$# -ne 1]; then
    echo "pass table name to be inspected!"
    exit -1
fi
#mvn clean install
table_to_inspect=$1
java -cp target/dlp-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.dlp.cli.RunInspection \
    -dbType "bigquery" \
    -limitMax 1000 \
    -dbName example_dataset \
    -tableName $table_to_inspect \
    -projectId $PROJECT_ID \
    -inspectTemplate projects/$PROJECT_ID/locations/global/inspectTemplates/1153590694604024581 \
    -minThreshold 1
