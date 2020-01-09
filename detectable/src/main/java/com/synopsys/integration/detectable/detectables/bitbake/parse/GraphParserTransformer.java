/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.bitbake.parse;

import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeFileType;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeGraph;

public class GraphParserTransformer {
    public BitbakeGraph transform(final GraphParser graphParser, final BitbakeFileType bitbakeFileType) {
        final BitbakeGraph bitbakeGraph = new BitbakeGraph();

        for (final GraphNode graphNode : graphParser.getNodes().values()) {
            final String name = getNameFromNode(graphNode);
            final String version = getVersionFromNode(graphNode, bitbakeFileType).orElse(null);
            bitbakeGraph.addNode(name, version);
        }

        for (final GraphEdge graphEdge : graphParser.getEdges().values()) {
            final String parent = getNameFromNode(graphEdge.getNode1());
            final String child = getNameFromNode(graphEdge.getNode2());
            bitbakeGraph.addChild(parent, child);
        }

        return bitbakeGraph;
    }

    private String getNameFromNode(final GraphNode graphNode) {
        return graphNode.getId().replaceAll("\"", "");
    }

    private Optional<String> getVersionFromNode(final GraphNode graphNode, final BitbakeFileType bitbakeFileType) {
        final Optional<String> attribute = getLabelAttribute(graphNode);
        return attribute.map((String label) -> getVersionFromLabel(label, bitbakeFileType));
    }

    private Optional<String> getLabelAttribute(final GraphNode graphNode) {
        final String attribute = (String) graphNode.getAttribute("label");
        Optional<String> result = Optional.empty();

        if (StringUtils.isNotBlank(attribute)) {
            result = Optional.of(attribute);
        }

        return result;
    }

    private String getVersionFromLabel(final String label, final BitbakeFileType bitbakeFileType) {
        if (bitbakeFileType.equals(BitbakeFileType.RECIPE_DEPENDS)) {
            final String[] components = label.split("\\\\n:|\\\\n");
            return components[1];
        } else if (bitbakeFileType.equals(BitbakeFileType.PACKAGE_DEPENDS)) {
            final String[] components = label.split(" |\\\\n");
            final String version = components[1];
            if (version.startsWith(":")) {
                return version.substring(1);
            } else {
                return version;
            }
        } else {
            throw new NotImplementedException(String.format("The %s does not support parsing of the '%s' file type.", this.getClass().getName(), bitbakeFileType.getFileName()));
        }
    }
}
