package com.synopsys.integration.detect.workflow.bdio;

public class BdioOptions {
    private final boolean enabledBdio2;
    private final String projectCodeLocationSuffix;
    private final String projectCodeLocationPrefix;

    public BdioOptions(boolean enabledBdio2, String projectCodeLocationPrefix, String projectCodeLocationSuffix) {
        this.enabledBdio2 = enabledBdio2;
        this.projectCodeLocationSuffix = projectCodeLocationSuffix;
        this.projectCodeLocationPrefix = projectCodeLocationPrefix;
    }

    public String getProjectCodeLocationSuffix() {
        return projectCodeLocationSuffix;
    }

    public String getProjectCodeLocationPrefix() {
        return projectCodeLocationPrefix;
    }

    public boolean isBdio2Enabled() {
        return enabledBdio2;
    }
    
}
