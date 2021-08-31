package com.dlp.cli;

import com.dlp.bq.DLPHelper;
import com.dlp.cli.RunDLPJobArg;
import com.dlp.domain.DLPTableInput;
import com.dlp.domain.InspectionFinding;
import com.google.privacy.dlp.v2.Likelihood;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RunInspection {

    public static void main(String[] args) throws Exception{

        java.util.Date startDate = new java.util.Date();
        System.out.println("[Start: " + startDate.toString() + "]");

        RunDLPJobArg parseArgs = new RunDLPJobArg(args).invoke();
        if (parseArgs.error()) {
            return;
        }

        //inspectInlineAPI(parseArgs);
        String storageTableName= "dlp_googleapis_2021_08_04_5872696567483983650";

        DLPHelper.submitInspectionJob(parseArgs.getProjectId(), parseArgs.getDbName(), parseArgs.getInspectTemplate(), parseArgs.getTableName(), storageTableName);


    }

    private static void inspectInlineAPI(RunDLPJobArg parseArgs) throws IOException {
        DLPTableInput bqTableData = DLPHelper.bqToDLPTable(parseArgs.getDbName(),
                parseArgs.getTableName(),
                parseArgs.getLimitMax(),
                parseArgs.getProjectId());


        List<InspectionFinding> findings = DLPHelper.inspect(parseArgs.getProjectId(), parseArgs.getInspectTemplate(), bqTableData);
        Map<String, Map<String, Map<Likelihood,Integer>>> groupedFindingsByColumns = DLPHelper.groupFindingByColumns(findings);

        System.out.println("Inspection finding " + findings);
        System.out.println("\n\n\n\n");
        System.out.println("Inspection finding " + groupedFindingsByColumns);
    }
}
