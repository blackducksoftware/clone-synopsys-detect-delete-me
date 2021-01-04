/**
 * detector
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.detector.evaluation;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.ExceptionDetectableResult;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;
import com.synopsys.integration.detector.result.DetectorResult;
import com.synopsys.integration.detector.result.FallbackNotNeededDetectorResult;
import com.synopsys.integration.detector.rule.DetectorRule;

public class ExtractableEvaluator extends Evaluator {
    private final Logger logger = LoggerFactory.getLogger(ExtractableEvaluator.class);
    private Function<DetectorEvaluation, ExtractionEnvironment> extractionEnvironmentProvider;

    public ExtractableEvaluator(DetectorEvaluationOptions evaluationOptions, Function<DetectorEvaluation, ExtractionEnvironment> extractionEnvironmentProvider) {
        super(evaluationOptions);
        this.extractionEnvironmentProvider = extractionEnvironmentProvider;
    }

    @Override
    protected DetectorEvaluationTree performEvaluation(DetectorEvaluationTree rootEvaluation) {
        extractableEvaluation(rootEvaluation);
        logger.debug("Preparing detectors for discovery and extraction.");
        setupDiscoveryAndExtractions(rootEvaluation, extractionEnvironmentProvider);
        return rootEvaluation;
    }

    public void extractableEvaluation(DetectorEvaluationTree detectorEvaluationTree) {
        logger.debug("Starting detector preparation.");
        logger.trace("Determining extractable detectors in the directory: {}", detectorEvaluationTree.getDirectory());
        for (DetectorEvaluation detectorEvaluation : detectorEvaluationTree.getOrderedEvaluations()) {
            if (detectorEvaluation.isSearchable() && detectorEvaluation.isApplicable()) {

                getDetectorEvaluatorListener().ifPresent(it -> it.extractableStarted(detectorEvaluation));

                logger.trace("Detector was searchable and applicable, will check extractable: {}", detectorEvaluation.getDetectorRule().getDescriptiveName());

                logger.trace("Checking to see if this detector is a fallback detector.");
                DetectableResult detectableExtractableResult = getDetectableExtractableResult(detectorEvaluationTree, detectorEvaluation);

                DetectorResult extractableResult = new DetectorResult(detectableExtractableResult.getPassed(), detectableExtractableResult.toDescription(), detectableExtractableResult.getClass());
                detectorEvaluation.setExtractable(extractableResult);
                if (detectorEvaluation.isExtractable()) {
                    logger.trace("Extractable passed. Done evaluating for now.");
                } else {
                    logger.trace("Extractable did not pass: {}", detectorEvaluation.getExtractabilityMessage());
                }

                getDetectorEvaluatorListener().ifPresent(it -> it.extractableEnded(detectorEvaluation));
            }
        }

        for (DetectorEvaluationTree childDetectorEvaluationTree : detectorEvaluationTree.getChildren()) {
            extractableEvaluation(childDetectorEvaluationTree);
        }
    }

    private DetectableResult getDetectableExtractableResult(DetectorEvaluationTree detectorEvaluationTree, DetectorEvaluation detectorEvaluation) {
        DetectableResult detectableExtractableResult;

        detectableExtractableResult = checkForFallbackDetector(detectorEvaluationTree, detectorEvaluation);

        if (detectableExtractableResult == null) {
            Detectable detectable = detectorEvaluation.getDetectable();
            try {
                return detectable.extractable();
            } catch (DetectableException e) {
                return new ExceptionDetectableResult(e);
            }
        }
        return detectableExtractableResult;
    }

    private DetectableResult checkForFallbackDetector(DetectorEvaluationTree detectorEvaluationTree, DetectorEvaluation detectorEvaluation) {
        Optional<DetectorRule> fallbackFrom = detectorEvaluationTree.getDetectorRuleSet().getFallbackFrom(detectorEvaluation.getDetectorRule());
        if (fallbackFrom.isPresent()) {
            Optional<DetectorEvaluation> fallbackEvaluationOptional = detectorEvaluationTree.getEvaluation(fallbackFrom.get());

            if (fallbackEvaluationOptional.isPresent()) {
                DetectorEvaluation fallbackEvaluation = fallbackEvaluationOptional.get();
                fallbackEvaluation.setFallbackTo(detectorEvaluation);
                detectorEvaluation.setFallbackFrom(fallbackEvaluation);

                if (fallbackEvaluation.isExtractable()) {
                    return new FallbackNotNeededDetectorResult(fallbackEvaluation.getDetectorRule());
                }
            }
        }
        return null;
    }

    public void setupDiscoveryAndExtractions(DetectorEvaluationTree detectorEvaluationTree, Function<DetectorEvaluation, ExtractionEnvironment> extractionEnvironmentProvider) {
        for (DetectorEvaluation detectorEvaluation : detectorEvaluationTree.getOrderedEvaluations()) {
            if (detectorEvaluation.isExtractable()) {
                ExtractionEnvironment extractionEnvironment = extractionEnvironmentProvider.apply(detectorEvaluation);
                detectorEvaluation.setExtractionEnvironment(extractionEnvironment);
            }
        }

        for (DetectorEvaluationTree childDetectorEvaluationTree : detectorEvaluationTree.getChildren()) {
            setupDiscoveryAndExtractions(childDetectorEvaluationTree, extractionEnvironmentProvider);
        }
    }
}
