/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.extraction.bomtool.npm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.strategy.evaluation.StrategyEnvironment;
import com.blackducksoftware.integration.hub.detect.strategy.evaluation.StrategyException;
import com.blackducksoftware.integration.hub.detect.type.ExecutableType;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;
import com.blackducksoftware.integration.hub.detect.util.executable.Executable;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunnerException;

@Component
public class NpmExecutableFinder {
    private final Logger logger = LoggerFactory.getLogger(NpmExecutableFinder.class);

    @Autowired
    public DetectFileManager detectFileManager;

    @Autowired
    public DetectConfiguration detectConfiguration;

    @Autowired
    public ExecutableManager executableManager;

    @Autowired
    public ExecutableRunner executableRunner;

    private String foundNpm = null;
    private boolean hasLookedForNpm = false;

    public String findNpm(final StrategyEnvironment environment) throws StrategyException {
        try {
            if (!hasLookedForNpm) {
                foundNpm = findNpm();
                hasLookedForNpm = true;
            }
            return foundNpm;
        }catch (final Exception e) {
            throw new StrategyException(e);
        }
    }

    String findNpm() {
        final String npm = executableManager.getExecutablePathOrOverride(ExecutableType.NPM, true, detectConfiguration.getSourcePath(), detectConfiguration.getNpmPath());
        if (validateNpm(null, npm)) {
            return npm;
        }
        return null;
    }

    boolean validateNpm(final File directoryToSearch, final String npmExePath) {
        if (StringUtils.isNotBlank(npmExePath)) {
            Executable npmVersionExe = null;
            final List<String> arguments = new ArrayList<>();
            arguments.add("-version");

            String npmNodePath = detectConfiguration.getNpmNodePath();
            if (StringUtils.isNotBlank(npmNodePath)) {
                final int lastSlashIndex = npmNodePath.lastIndexOf("/");
                if (lastSlashIndex >= 0) {
                    npmNodePath = npmNodePath.substring(0, lastSlashIndex);
                }
                final Map<String, String> environmentVariables = new HashMap<>();
                environmentVariables.put("PATH", npmNodePath);

                npmVersionExe = new Executable(directoryToSearch, environmentVariables, npmExePath, arguments);
            } else {
                npmVersionExe = new Executable(directoryToSearch, npmExePath, arguments);
            }
            try {
                final String npmVersion = executableRunner.execute(npmVersionExe).getStandardOutput();
                logger.debug("Npm version " + npmVersion);
                return true;
            } catch (final ExecutableRunnerException e) {
                logger.error("Could not run npm to get the version: " + e.getMessage());
            }
        }
        return false;
    }

}
