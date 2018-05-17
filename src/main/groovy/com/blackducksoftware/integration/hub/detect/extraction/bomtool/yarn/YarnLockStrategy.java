package com.blackducksoftware.integration.hub.detect.extraction.bomtool.yarn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.strategy.Strategy;
import com.blackducksoftware.integration.hub.detect.strategy.evaluation.StrategyEnvironment;
import com.blackducksoftware.integration.hub.detect.strategy.result.FileNotFoundStrategyResult;
import com.blackducksoftware.integration.hub.detect.strategy.result.PassedStrategyResult;
import com.blackducksoftware.integration.hub.detect.strategy.result.StrategyResult;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;

@Component
public class YarnLockStrategy extends Strategy<YarnLockContext, YarnLockExtractor> {
    public static final String YARN_LOCK_FILENAME = "yarn.lock";

    @Autowired
    public DetectFileFinder fileFinder;

    public YarnLockStrategy() {
        super("Yarn Lock", BomToolType.YARN, YarnLockContext.class, YarnLockExtractor.class);
    }

    @Override
    public StrategyResult applicable(final StrategyEnvironment environment, final YarnLockContext context) {
        context.yarnlock = fileFinder.findFile(environment.getDirectory(), YARN_LOCK_FILENAME);
        if (context.yarnlock == null) {
            return new FileNotFoundStrategyResult(YARN_LOCK_FILENAME);
        }

        return new PassedStrategyResult();
    }

    @Override
    public StrategyResult extractable(final StrategyEnvironment environment, final YarnLockContext context){
        return new PassedStrategyResult();
    }

}