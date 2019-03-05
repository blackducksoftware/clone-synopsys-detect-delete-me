/**
 * synopsys-detect
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.detect;

import javax.xml.parsers.DocumentBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.BdioTransformer;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchRunner;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.detect.configuration.ConnectionManager;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectableOptionFactory;
import com.synopsys.integration.detect.tool.detector.impl.DetectExecutableResolver;
import com.synopsys.integration.detect.tool.detector.impl.DetectInspectorResolver;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerOptions;
import com.synopsys.integration.detect.tool.signaturescanner.OfflineBlackDuckSignatureScanner;
import com.synopsys.integration.detect.tool.signaturescanner.OnlineBlackDuckSignatureScanner;
import com.synopsys.integration.detect.workflow.ArtifactResolver;
import com.synopsys.integration.detect.workflow.DetectRun;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameGenerator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.diagnostic.DiagnosticManager;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.AirGapManager;
import com.synopsys.integration.detect.workflow.file.AirGapOptions;
import com.synopsys.integration.detect.workflow.file.DetectFileFinder;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.impl.SimpleExecutableRunner;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectable.file.impl.SimpleFileFinder;

import freemarker.template.Configuration;

@org.springframework.context.annotation.Configuration
public class RunBeanConfiguration {
    @Autowired
    public DetectRun detectRun;
    @Autowired
    public DetectInfo detectInfo;
    @Autowired
    public DetectConfiguration detectConfiguration;
    @Autowired
    public DirectoryManager directoryManager;
    @Autowired
    public DiagnosticManager diagnosticManager;
    @Autowired
    public EventSystem eventSystem;
    @Autowired
    public Gson gson;
    @Autowired
    public Configuration configuration;
    @Autowired
    public DocumentBuilder documentBuilder;

    @Bean
    public ExternalIdFactory externalIdFactory() {
        return new ExternalIdFactory();
    }

    @Bean
    public FileFinder fileFinder() {
        return new SimpleFileFinder();
    }

    @Bean
    public ConnectionManager connectionManager() {
        return new ConnectionManager(detectConfiguration);
    }

    @Bean
    public ArtifactResolver artifactResolver() {
        return new ArtifactResolver(connectionManager(), gson);
    }

    @Bean
    public DetectConfigurationFactory detectConfigurationFactory() {
        return new DetectConfigurationFactory(detectConfiguration);
    }

    @Bean
    public CodeLocationNameGenerator codeLocationNameService() {
        return new CodeLocationNameGenerator();
    }

    @Bean
    public CodeLocationNameManager codeLocationNameManager() {
        return new CodeLocationNameManager(detectConfiguration, codeLocationNameService());
    }

    @Bean
    public BdioCodeLocationCreator detectCodeLocationManager() {
        return new BdioCodeLocationCreator(codeLocationNameManager(), detectConfiguration, directoryManager, eventSystem);
    }

    @Bean
    public AirGapManager airGapManager() {
        final AirGapOptions airGapOptions = detectConfigurationFactory().createAirGapOptions();
        return new AirGapManager(airGapOptions);
    }

    @Bean
    public BdioTransformer bdioTransformer() {
        return new BdioTransformer();
    }

    @Bean
    public ExecutableRunner executableRunner() {
        return new SimpleExecutableRunner();
    }

    @Bean
    public DetectableOptionFactory detectableOptionFactory() {
        return new DetectableOptionFactory(detectConfiguration, null);
    }

    @Bean
    public DetectExecutableResolver detectExecutableResolver() {
        return new DetectExecutableResolver();
    }

    @Bean
    public DetectInspectorResolver detectInspectorResolver() {
        return new DetectInspectorResolver();
    }

    // TODO: Jordan fix me

    //    @Bean
    //    public DockerInspectorManager dockerInspectorManager() {
    //        return new DockerInspectorManager(directoryManager, airGapManager(), fileFinder(), detectConfiguration, artifactResolver());
    //    }
    //
    //    @Lazy
    //    @Bean
    //    public DockerDetectable dockerBomTool(final DetectorEnvironment detectorEnvironment) {
    //        final DockerProperties dockerProperties = new DockerProperties(detectConfiguration);
    //
    //        final String tar = detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_TAR, PropertyAuthority.None);
    //        final String image = detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_IMAGE, PropertyAuthority.None);
    //        final boolean dockerRequired = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_DOCKER_PATH_REQUIRED, PropertyAuthority.None);
    //
    //        return new DockerDetectable(detectInfo, detectorEnvironment, directoryManager, dockerInspectorManager(), standardExecutableFinder(), dockerRequired, image, tar, dockerExtractor(dockerProperties));
    //    }
    //
    //    @Lazy
    //    @Bean
    //    public DockerExtractor dockerExtractor(final DockerProperties dockerProperties) {
    //        return new DockerExtractor(fileFinder(), dockerProperties, executableRunner(), bdioTransformer(), externalIdFactory(), gson);
    //    }

    @Lazy
    @Bean
    public OnlineBlackDuckSignatureScanner onlineBlackDuckSignatureScanner(final BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions, final ScanBatchRunner scanBatchRunner,
        final CodeLocationCreationService codeLocationCreationService, final BlackDuckServerConfig hubServerConfig) {
        return new OnlineBlackDuckSignatureScanner(directoryManager, fileFinder(), codeLocationNameManager(), blackDuckSignatureScannerOptions, eventSystem, scanBatchRunner, codeLocationCreationService, hubServerConfig);
    }

    @Lazy
    @Bean
    public OfflineBlackDuckSignatureScanner offlineBlackDuckSignatureScanner(final BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions, final ScanBatchRunner scanBatchRunner) {
        return new OfflineBlackDuckSignatureScanner(directoryManager, fileFinder(), codeLocationNameManager(), blackDuckSignatureScannerOptions, eventSystem, scanBatchRunner);
    }

}
