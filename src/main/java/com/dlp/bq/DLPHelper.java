package com.dlp.bq;

import com.dlp.domain.DLPTableInput;
import com.dlp.domain.InspectionFinding;
import com.google.api.services.bigquery.model.TableReference;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.*;
import com.google.type.Date;
import com.simba.googlebigquery.jdbc42.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLPHelper {

    private static final Logger logger = LoggerFactory.getLogger(DLPHelper.class);

    public static DLPTableInput bqToDLPTable(String databaseName,
                                             String tableName,
                                             String rowLimitMax,
                                             String projectId){

        DataSource ds2 = new DataSource();
        Connection connection = null;
        String url =
                String.format(
                        "jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=3;ProjectId=%s;",
                        projectId);
        ds2.setURL(url);
        List<FieldId> headers = new ArrayList<>();
        List<Table.Row> rows = new ArrayList<>();
        try{
            connection = ds2.getConnection();

            String sqlQuery =
                    // ADD ` to escape reserved keywords.
                    "SELECT * from " + "`" + databaseName + "`" + "." + "`" + tableName +
                            "`" + " limit " + rowLimitMax;

            ResultSet rs = BQHelper.queryTable(databaseName, tableName, sqlQuery, connection);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            String headerNames = "";

            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) {
                    headerNames = headerNames + "," + rsmd.getColumnName(i);
                } else {
                    headerNames = rsmd.getColumnName(i);
                }
            }
            headers =
                    Arrays.stream(headerNames.split(","))
                            .map(header -> FieldId.newBuilder().setName(header).build())
                            .collect(Collectors.toList());


            while (rs.next()) {
                String rowStr = "";
                for (int i = 1; i <= columnsNumber; i++) {
                    String theValue = rs.getString(i);
                    if (theValue == null) {
                        theValue = "";
                    }
                    if (i > 1) {
                        rowStr = rowStr + "," + theValue.replace(",", "-");
                    } else {
                        rowStr = theValue.replace(",", "-");
                    }
                }
                rows.add(convertCsvRowToTableRow(rowStr));
            }

            connection.close();

        }catch(Exception r){
            logger.error("", r);
        }finally {

        }

    return new DLPTableInput(headers,rows);
    }
    // Parse string to valid date, return null when invalid
    private static LocalDate getValidDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    private static Table.Row convertCsvRowToTableRow(String row) {
        // Complex split that allows quoted commas
        String[] values = row.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        Table.Row.Builder tableRowBuilder = Table.Row.newBuilder();
        int i = 0;
        for (String value : values) {
            i++;
            LocalDate date = getValidDate(value);
            if (date != null) {
                // convert to com.google.type.Date
                Date dateValue =
                        Date.newBuilder()
                                .setYear(date.getYear())
                                .setMonth(date.getMonthValue())
                                .setDay(date.getDayOfMonth())
                                .build();
                Value tableValue = Value.newBuilder().setDateValue(dateValue).build();
                tableRowBuilder.addValues(tableValue);
            } else {
                tableRowBuilder.addValues(Value.newBuilder().setStringValue(value).build());
            }
        }
        return tableRowBuilder.build();
    }

    public static List<InspectionFinding> inspect(
            String projectId, String inspectTemplate,DLPTableInput headerAndRows) throws IOException {

        DlpServiceClient dlpServiceClient = DlpServiceClient.create();
        // Inspect the table for info types
        //list of findings as custom class
        List<InspectionFinding> findings = new ArrayList<>();
        Table table = Table.newBuilder()
                .addAllHeaders(headerAndRows.getHeader())
                .addAllRows(headerAndRows.getRows())
                .build();

        ContentItem tableItem = ContentItem.newBuilder().setTable(table).build();

        InspectContentRequest request =
                InspectContentRequest.newBuilder()
                        .setParent(LocationName.of(projectId, "global").toString())
                        .setInspectTemplateName(inspectTemplate)
                        .setItem(tableItem)
                        .build();
        try {
            InspectContentResponse response = dlpServiceClient.inspectContent(request);
            response.getResult().getFindingsList().forEach( s -> {
                InspectionFinding finding = new InspectionFinding(
                        s.getLocation().getContentLocationsList().get(0).getRecordLocation().getFieldId().getName()
                        ,s.getInfoType().getName()
                        ,s.getLikelihood()
                        ,s.getQuote()
                );
                findings.add(finding);
            });


        } catch (Exception e2) {
            throw e2;
        }

        return findings;
    }

    /**
     * Groups the inspection findings returned by DLP by columns, info types and likelihood
     * @param findings
     * @return
     */
    public static Map<String, Map<String, Map<Likelihood,Integer>>> groupFindingByColumns(List<InspectionFinding> findings){
        return findings.stream().collect(
                Collectors.groupingBy(InspectionFinding::getColumnName,
                        Collectors.groupingBy(InspectionFinding::getInfoType,
                                Collectors.groupingBy(InspectionFinding::getLikelyHood,
                                        Collectors.summingInt(s->1)))));
    }

    public static void submitInspectionJob(
            String projectId, String storageTableDataSetId, String inspectTemplate,String tableName, String storageTableName) throws IOException {

        try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
            // Specify the action that is triggered when the job completes.
            OutputStorageConfig outputStorageConfig =
                    OutputStorageConfig.newBuilder()
                            .setTable(BigQueryTable
                                    .newBuilder()
                                    .setProjectId(projectId)
                                    .setDatasetId(storageTableDataSetId)
                                    .setTableId(storageTableName)
                                    .build()
                            ).build();

            Action action = Action.newBuilder().setSaveFindings(Action.SaveFindings.newBuilder().setOutputConfig(outputStorageConfig).build()).build();
            //.setPublishSummaryToCscc(publishSummaryToCscc).build();
            BigQueryTable bqTable =
                    BigQueryTable.newBuilder()
                            .setProjectId(projectId)
                            .setDatasetId(storageTableDataSetId)
                            .setTableId(tableName).build();

            //Note these are all DLP wrapper classes
            BigQueryOptions bigQueryOption =
                    BigQueryOptions.newBuilder()
                            .setTableReference(bqTable)
                            .setSampleMethod(BigQueryOptions.SampleMethod.TOP)
                            .setRowsLimitPercent(50).build();

            InspectJobConfig inspectJobConfig =
                    InspectJobConfig.newBuilder()
                            .setInspectTemplateName(inspectTemplate)
                            .setStorageConfig(StorageConfig.newBuilder().setBigQueryOptions(bigQueryOption))
                            .addActions(action)
                            .build();

            // Construct the job creation request to be sent by the client.
            CreateDlpJobRequest createDlpJobRequest =
                    CreateDlpJobRequest.newBuilder()
                            .setParent(LocationName.of(projectId, "global").toString())
                            .setInspectJob(inspectJobConfig)
                            .build();

            // Send the job creation request and process the response.
            DlpJob createdDlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);

            System.out.println("Job created successfully: " + createdDlpJob.getName());

        }
    }


}
