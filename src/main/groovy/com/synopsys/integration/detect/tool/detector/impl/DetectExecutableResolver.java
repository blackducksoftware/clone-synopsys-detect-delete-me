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
package com.synopsys.integration.detect.tool.detector.impl;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.impl.SimpleExecutableResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.BashResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.BazelResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.CondaResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.CpanResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.CpanmResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.DockerResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.DotNetResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.GradleResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.JavaResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.MavenResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.NpmResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.PearResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.PipResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.PipenvResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.PythonResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.Rebar3Resolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.YarnResolver;
import com.synopsys.integration.detectable.detectable.inspector.go.GoDepResolver;
import com.synopsys.integration.detectable.detectable.inspector.go.GoResolver;
import com.synopsys.integration.detectable.detectable.inspector.go.impl.GithubGoDepResolver;

public class DetectExecutableResolver
    implements JavaResolver, GradleResolver, BashResolver, CondaResolver, CpanmResolver, CpanResolver, PearResolver, Rebar3Resolver, YarnResolver, PythonResolver, PipResolver, PipenvResolver, MavenResolver, NpmResolver, BazelResolver,
                   DockerResolver, GoDepResolver, GoResolver, DotNetResolver {

    private final SimpleExecutableResolver simpleExecutableResolver;
    private final GithubGoDepResolver githubGoDepResolver;
    private final DetectConfiguration detectConfiguration;

    public DetectExecutableResolver(final SimpleExecutableResolver simpleExecutableResolver, final GithubGoDepResolver githubGoDepResolver, final DetectConfiguration detectConfiguration) {
        this.simpleExecutableResolver = simpleExecutableResolver;
        this.githubGoDepResolver = githubGoDepResolver;
        this.detectConfiguration = detectConfiguration;
    }

    private File resolveExecutable(final Supplier<File> resolveExecutable, final String executableOverride) {
        if (StringUtils.isBlank(executableOverride)) {
            return resolveExecutable.get();
        } else {
            return new File(executableOverride);
        }
    }

    private File resolveExecutable(final Function<DetectableEnvironment, File> resolveExecutable, final DetectableEnvironment environment, final String executableOverride) {
        if (StringUtils.isBlank(executableOverride)) {
            return resolveExecutable.apply(environment);
        } else {
            return new File(executableOverride);
        }
    }

    @Override
    public File resolveBash() {
        return resolveExecutable(simpleExecutableResolver::resolveBash, detectConfiguration.getProperty(DetectProperty.DETECT_BASH_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveBazel() {
        return resolveExecutable(simpleExecutableResolver::resolveBazel, detectConfiguration.getProperty(DetectProperty.DETECT_BAZEL_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveConda() {
        return resolveExecutable(simpleExecutableResolver::resolveConda, detectConfiguration.getProperty(DetectProperty.DETECT_CONDA_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveCpan() {
        return resolveExecutable(simpleExecutableResolver::resolveCpan, detectConfiguration.getProperty(DetectProperty.DETECT_CPAN_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveCpanm() {
        return resolveExecutable(simpleExecutableResolver::resolveCpanm, detectConfiguration.getProperty(DetectProperty.DETECT_CPANM_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveGradle(final DetectableEnvironment environment) {
        return resolveExecutable(simpleExecutableResolver::resolveGradle, environment, detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveMaven(final DetectableEnvironment environment) {
        return resolveExecutable(simpleExecutableResolver::resolveMaven, environment, detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveNpm(final DetectableEnvironment environment) {
        return resolveExecutable(simpleExecutableResolver::resolveNpm, environment, detectConfiguration.getProperty(DetectProperty.DETECT_NPM_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolvePear() {
        return resolveExecutable(simpleExecutableResolver::resolvePear, detectConfiguration.getProperty(DetectProperty.DETECT_PEAR_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolvePip() {
        return resolveExecutable(simpleExecutableResolver::resolvePip, null);
    }

    @Override
    public File resolvePipenv() {
        return resolveExecutable(simpleExecutableResolver::resolvePipenv, detectConfiguration.getProperty(DetectProperty.DETECT_PIPENV_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolvePython() {
        return resolveExecutable(simpleExecutableResolver::resolvePython, detectConfiguration.getProperty(DetectProperty.DETECT_PYTHON_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveRebar3() {
        return resolveExecutable(simpleExecutableResolver::resolveRebar3, detectConfiguration.getProperty(DetectProperty.DETECT_HEX_REBAR3_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveYarn() {
        return resolveExecutable(simpleExecutableResolver::resolveYarn, detectConfiguration.getProperty(DetectProperty.DETECT_YARN_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveGoDep(final File location) throws DetectableException {
        return githubGoDepResolver.resolveGoDep(location);
    }

    @Override
    public File resolveGo() {
        return githubGoDepResolver.resolveGo();
    }

    @Override
    public File resolveJava() {
        return resolveExecutable(simpleExecutableResolver::resolveJava, detectConfiguration.getProperty(DetectProperty.DETECT_JAVA_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveDocker() {
        return resolveExecutable(simpleExecutableResolver::resolveDocker, detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_PATH, PropertyAuthority.None));
    }

    @Override
    public File resolveDotNet() {
        return resolveExecutable(simpleExecutableResolver::resolveDotNet, detectConfiguration.getProperty(DetectProperty.DETECT_DOTNET_PATH, PropertyAuthority.None));
    }
}
