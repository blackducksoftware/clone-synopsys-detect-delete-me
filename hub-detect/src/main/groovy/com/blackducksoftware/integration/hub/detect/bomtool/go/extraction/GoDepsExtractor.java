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
package com.blackducksoftware.integration.hub.detect.bomtool.go.extraction;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.bomtool.go.parse.GoGodepsParser;
import com.blackducksoftware.integration.hub.detect.extraction.model.Extraction;
import com.blackducksoftware.integration.hub.detect.model.BomToolGroupType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.google.gson.Gson;

public class GoDepsExtractor {

    private final Gson gson;
    private final ExternalIdFactory externalIdFactory;

    public GoDepsExtractor(final Gson gson, final ExternalIdFactory externalIdFactory) {
        this.gson = gson;
        this.externalIdFactory = externalIdFactory;
    }

    public Extraction extract(final File directory, final File goDepsDirectory) {
        try {
            final File goDepsFile = new File(goDepsDirectory, "Godeps.json");

            final String text = FileUtils.readFileToString(goDepsFile, StandardCharsets.UTF_8);

            final GoGodepsParser goDepParser = new GoGodepsParser(gson, externalIdFactory);
            final DependencyGraph dependencyGraph = goDepParser.extractProjectDependencies(text);

            final ExternalId externalId = externalIdFactory.createPathExternalId(Forge.GOLANG, directory.toString());

            final DetectCodeLocation codeLocation = new DetectCodeLocation.Builder(BomToolGroupType.GO_GODEP, directory.toString(), externalId, dependencyGraph).build();
            return new Extraction.Builder().success(codeLocation).build();
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

}
