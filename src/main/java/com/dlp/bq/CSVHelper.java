package com.dlp.bq;

import com.dlp.domain.ColumnTag;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSVHelper {

    private List<String> headers;
    private List<ColumnTag> rows;

    public List<String> getHeaders() {
        return headers;
    }

    public List<ColumnTag> getRows() {
        return rows;
    }

    private static ColumnTag parseLineAsColumnTag(String line) {
        //customer id,name,email ,org id
        String[] values = line.split(",");

        //TODO:: validate
        //csv format
        // tablename,columnname,riskCategory,Data Domain,Sensitivity,should_encrypt
        return new ColumnTag(values[0], values[1], values[2], values[3], values[4], Boolean.parseBoolean(values[5]));
    }

    public void readCsv(String inputCsvFile) throws IOException {

        try(BufferedReader input = Files.newBufferedReader(Paths.get(inputCsvFile))){

            headers = Arrays.stream(input.readLine().split(","))
                                        .collect(Collectors.toList());

            rows = input.lines()
                    .map(CSVHelper::parseLineAsColumnTag)
                    .collect(Collectors.toList());
        }
    }




}
