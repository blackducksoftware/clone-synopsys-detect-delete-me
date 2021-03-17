/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.blackduck.developer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.developermode.RapidScanService;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.status.OperationSystem;
import com.synopsys.integration.detect.workflow.status.StatusEventPublisher;

public class BlackDuckRapidMode {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 1;
    private static final String OPERATION_NAME = "Black Duck Rapid Scan";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final StatusEventPublisher statusEventPublisher;
    private final BlackDuckRunData blackDuckRunData;
    private final RapidScanService rapidScanService;
    private final Long timeoutInSeconds;
    private final OperationSystem operationSystem;

    public BlackDuckRapidMode(StatusEventPublisher statusEventPublisher, BlackDuckRunData blackDuckRunData, RapidScanService rapidScanService, Long timeoutInSeconds, OperationSystem operationSystem) {
        this.statusEventPublisher = statusEventPublisher;
        this.blackDuckRunData = blackDuckRunData;
        this.rapidScanService = rapidScanService;
        this.timeoutInSeconds = timeoutInSeconds;
        this.operationSystem = operationSystem;
    }

    public List<DeveloperScanComponentResultView> run(BdioResult bdioResult) throws DetectUserFriendlyException {
        logger.info("Begin Rapid Mode Scan");
        operationSystem.beginOperation(OPERATION_NAME);
        if (!blackDuckRunData.isOnline()) {
            logger.warn("Black Duck isn't online skipping rapid mode scan.");
            return Collections.emptyList();
        }

        List<DeveloperScanComponentResultView> results = new LinkedList<>();
        try {
            for (UploadTarget uploadTarget : bdioResult.getUploadTargets()) {
                results.addAll(rapidScanService.performDeveloperScan(uploadTarget.getUploadFile(), timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS));
            }
            logger.debug("Rapid scan result count: {}", results.size());
            operationSystem.completeWithSuccess(OPERATION_NAME);
        } catch (Exception e) {
            String errorReason = String.format("There was a problem: %s", e.getMessage());
            operationSystem.completeWithError(OPERATION_NAME, errorReason);
            throw new DetectUserFriendlyException(errorReason, e, ExitCodeType.FAILURE_GENERAL_ERROR);
        }
        return results;
    }
}
