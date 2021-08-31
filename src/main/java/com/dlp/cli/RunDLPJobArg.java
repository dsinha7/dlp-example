package com.dlp.cli;

import com.google.cloud.ServiceOptions;
import org.apache.commons.cli.*;

public class RunDLPJobArg {
    private boolean error;
    private String[] args;
    private CommandLine cmd;
    private String minThreshold;
    private String projectId;
    private String dbName;
    private String tableName;
    private String datasourceType;
    private String limitMax;
    private String inspectTemplate;

    public RunDLPJobArg(String... args) {
        this.args = args;
    }
    boolean error() {
        return error;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public String getMinThreshold() {
        return minThreshold;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDbName() {
        return dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public String getLimitMax() {
        return limitMax;
    }

    public String getInspectTemplate() {
        return inspectTemplate;
    }

    public RunDLPJobArg invoke() {
        OptionGroup optionsGroup = new OptionGroup();
        optionsGroup.setRequired(true);
        Option dbTypeOption = new Option("dbType", "dbType", true, "Database Type");
        optionsGroup.addOption(dbTypeOption);

        Options commandLineOptions = new Options();
        commandLineOptions.addOptionGroup(optionsGroup);

        Option minLikelihoodOption =
                Option.builder("minLikelihood").hasArg(true).required(false).build();

        commandLineOptions.addOption(minLikelihoodOption);

        Option dbNameOption = Option.builder("dbName").hasArg(true).required(false).build();
        commandLineOptions.addOption(dbNameOption);

        Option tableNameOption = Option.builder("tableName").hasArg(true).required(false).build();
        commandLineOptions.addOption(tableNameOption);

        Option limitMaxOption = Option.builder("limitMax").hasArg(true).required(false).build();
        commandLineOptions.addOption(limitMaxOption);

        Option inspectTemplateOption =
                Option.builder("inspectTemplate").hasArg(true).required(false).build();
        commandLineOptions.addOption(inspectTemplateOption);

        Option threadPoolSizeOption =
                Option.builder("threadPoolSize").hasArg(true).required(false).build();
        commandLineOptions.addOption(threadPoolSizeOption);

        Option projectIdOption = Option.builder("projectId").hasArg(true).required(false).build();
        commandLineOptions.addOption(projectIdOption);

        Option minThresholdOption = Option.builder("minThreshold").hasArg(true).required(false).build();
        commandLineOptions.addOption(minThresholdOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(commandLineOptions, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(RunInspection.class.getName(), commandLineOptions);
            System.exit(1);
            error = true;
            return this;
        }

        minThreshold = cmd.getOptionValue(minThresholdOption.getOpt(), "10");

        projectId = cmd
                .getOptionValue(projectIdOption.getOpt(), ServiceOptions.getDefaultProjectId());

        dbName = cmd.getOptionValue(dbNameOption.getOpt());
        tableName = cmd.getOptionValue(tableNameOption.getOpt());
        datasourceType = cmd.getOptionValue(dbTypeOption.getOpt());
        limitMax = cmd.getOptionValue(limitMaxOption.getOpt());
        inspectTemplate = cmd.getOptionValue(inspectTemplateOption.getOpt());
        error = false;
        return this;
    }


}
