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
package com.blackducksoftware.integration.hub.detect.workflow.report;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.detect.workflow.bomtool.BomToolEvaluation;
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.DetectCodeLocation;

public class ExtractionSummaryReporter {

    public void writeSummary(final ReportWriter writer, final List<BomToolEvaluation> results, final Map<DetectCodeLocation, String> codeLocationNameMap) {
        final ExtractionSummarizer summarizer = new ExtractionSummarizer();

        final List<ExtractionSummaryData> summaries = summarizer.summarize(results, codeLocationNameMap);

        writeSummaries(writer, summaries);
    }

    private void writeSummaries(final ReportWriter writer, final List<ExtractionSummaryData> data) {
        writer.writeLine();
        writer.writeLine();
        writer.writeHeader();
        writer.writeLine("Extraction results:");
        writer.writeHeader();
        data.stream().forEach(it -> {
            if (it.applicable > 0) {
                writer.writeLine(it.getDirectory());
                writer.writeLine("\tCode locations: " + it.codeLocationsExtracted);
                it.codeLocationNames.stream().forEach(name -> writer.writeLine("\t\t" + name));
                if (it.success.size() > 0) {
                    writer.writeLine("\tSuccess: " + it.success.stream().map(success -> success.getBomTool().getDescriptiveName()).collect(Collectors.joining(", ")));
                }
                if (it.failed.size() > 0) {
                    writer.writeLine("\tFailure: " + it.failed.stream().map(failed -> failed.getBomTool().getDescriptiveName()).collect(Collectors.joining(", ")));
                }
                if (it.exception.size() > 0) {
                    writer.writeLine("\tException: " + it.exception.stream().map(exception -> exception.getBomTool().getDescriptiveName()).collect(Collectors.joining(", ")));
                }
            }
        });
        writer.writeHeader();
        writer.writeLine();
        writer.writeLine();
    }

}
