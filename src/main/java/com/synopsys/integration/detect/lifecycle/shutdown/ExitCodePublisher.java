/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.shutdown;

import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;

public class ExitCodePublisher {
    private EventSystem eventSystem;

    public ExitCodePublisher(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    public void publishExitCode(ExitCodeRequest exitCodeRequest) {
        eventSystem.publishEvent(Event.ExitCode, exitCodeRequest);
    }
}
