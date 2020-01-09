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

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;

import com.synopsys.integration.detect.config.DetectConfig;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.boot.DetectBoot;
import com.synopsys.integration.detect.lifecycle.boot.DetectBootFactory;
import com.synopsys.integration.detect.lifecycle.boot.DetectBootResult;
import com.synopsys.integration.detect.lifecycle.run.RunManager;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeUtility;
import com.synopsys.integration.detect.lifecycle.shutdown.ShutdownManager;
import com.synopsys.integration.detect.workflow.DetectRun;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.report.ReportManager;
import com.synopsys.integration.detect.workflow.status.DetectIssue;
import com.synopsys.integration.detect.workflow.status.DetectIssueType;
import com.synopsys.integration.detect.workflow.status.DetectStatusManager;
import com.synopsys.integration.log.Slf4jIntLogger;

public class Application implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    public static boolean SHOULD_EXIT = true;

    private final ConfigurableEnvironment environment;

    @Autowired
    public Application(final ConfigurableEnvironment environment) {
        this.environment = environment;
        environment.setIgnoreUnresolvableNestedPlaceholders(true);
    }

    public static void main(final String[] args) {
        final SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
        builder.logStartupInfo(false);
        builder.run(args);
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) {
        final long startTime = System.currentTimeMillis();

        //Events, Status and Exit Codes are required even if boot fails.
        final EventSystem eventSystem = new EventSystem();
        final DetectStatusManager statusManager = new DetectStatusManager(eventSystem);

        final ExitCodeUtility exitCodeUtility = new ExitCodeUtility();
        final ExitCodeManager exitCodeManager = new ExitCodeManager(eventSystem, exitCodeUtility);

        final ReportManager reportManager = ReportManager.createDefault(eventSystem);

        //Before boot even begins, we create a new Spring context for Detect to work within.
        logger.debug("Initializing detect.");
        final DetectRun detectRun = DetectRun.createDefault();
        final DetectContext detectContext = new DetectContext(detectRun);

        Optional<DetectBootResult> detectBootResultOptional = Optional.empty();
        boolean printOutput = true;
        boolean shouldForceSuccess = false;

        try {
            logger.debug("Detect boot begin.");
            final DetectBoot detectBoot = new DetectBoot(new DetectBootFactory());
            detectBootResultOptional = Optional.ofNullable(detectBoot.boot(detectRun, applicationArguments.getSourceArgs(), environment, eventSystem, detectContext));
            logger.debug("Detect boot completed.");
        } catch (final Exception e) {
            logger.error("Detect boot failed.");
            exitCodeManager.requestExitCode(e);
        }
        if (detectBootResultOptional.isPresent()) {
            final DetectBootResult detectBootResult = detectBootResultOptional.get();
            if (detectBootResult.getBootType() == DetectBootResult.BootType.RUN && detectBootResult.getProductRunData().isPresent()) {
                logger.debug("Detect will attempt to run.");
                final ProductRunData productRunData = detectBootResult.getProductRunData().get();
                final RunManager runManager = new RunManager(detectContext);
                try {
                    logger.debug("Detect run begin: " + detectRun.getRunId());
                    runManager.run(productRunData);
                    logger.debug("Detect run completed.");
                } catch (final Exception e) {
                    if (e.getMessage() != null) {
                        logger.error("Detect run failed: " + e.getMessage());
                    } else {
                        logger.error("Detect run failed: " + e.getClass().getSimpleName());
                    }
                    logger.debug("An exception was thrown during the detect run.", e);
                    exitCodeManager.requestExitCode(e);
                }
            } else {
                logger.debug("Detect will NOT attempt to run.");
                detectBootResult.getException().ifPresent(exitCodeManager::requestExitCode);
                detectBootResult.getException().ifPresent(e -> DetectIssue.publish(eventSystem, DetectIssueType.Exception, e.getMessage()));
            }

            if (detectBootResult.getDetectConfiguration().isPresent()) {
                final DetectConfig detectConfiguration = detectBootResult.getDetectConfiguration().get();
                printOutput = !detectConfiguration.getValueOrDefault(DetectProperties.Companion.getDETECT_SUPPRESS_RESULTS_OUTPUT());
                shouldForceSuccess = detectConfiguration.getValueOrDefault(DetectProperties.Companion.getDETECT_FORCE_SUCCESS());
            }
        }

        try {
            logger.debug("Detect shutdown begin.");
            final ShutdownManager shutdownManager = new ShutdownManager();
            shutdownManager.shutdown(
                ifPresentMap(detectBootResultOptional, DetectBootResult::getProductRunData),
                ifPresentMap(detectBootResultOptional, DetectBootResult::getAirGapZip),
                ifPresentMap(detectBootResultOptional, DetectBootResult::getDetectConfiguration),
                ifPresentMap(detectBootResultOptional, DetectBootResult::getDirectoryManager),
                ifPresentMap(detectBootResultOptional, DetectBootResult::getDiagnosticSystem));
            logger.debug("Detect shutdown completed.");
        } catch (final Exception e) {
            logger.error("Detect shutdown failed.");
            exitCodeManager.requestExitCode(e);
        }

        logger.debug("All Detect actions completed.");

        //Generally, when requesting a failure status, an exit code is also requested, but if it is not, we default to an unknown error.
        if (statusManager.hasAnyFailure()) {
            eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_UNKNOWN_ERROR, "A failure status was requested by one or more of Detect's tools."));
        }

        //Find the final (as requested) exit code
        final ExitCodeType finalExitCode = exitCodeManager.getWinningExitCode();

        //Print detect's status
        if (printOutput) {
            reportManager.printDetectorIssues();
            statusManager.logDetectResults(new Slf4jIntLogger(logger), finalExitCode);
        }

        //Print duration of run
        final long endTime = System.currentTimeMillis();
        logger.info(String.format("Detect duration: %s", DurationFormatUtils.formatPeriod(startTime, endTime, "HH'h' mm'm' ss's' SSS'ms'")));

        //Exit with formal exit code
        if (finalExitCode != ExitCodeType.SUCCESS && shouldForceSuccess) {
            logger.warn(String.format("Forcing success: Exiting with exit code 0. Ignored exit code was %s.", finalExitCode.getExitCode()));
            System.exit(0);
        } else if (finalExitCode != ExitCodeType.SUCCESS) {
            logger.error(String.format("Exiting with code %s - %s", finalExitCode.getExitCode(), finalExitCode.toString()));
        }

        if (SHOULD_EXIT) {
            System.exit(finalExitCode.getExitCode());
        } else {
            logger.info(String.format("Would normally exit(%s) but it is overridden.", finalExitCode.getExitCode()));
        }
    }

    private static <T, U> Optional<U> ifPresentMap(final Optional<T> optional, final Function<T, Optional<U>> operator) {
        if (optional.isPresent()) {
            return operator.apply(optional.get());
        } else {
            return Optional.empty();
        }
    }
}