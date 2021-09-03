package com.dlp.cli;

import com.google.cloud.ServiceOptions;
import org.apache.commons.cli.*;

import java.util.Arrays;

public class UploadTagsArg {
    private String[] args;
    private boolean error;
    private CommandLine cmd;
    private String projectId;
    private String tagRegion;
    private String bqDatasetName;
    private String tagTemplateName;
    private String inputCSVPath;

    public UploadTagsArg(String... args) {
        this.args = args;
    }

    public boolean isError() {
        return error;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTagRegion() {
        return tagRegion;
    }

    public String getBqDatasetName() {
        return bqDatasetName;
    }

    public String getTagTemplateName() {
        return tagTemplateName;
    }

    public String getInputCSVPath() {
        return inputCSVPath;
    }

    public UploadTagsArg invoke() {

        Options commandLineOptions = new Options();

        Option projectOption =
                Option.builder("projectId").hasArg(true).required(true).build();
        commandLineOptions.addOption(projectOption);
        Option tagRegionOption = Option.builder("tagRegion").hasArg(true).required(false).build();
        commandLineOptions.addOption(tagRegionOption);

        Option bqDatasetNameOption = Option.builder("bqDatasetName").hasArg(true).required(true).build();
        commandLineOptions.addOption(bqDatasetNameOption);
        Option tagTemplateNameOption = Option.builder("tagTemplateName").hasArg(true).required(true).build();
        commandLineOptions.addOption(tagTemplateNameOption);
        Option inputCSVPathOption = Option.builder("inputCSVPath").hasArg(true).required(true).build();
        commandLineOptions.addOption(inputCSVPathOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(commandLineOptions, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(RunInspection.class.getName(), commandLineOptions);
            System.exit(1);

            return this;
        }

        projectId = cmd
                .getOptionValue(projectOption.getOpt(), ServiceOptions.getDefaultProjectId());

        tagRegion = cmd.getOptionValue(tagRegionOption.getOpt(), "us-central1");
        bqDatasetName = cmd.getOptionValue(bqDatasetNameOption.getOpt());
        tagTemplateName = cmd.getOptionValue(tagTemplateNameOption.getOpt());
        inputCSVPath = cmd.getOptionValue(inputCSVPathOption.getOpt());
        error = false;
        return this;
    }

    @Override
    public String toString() {
        return "UploadTagsArg{" +
                "args=" + Arrays.toString(args) +
                ", cmd=" + cmd +
                ", projectId='" + projectId + '\'' +
                ", tagRegion='" + tagRegion + '\'' +
                ", bqDatasetName='" + bqDatasetName + '\'' +
                ", tagTemplateName='" + tagTemplateName + '\'' +
                ", inputCSVPath='" + inputCSVPath + '\'' +
                '}';
    }
}
