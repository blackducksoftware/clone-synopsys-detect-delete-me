package com.blackducksoftware.integration.hub.detect.workflow.diagnostic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.detect.bomtool.ExtractionId;
import com.blackducksoftware.integration.hub.detect.configuration.DetectConfigWrapper;
import com.blackducksoftware.integration.hub.detect.configuration.DetectProperty;

public class DiagnosticManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectConfigWrapper detectConfigWrapper;

    private File outputDirectory;

    private final List<File> trackedDirectories = new ArrayList<>();

    private final DiagnosticReportManager diagnosticReportManager;
    private final DiagnosticLogManager diagnosticLogManager;
    private final DetectRunManager detectRunManager;
    private final DiagnosticFileManager diagnosticFileManager;
    private final DiagnosticProfilingManager diagnosticProfilingManager;

    private boolean isDiagnosticProtected = false;
    private boolean isDiagnostic = false;

    public DiagnosticManager(final DetectConfigWrapper detectConfigWrapper, final BomToolProfiler profiler, final DiagnosticReportManager diagnosticReportManager, final DiagnosticLogManager diagnosticLogManager,
            final DetectRunManager detectRunManager, final DiagnosticFileManager diagnosticFileManager, final DiagnosticProfilingManager diagnosticProfilingManager) {
        this.detectConfigWrapper = detectConfigWrapper;
        this.diagnosticProfilingManager = diagnosticProfilingManager;
        this.diagnosticReportManager = diagnosticReportManager;
        this.diagnosticLogManager = diagnosticLogManager;
        this.detectRunManager = detectRunManager;
        this.diagnosticFileManager = diagnosticFileManager;
    }

    public void init(final boolean isDiagnostic, final boolean isDiagnosticProtected) {

        this.isDiagnostic = isDiagnostic;
        this.isDiagnosticProtected = isDiagnosticProtected;

        if (!isDiagnostic) {
            return;
        }

        System.out.println("");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Diagnostic mode on. Run id " + detectRunManager.getRunId());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("");

        final File outputDirectory = new File(detectConfigWrapper.getProperty(DetectProperty.DETECT_BDIO_OUTPUT_PATH));
        final File bdioDirectory = new File(detectConfigWrapper.getProperty(DetectProperty.DETECT_OUTPUT_PATH));
        try {
            diagnosticFileManager.init(outputDirectory, bdioDirectory, detectRunManager.getRunId());
        } catch (final Exception e) {
            logger.error("Failed to create diagnostics directory.");
            e.printStackTrace();
        }

        logger.info("Initializing diagnostic managers.");
        try {
            diagnosticReportManager.init(diagnosticFileManager.getReportDirectory(), detectRunManager.getRunId());
            diagnosticLogManager.init(diagnosticFileManager.getLogDirectory());
        } catch (final Exception e) {
            logger.error("Failed to initialize.");
            e.printStackTrace();
        }

        logger.info("Diagnostic mode on. Run id " + detectRunManager.getRunId());
    }

    public void finish() {
        if (!isDiagnosticModeOn()) {
            return;
        }

        try {
            logger.info("Finishing diagnostic mode.");
            diagnosticProfilingManager.finish();
            diagnosticReportManager.finish();
            diagnosticLogManager.finish();
        } catch (final Exception e) {
            logger.error("Failed to finish.");
            e.printStackTrace();
        }

        logger.info("Creating diagnostics zip.");
        boolean zipCreated = false;
        try {
            zipCreated = createZip();
        } catch (final Exception e) {
            logger.error("Failed to create diagnostic zip. Cleanup will not occur.");
            e.printStackTrace();
        }

        if (zipCreated) {
            if (detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_CLEANUP)) {
                try {
                    for (final File file : trackedDirectories) {
                        logger.info("Cleaning diagnostics directory: " + file.getPath());
                        FileUtils.deleteDirectory(file);
                    }
                } catch (final IOException e) {
                    logger.error("Failed to cleanup:");
                    e.printStackTrace();
                }
            }
        }

        logger.info("Diagnostic mode has completed.");
    }

    public boolean isDiagnosticModeOn() {
        return isDiagnostic;
    }

    /*
     * If this returns true, customer files or anything related to customer source should NOT be collected during diagnostics. Otherwise, things like lock files, solutions files, build reports may be collected during diagnostics.
     */
    public boolean isProtectedModeOn() {
        return isDiagnosticProtected;
    }

    public void registerFileOfInterest(final ExtractionId extractionId, final File file) {
        if (!isDiagnosticModeOn()) {
            return;
        }
        diagnosticFileManager.registerFileOfInterest(extractionId, file);
    }

    public void registerGlobalFileOfInterest(final File file) {
        if (!isDiagnosticModeOn()) {
            return;
        }
        diagnosticFileManager.registerGlobalFileOfInterest(file);
    }

    public void startLoggingExtraction(final ExtractionId extractionId) {
        if (!isDiagnosticModeOn()) {
            return;
        }
        diagnosticLogManager.startLoggingExtraction(extractionId);
    }

    public void stopLoggingExtraction(final ExtractionId extractionId) {
        if (!isDiagnosticModeOn()) {
            return;
        }
        diagnosticLogManager.stopLoggingExtraction(extractionId);
    }

    private boolean createZip() {
        final List<File> directoriesToCompress = diagnosticFileManager.getAllDirectories().stream()
                .filter(it -> it.exists())
                .collect(Collectors.toList());

        final DiagnosticZipCreator zipper = new DiagnosticZipCreator();
        return zipper.createDiagnosticZip(detectRunManager.getRunId(), outputDirectory, directoriesToCompress);
    }

}
