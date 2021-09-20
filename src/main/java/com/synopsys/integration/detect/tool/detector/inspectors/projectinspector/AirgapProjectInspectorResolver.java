/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.tool.detector.inspectors.projectinspector;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.workflow.airgap.AirGapInspectorPaths;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.inspector.ProjectInspectorResolver;
import com.synopsys.integration.util.OperatingSystemType;

public class AirgapProjectInspectorResolver implements ProjectInspectorResolver {
    public static final String LINUX_DIR = "linux64";
    public static final String WINDOWS_DIR = "win64";
    public static final String MAC_DIR = "macosx";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AirGapInspectorPaths airGapInspectorPaths;
    private final ProjectInspectorExecutableLocator projectInspectorExecutableLocator;
    private final DetectInfo detectInfo;

    private boolean hasResolvedInspector;
    private ExecutableTarget inspector = null;

    public AirgapProjectInspectorResolver(AirGapInspectorPaths airGapInspectorPaths, ProjectInspectorExecutableLocator projectInspectorExecutableLocator, DetectInfo detectInfo) {
        this.airGapInspectorPaths = airGapInspectorPaths;
        this.projectInspectorExecutableLocator = projectInspectorExecutableLocator;
        this.detectInfo = detectInfo;
    }

    @Override
    public ExecutableTarget resolveProjectInspector() throws DetectableException {
        if (!hasResolvedInspector) {
            hasResolvedInspector = true;
            resolve();
        }
        return inspector;
    }

    private void resolve() throws DetectableException {
        if (airGapInspectorPaths.getProjectInspectorAirGapFile().isPresent()) {
            File airgapPath = airGapInspectorPaths.getProjectInspectorAirGapFile().get();
            File platformPath = new File(airgapPath, determinePlatformDirectory());
            inspector = ExecutableTarget.forFile(projectInspectorExecutableLocator.findExecutable(platformPath));
        } else {
            logger.debug("Could not locate project inspector executable in Air Gap zip.");
            inspector = null;
        }
    }

    private String determinePlatformDirectory() {
        if (detectInfo.getCurrentOs() == OperatingSystemType.WINDOWS) {
            return WINDOWS_DIR;
        } else if (detectInfo.getCurrentOs() == OperatingSystemType.MAC) {
            return MAC_DIR;
        } else {
            return LINUX_DIR;
        }
    }
}
