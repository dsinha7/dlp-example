package com.dlp.cli;

public class DownloadInspectionResults {

    public static void main(String[] args) {

        String inspectionResultTable = "data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650";

        String sql = String.format("with falatten_inspection_results as \n" +
                "(SELECT\n" +
                "    locations.record_location.record_key.big_query_key.table_reference.dataset_id as data_set_name,\n" +
                "    locations.record_location.record_key.big_query_key.table_reference.table_id as table_name,\n" +
                "    locations.record_location.field_id.name AS column_name,\n" +
                "    info_type.name as GCP_INFO_TYPE,\n" +
                "    quote as sample\n" +
                "    FROM\n" +
                "    `%s`,\n" +
                "    UNNEST(location.content_locations) AS locations\n" +
                "    WHERE\n" +
                "    (likelihood = 'LIKELY'\n" +
                "    OR likelihood = 'VERY_LIKELY'\n" +
                "    OR likelihood = 'POSSIBLE')\n" +
                ")\n" +
                ", aggregated_data as (\n" +
                "    select data_set_name,table_name, column_name, GCP_INFO_TYPE, count(1) as total_findings, \n" +
                "    STRING_AGG( distinct sample ,\",\" LIMIT %d) as samples\n" +
                "    from falatten_inspection_results\n" +
                "group by data_set_name,table_name, column_name, GCP_INFO_TYPE\n" +
                ")\n" +
                ",results as (\n" +
                "Select data_set_name, table_name, column_name, samples as samples,\n" +
                "STRING_AGG( CONCAT(\" \",GCP_INFO_TYPE ,\" [count: \",CAST(total_findings  AS String),\"]\")\n" +
                "    ORDER BY\n" +
                "    total_findings  DESC) AS INFO_TYPES\n" +
                "from aggregated_data\n" +
                "group by data_set_name, table_name, column_name, samples\n" +
                "order by data_set_name, table_name, column_name\n" +
                ")\n" +
                "select data_set_name, table_name, column_name, INFO_TYPES, samples as examples from results\n",
                inspectionResultTable,
                5 // samples to limit
                );




    }
}
