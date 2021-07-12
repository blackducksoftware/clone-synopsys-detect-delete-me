/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.detect.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import com.synopsys.integration.detect.Application;
import com.synopsys.integration.detect.battery.docker.integration.BlackDuckAssertions;
import com.synopsys.integration.detect.battery.docker.integration.BlackDuckTestConnection;
import com.synopsys.integration.detect.battery.docker.util.DetectCommandBuilder;
import com.synopsys.integration.detect.configuration.DetectProperties;

@Tag("integration")
public class SignatureScanTest {
    private static final long HALF_MILLION_BYTES = 500_000;

    @Test
    @ExtendWith(TempDirectory.class)
    public void testOfflineScanWithSnippetMatching(@TempDirectory.TempDir final Path tempOutputDirectory) throws Exception {
        final String projectName = "synopsys-detect-junit";
        final String projectVersionName = "offline-scan";
        BlackDuckTestConnection blackDuckTestConnection = BlackDuckTestConnection.fromEnvironment();
        BlackDuckAssertions blackDuckAssertions = blackDuckTestConnection.projectVersionAssertions(projectName, projectVersionName);

        blackDuckAssertions.emptyOnBlackDuck();

        DetectCommandBuilder detectCommandBuilder = new DetectCommandBuilder();
        detectCommandBuilder.projectNameVersion(blackDuckAssertions.getProjectNameVersion());
        detectCommandBuilder.connectToBlackDuck(blackDuckTestConnection);
        detectCommandBuilder.property(DetectProperties.DETECT_OUTPUT_PATH, tempOutputDirectory.toString());
        detectCommandBuilder.property(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_SNIPPET_MATCHING, "SNIPPET_MATCHING");
        detectCommandBuilder.property(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN, "true");

        Application.setShouldExit(false);
        Application.main(detectCommandBuilder.buildArguments());

        assertDirectoryStructureForOfflineScan(tempOutputDirectory);
    }

    private void assertDirectoryStructureForOfflineScan(@TempDirectory.TempDir final Path tempOutputDirectory) {
        final Path runsPath = tempOutputDirectory.resolve("runs");
        assertTrue(runsPath.toFile().exists());
        assertTrue(runsPath.toFile().isDirectory());

        final File[] runDirectories = runsPath.toFile().listFiles();

        assertNotNull(runDirectories);
        assertEquals(1, runDirectories.length);

        final File runDirectory = runDirectories[0];
        assertTrue(runDirectory.exists());
        assertTrue(runDirectory.isDirectory());

        final File scanDirectory = new File(runDirectory, "scan");
        assertTrue(scanDirectory.exists());
        assertTrue(scanDirectory.isDirectory());

        final File blackDuckScanOutput = new File(scanDirectory, "BlackDuckScanOutput");
        assertTrue(blackDuckScanOutput.exists());
        assertTrue(blackDuckScanOutput.isDirectory());

        final File[] outputDirectories = blackDuckScanOutput.listFiles();
        assertNotNull(outputDirectories);
        assertEquals(1, outputDirectories.length);

        final File outputDirectory = outputDirectories[0];
        assertTrue(outputDirectory.exists());
        assertTrue(outputDirectory.isDirectory());

        final File dataDirectory = new File(outputDirectory, "data");
        assertTrue(dataDirectory.exists());
        assertTrue(dataDirectory.isDirectory());

        final File[] dataFiles = dataDirectory.listFiles();
        assertNotNull(dataFiles);
        assertEquals(1, dataFiles.length);
        assertTrue(dataFiles[0].length() > HALF_MILLION_BYTES);
    }

}
