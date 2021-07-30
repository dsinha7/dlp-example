package com.dlp.bq;

import com.dlp.domain.DLPTableInput;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.Value;
import com.google.type.Date;
import com.simba.googlebigquery.jdbc42.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLPHelper {

    private static final Logger logger = LoggerFactory.getLogger(DLPHelper.class);

    public static DLPTableInput BQToDLPTable(String databaseName,
                                             String tableName,
                                             String limitMax,
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
                            "`" + " limit " + limitMax;

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
}
