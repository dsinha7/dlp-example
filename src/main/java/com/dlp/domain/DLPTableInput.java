package com.dlp.domain;

import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.Table;

import java.util.List;

public class DLPTableInput {

    private final List<FieldId> header;
    private final List<Table.Row> rows;

    public DLPTableInput(List<FieldId> header, List<Table.Row> rows) {
        this.header = header;
        this.rows = rows;
    }
}
