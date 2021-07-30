package com.dlp.bq;

import com.google.common.base.Throwables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class BQHelper {

    private static Logger logger = Logger.getLogger(BQHelper.class.getName());
    private static final String SQL_FIND_PARTITION_COLUMN_TEMPLATE =
            "SELECT\n" +
                    " table_name, column_Name, is_partitioning_column from\n" +
                    " `%s`.INFORMATION_SCHEMA.COLUMNS\n" +
                    "WHERE\n" +
                    " table_name=\"%s\";";

    private static final String SQL_PARTITION_FILTER_TEMPLATE =
            "WHERE DATE(%s) <= CURRENT_DATE()";

    private static final String SQL_END_REGEX_STRING =
            "(limit \\d+)";

    public static final String PARTITION_COLUMN_YES = "YES";

    public static ResultSet queryTable(String databaseName,
                                        String tableName,
                                        String sqlQuery,
                                        Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
            return stmt.executeQuery(sqlQuery);
        } catch (SQLException exp) {
            // We have to handle query errors, when BigQuery tables require partition filters
            if(Throwables.getRootCause(exp).getMessage().contains("partition")){
                logger.info(
                    "Query failed, table contains partition filter ["
                                    + databaseName
                                    + "]["
                                    + tableName
                                    + "]");

                ResultSet rs = queryWithPartition(
                        databaseName, tableName, sqlQuery, connection);

                if (rs != null){
                    return rs;
                }

                logger.info(
                            "Unable to build partition filter [" + databaseName + "][" + tableName + "]");

            }
            throw exp;
        }
    }
    private static ResultSet queryWithPartition(String databaseName,
                                               String tableName,
                                               String sqlQuery,
                                               Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
                String.format(SQL_FIND_PARTITION_COLUMN_TEMPLATE, databaseName, tableName));

        // Find first partition column
        String partitionColumnName = null;

        while (rs.next()) {
            String columnName = rs.getString(2);
            String isPartitionColumn = rs.getString(3);
            if (PARTITION_COLUMN_YES.equalsIgnoreCase(isPartitionColumn)) {
                partitionColumnName = columnName;
                break;
            }
        }
        return null;
    }


}
