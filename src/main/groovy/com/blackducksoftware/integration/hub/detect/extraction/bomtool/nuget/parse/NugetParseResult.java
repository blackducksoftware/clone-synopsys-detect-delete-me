package com.blackducksoftware.integration.hub.detect.extraction.bomtool.nuget.parse;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.detect.model.BomDetectCodeLocation;

public class NugetParseResult {
    public String projectName;
    public String projectVersion;
    public List<BomDetectCodeLocation> codeLocations;

    public NugetParseResult(final String projectName, final String projectVersion, final List<BomDetectCodeLocation> codeLocations) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.codeLocations = codeLocations;
    }

    public NugetParseResult(final String projectName, final String projectVersion, final BomDetectCodeLocation codeLocation) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.codeLocations = new ArrayList<>();
        this.codeLocations.add(codeLocation);
    }
}
