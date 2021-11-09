/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.pip.inspector;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.detectable.util.ToolVersionLogger;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.pip.PythonProjectInfoResolver;
import com.synopsys.integration.detectable.detectables.pip.inspector.model.NameVersionCodeLocation;
import com.synopsys.integration.detectable.detectables.pip.inspector.parser.PipInspectorTreeParser;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.executable.ExecutableRunnerException;

public class PipInspectorExtractor {
    private final DetectableExecutableRunner executableRunner;
    private final PipInspectorTreeParser pipInspectorTreeParser;
    private final PythonProjectInfoResolver pythonProjectInfoResolver;
    private final ToolVersionLogger toolVersionLogger;

    public PipInspectorExtractor(DetectableExecutableRunner executableRunner, PipInspectorTreeParser pipInspectorTreeParser, PythonProjectInfoResolver pythonProjectInfoResolver, ToolVersionLogger toolVersionLogger) {
        this.executableRunner = executableRunner;
        this.pipInspectorTreeParser = pipInspectorTreeParser;
        this.pythonProjectInfoResolver = pythonProjectInfoResolver;
        this.toolVersionLogger = toolVersionLogger;
    }

    public Extraction extract(File directory, ExecutableTarget pythonExe, ExecutableTarget pipExe, File pipInspector, File setupFile, List<Path> requirementFilePaths, String providedProjectName) {
        toolVersionLogger.log(directory, pythonExe);
        toolVersionLogger.log(directory, pipExe);
        Extraction extractionResult;
        try {
            String projectName = pythonProjectInfoResolver.resolveProjectName(directory, pythonExe, setupFile, providedProjectName);
            List<CodeLocation> codeLocations = new ArrayList<>();
            String projectVersion = null;

            List<Path> requirementsPaths = new ArrayList<>();

            if (requirementFilePaths.isEmpty()) {
                requirementsPaths.add(null);
            } else {
                requirementsPaths.addAll(requirementFilePaths);
            }

            for (Path requirementFilePath : requirementsPaths) {
                List<String> inspectorOutput = runInspector(directory, pythonExe, pipInspector, projectName, requirementFilePath);
                Optional<NameVersionCodeLocation> result = pipInspectorTreeParser.parse(inspectorOutput, directory.toString());
                if (result.isPresent()) {
                    codeLocations.add(result.get().getCodeLocation());
                    String potentialProjectVersion = result.get().getProjectVersion();
                    if (projectVersion == null && StringUtils.isNotBlank(potentialProjectVersion)) {
                        projectVersion = potentialProjectVersion;
                    }
                }
            }

            if (codeLocations.isEmpty()) {
                extractionResult = new Extraction.Builder().failure("The Pip Inspector tree parse failed to produce output.").build();
            } else {
                extractionResult = new Extraction.Builder()
                    .success(codeLocations)
                    .projectName(projectName)
                    .projectVersion(projectVersion)
                    .build();
            }
        } catch (Exception e) {
            extractionResult = new Extraction.Builder().exception(e).build();
        }

        return extractionResult;
    }

    private List<String> runInspector(File sourceDirectory, ExecutableTarget pythonExe, File inspectorScript, String projectName, Path requirementsFilePath) throws ExecutableRunnerException {
        List<String> inspectorArguments = new ArrayList<>();
        inspectorArguments.add(inspectorScript.getAbsolutePath());

        if (requirementsFilePath != null) {
            inspectorArguments.add(String.format("--requirements=%s", requirementsFilePath.toAbsolutePath().toString()));
        }

        if (StringUtils.isNotBlank(projectName)) {
            inspectorArguments.add(String.format("--projectname=%s", projectName));
        }

        return executableRunner.execute(ExecutableUtils.createFromTarget(sourceDirectory, pythonExe, inspectorArguments)).getStandardOutputAsList();
    }

}
