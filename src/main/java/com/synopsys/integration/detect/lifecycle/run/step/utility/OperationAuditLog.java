/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.step.utility;

import java.io.IOException;

import com.synopsys.integration.blackduck.exception.BlackDuckTimeoutExceededException;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.workflow.status.Operation;
import com.synopsys.integration.detect.workflow.status.OperationSystem;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

//Essentially an adapter for 'running an operation' and 'reporting the operation' in one step. Whether or not this is desired is TBD.
public class OperationAuditLog { //NoOpAuditLog
    private final OperationSystem operationSystem;
    private final ExitCodeManager exitCodeManager;

    public OperationAuditLog(OperationSystem operationSystem, ExitCodeManager exitCodeManager) {
        this.operationSystem = operationSystem;
        this.exitCodeManager = exitCodeManager;
    }

    public void named(String name, OperationFunction supplier) throws DetectUserFriendlyException {
        Operation operation = operationSystem.startOperation(name);
        try {
            supplier.execute();
            operationSystem.completeWithSuccess(name);
        } catch (InterruptedException e) {
            String errorReason = String.format("There was a problem: %s", e.getMessage());
            operationSystem.completeWithError(name, errorReason);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new DetectUserFriendlyException(errorReason, e, ExitCodeType.FAILURE_GENERAL_ERROR);
        } catch (Exception e) {
            String errorReason = String.format("There was a problem: %s", e.getMessage());
            operationSystem.completeWithError(name, errorReason);
            throw new DetectUserFriendlyException(errorReason, e, exitCodeManager.getExitCodeFromExceptionDetails(e));
        } finally {
            operation.finish();
        }
    }

    public <T> T named(String name, OperationSupplier<T> supplier) throws DetectUserFriendlyException {
        Operation operation = operationSystem.startOperation(name);
        try {
            T value = supplier.execute();
            operationSystem.completeWithSuccess(name);
            return value;
        } catch (InterruptedException e) {
            String errorReason = String.format("There was a problem: %s", e.getMessage());
            operationSystem.completeWithError(name, errorReason);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new DetectUserFriendlyException(errorReason, e, ExitCodeType.FAILURE_GENERAL_ERROR);
        } catch (Exception e) {
            String errorReason = String.format("There was a problem: %s", e.getMessage());
            operationSystem.completeWithError(name, errorReason);
            throw new DetectUserFriendlyException(errorReason, e, exitCodeManager.getExitCodeFromExceptionDetails(e));
        } finally {
            operation.finish();
        }
    }

    @FunctionalInterface
    public interface OperationSupplier<T> {
        public T execute() throws DetectUserFriendlyException, IntegrationException, InterruptedException, IOException, IntegrationRestException, BlackDuckTimeoutExceededException; //basically all known detect exceptions.
    }

    @FunctionalInterface
    public interface OperationFunction {
        public void execute() throws DetectUserFriendlyException, IntegrationException, InterruptedException, IOException, IntegrationRestException, BlackDuckTimeoutExceededException; //basically all known detect exceptions.
    }
}