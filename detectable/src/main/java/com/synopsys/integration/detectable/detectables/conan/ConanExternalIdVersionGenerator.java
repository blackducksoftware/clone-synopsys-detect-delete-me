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
package com.synopsys.integration.detectable.detectables.conan;

import java.util.Optional;

import com.synopsys.integration.detectable.detectables.conan.graph.ConanNode;
import com.synopsys.integration.exception.IntegrationException;

public class ConanExternalIdVersionGenerator {

    public String generateExternalIdVersionString(ConanNode<String> node, boolean preferLongFormExternalIds) throws IntegrationException {
        String externalIdVersion;
        if (hasValue(node.getRecipeRevision()) && hasValue(node.getPackageRevision()) && preferLongFormExternalIds) {
            // generate long form
            // <name>/<version>@<user>/<channel>#<recipe_revision>:<package_id>#<package_revision>
            externalIdVersion = String.format("%s@%s/%s#%s:%s#%s",
                node.getVersion().orElseThrow(() -> new IntegrationException(String.format("Missing dependency version: %s", node))),
                node.getUser().orElse("_"),
                node.getChannel().orElse("_"),
                node.getRecipeRevision().get(),
                node.getPackageId().orElse("0"),
                node.getPackageRevision().get());
        } else {
            // generate short form
            // <name>/<version>@<user>/<channel>#<recipe_revision>
            externalIdVersion = String.format("%s@%s/%s#%s",
                node.getVersion().orElseThrow(() -> new IntegrationException(String.format("Missing dependency version: %s", node))),
                node.getUser().orElse("_"),
                node.getChannel().orElse("_"),
                node.getRecipeRevision().orElse("0"));
        }
        return externalIdVersion;
    }

    private boolean hasValue(Optional<String> value) {
        if ((!value.isPresent()) || ("None".equals(value.get()))) {
            return false;
        }
        return true;
    }
}
