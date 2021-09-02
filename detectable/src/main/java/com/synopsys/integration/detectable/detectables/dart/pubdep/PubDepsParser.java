/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.dart.pubdep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

public class PubDepsParser {
    private static String UNRESOLVED_VERSION_SUFFIX = "...";  //TODO- name this more appropriately
    private static int ROOT_DEPTH = 1;

    private ExternalIdFactory externalIdFactory;

    public PubDepsParser(ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public DependencyGraph parse(List<String> pubDepsOutput) {
        MutableDependencyGraph dependencyGraph = new MutableMapDependencyGraph();

        Map<String, String> resolvedVersions = resolveVersionsOfDependencies(pubDepsOutput);

        parseLines(pubDepsOutput, 1, "", resolvedVersions, dependencyGraph);

        return dependencyGraph;
    }

    private void parseLines(List<String> lines, int depthOfParent, String nameOfParent, Map<String, String> resolvedVersions, MutableDependencyGraph dependencyGraph) {
        for (String line : lines) {
            int depthOfDependency = calculateDepth(line);
            String nameOfDependency = parseNameFromlLine(line);
            if (depthOfDependency == 0) {
                // non-graph line
                continue;
            } else if (depthOfDependency == ROOT_DEPTH) {
                dependencyGraph.addChildToRoot(createDependency(nameOfDependency, resolvedVersions));
            } else if (depthOfDependency == depthOfParent + 1) {
                // current dep is a child of parent
                dependencyGraph.addChildWithParent(createDependency(nameOfDependency, resolvedVersions), createDependency(nameOfParent, resolvedVersions));
            } else if (depthOfDependency <= depthOfParent) {
                return;
            }
            List<String> restOfLines = lines.subList(lines.indexOf(line) + 1, lines.size());
            parseLines(restOfLines, depthOfDependency, nameOfDependency, resolvedVersions, dependencyGraph);
        }
    }

    private Dependency createDependency(String name, Map<String, String> resolvedVersions) {
        String version = resolvedVersions.get(name);
        ExternalId externalIdChild = externalIdFactory.createNameVersionExternalId(Forge.DART, name, version);
        return new Dependency(name, version, externalIdChild);
    }

    private Map<String, String> resolveVersionsOfDependencies(List<String> pubDepsOutput) {
        Map<String, String> resolvedVersionsOfDependencies = new HashMap<>();
        for (String line : pubDepsOutput) {
            if (!line.endsWith("...")) {
                // <...> |-- <name> <version>
                String[] pieces = line.split(" ");
                String name = pieces[pieces.length - 2];
                String vesion = pieces[pieces.length - 1];
                resolvedVersionsOfDependencies.put(name, vesion);
            }
        }
        return resolvedVersionsOfDependencies;
    }

    private String parseNameFromlLine(String line) {
        String[] pieces = line.split(" ");
        if (line.endsWith(UNRESOLVED_VERSION_SUFFIX)) {
            // <...> <name>...
            String nameWithSuffix = pieces[pieces.length - 1];
            return nameWithSuffix.substring(0, nameWithSuffix.length() - UNRESOLVED_VERSION_SUFFIX.length());
        } else {
            // <...> <name> <version>
            return pieces[pieces.length - 2];
        }
    }

    private int calculateDepth(String line) {
        int depth = StringUtils.countMatches(line, "|");
        if (line.contains("'--")) {
            // |   '-- <name>...
            depth += StringUtils.countMatches(line, "'");
        }
        if (!line.equals(line.trim()) && line.trim().startsWith("'--")) {
            //    '-- collection...
            depth += 1;
        }
        return depth;
    }

}
