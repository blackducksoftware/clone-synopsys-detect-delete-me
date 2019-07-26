/**
 * detectable
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.detectable.detectables.maven.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.SAXParser;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.maven.parsing.parse.PomDependenciesHandler;

public class MavenParseExtractor {
    private final ExternalIdFactory externalIdFactory;
    private final boolean includePluginDependencies;
    private final SAXParser saxParser;

    public MavenParseExtractor(final ExternalIdFactory externalIdFactory, final boolean includePluginDependencies, final SAXParser saxParser) {
        this.externalIdFactory = externalIdFactory;
        this.includePluginDependencies = includePluginDependencies;
        this.saxParser = saxParser;
    }

    public Extraction extract(File pomXmlFile) {
        try (final InputStream pomXmlInputStream = new FileInputStream(pomXmlFile)) {
            //we have to create a new handler or the state of all handlers would be shared.
            //we could create a handler factory or some other indirection so it could be injected but for now we make a new one.
            PomDependenciesHandler pomDependenciesHandler = new PomDependenciesHandler(externalIdFactory, includePluginDependencies);
            saxParser.parse(pomXmlInputStream, pomDependenciesHandler);
            final List<Dependency> dependencies = pomDependenciesHandler.getDependencies();

            MutableMapDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
            dependencyGraph.addChildrenToRoot(dependencies);

            final CodeLocation codeLocation = new CodeLocation(dependencyGraph);
            return new Extraction.Builder().success(codeLocation).build();
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
