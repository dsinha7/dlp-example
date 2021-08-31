with falatten_inspection_results as
(SELECT
    locations.record_location.record_key.big_query_key.table_reference.dataset_id as data_set_name,
    locations.record_location.record_key.big_query_key.table_reference.table_id as table_name,
    locations.record_location.field_id.name AS column_name,
    info_type.name as GCP_INFO_TYPE,
    quote as sample
    FROM
    `data-catalog-examples-321520.example_dataset.dlp_googleapis_2021_08_04_5872696567483983650`,
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
,results as (
Select data_set_name, table_name, column_name, samples as samples,
STRING_AGG( CONCAT(" ",GCP_INFO_TYPE ," [count: ",CAST(total_findings  AS String),"]")
    ORDER BY
    total_findings  DESC) AS INFO_TYPES
from aggregated_data
group by data_set_name, table_name, column_name, samples
order by data_set_name, table_name, column_name
)
select data_set_name, table_name, column_name, INFO_TYPES, samples as examples from results
