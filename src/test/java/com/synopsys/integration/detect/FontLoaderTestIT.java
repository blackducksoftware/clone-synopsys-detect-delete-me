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
package com.synopsys.integration.detect;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.configuration.property.types.path.SimplePathResolver;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.connection.ConnectionFactory;
import com.synopsys.integration.detect.tool.cache.CachedToolInstaller;
import com.synopsys.integration.detect.workflow.ArtifactResolver;
import com.synopsys.integration.detect.workflow.DetectRunId;
import com.synopsys.integration.detect.workflow.blackduck.DetectFontLoader;
import com.synopsys.integration.detect.workflow.blackduck.font.DetectFontInstaller;
import com.synopsys.integration.detect.workflow.blackduck.font.DetectFontLocator;
import com.synopsys.integration.detect.workflow.blackduck.font.OnlineDetectFontLocator;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.file.DirectoryOptions;

@Tag("integration")
public class FontLoaderTestIT {
    private File fontDirectory;
    private DetectFontLocator detectFontLocator;

    @BeforeEach
    public void createTempDirectory() throws Exception {
        fontDirectory = Files.createTempDirectory("junit_test_font_loader").toFile();
        PropertyConfiguration propertyConfiguration = new PropertyConfiguration(Collections.emptyList());
        Gson gson = new Gson();
        DetectConfigurationFactory detectConfigurationFactory = new DetectConfigurationFactory(propertyConfiguration, new SimplePathResolver(), gson);
        ConnectionFactory connectionFactory = new ConnectionFactory(detectConfigurationFactory.createConnectionDetails());
        ArtifactResolver artifactResolver = new ArtifactResolver(connectionFactory, gson, new CachedToolInstaller(null, gson));
        DetectFontInstaller installer = new DetectFontInstaller(artifactResolver);
        DirectoryOptions directoryOptions = new DirectoryOptions(null, null, null, null, fontDirectory.toPath(), null);
        DirectoryManager directoryManager = new DirectoryManager(directoryOptions, DetectRunId.createDefault());
        detectFontLocator = new OnlineDetectFontLocator(installer, directoryManager);
    }

    @AfterEach
    public void cleanTempDirectory() {
        FileUtils.deleteQuietly(fontDirectory);
    }

    @Test
    public void loadsCJKFont() {
        DetectFontLoader fontLoader = new DetectFontLoader(detectFontLocator);
        PDFont font = fontLoader.loadFont(new PDDocument());
        Assertions.assertTrue(font.getName().contains("CJK"));
    }

    @Test
    public void loadsCJKBoldFont() {
        DetectFontLoader fontLoader = new DetectFontLoader(detectFontLocator);
        PDFont font = fontLoader.loadBoldFont(new PDDocument());
        Assertions.assertTrue(font.getName().contains("CJK"));
    }

    @Test
    public void installOnceTestFont() {
        DetectFontLoader fontLoader = new DetectFontLoader(detectFontLocator);
        PDFont regularFont = fontLoader.loadFont(new PDDocument());
        PDFont boldFont = fontLoader.loadBoldFont(new PDDocument());
        Assertions.assertTrue(regularFont.getName().contains("CJK"));
        Assertions.assertTrue(boldFont.getName().contains("CJK"));
    }
}
