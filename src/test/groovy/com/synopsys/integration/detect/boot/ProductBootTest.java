package com.synopsys.integration.detect.boot;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.boot.ProductBoot;
import com.synopsys.integration.detect.lifecycle.boot.ProductBootFactory;
import com.synopsys.integration.detect.lifecycle.boot.decision.BlackDuckDecision;
import com.synopsys.integration.detect.lifecycle.boot.decision.BootDecision;
import com.synopsys.integration.detect.lifecycle.boot.decision.PolarisDecision;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;

public class ProductBootTest {

    @Test(expected = DetectUserFriendlyException.class)
    public void bothProductsSkippedThrows() throws DetectUserFriendlyException {
        testBoot(BlackDuckDecision.forSkipBlackduck(), PolarisDecision.forSkipPolaris(), new HashMap<>());
    }

    @Test(expected = DetectUserFriendlyException.class)
    public void blackDuckConnectionFailureThrows() throws DetectUserFriendlyException {
        testBoot(BlackDuckDecision.forOnlineNotConnected("Failed to connect"), PolarisDecision.forSkipPolaris(), new HashMap<>());
    }

    @Test()
    public void blackDuckConnectionFailureWithDisableReturnsNull() throws DetectUserFriendlyException {
        HashMap<DetectProperty, Boolean> properties = new HashMap<>();
        properties.put(DetectProperty.DETECT_DISABLE_WITHOUT_BLACKDUCK, true);

        ProductRunData productRunData = testBoot(BlackDuckDecision.forOnlineNotConnected("Failed to connect"), PolarisDecision.forSkipPolaris(), properties);

        Assert.assertNull(productRunData);
    }

    @Test()
    public void blackDuckConnectionFailureWithTestReturnsNull() throws DetectUserFriendlyException {
        HashMap<DetectProperty, Boolean> properties = new HashMap<>();
        properties.put(DetectProperty.DETECT_TEST_CONNECTION, true);

        ProductRunData productRunData = testBoot(BlackDuckDecision.forOnlineNotConnected("Failed to connect"), PolarisDecision.forSkipPolaris(), properties);

        Assert.assertNull(productRunData);
    }


    private ProductRunData testBoot(BlackDuckDecision blackDuckDecision, PolarisDecision polarisDecision, Map<DetectProperty, Boolean> properties) throws DetectUserFriendlyException {
        DetectConfiguration detectConfiguration = Mockito.mock(DetectConfiguration.class);
        properties.forEach((key, value) -> Mockito.when(detectConfiguration.getBooleanProperty(key, PropertyAuthority.None)).thenReturn(value));

        ProductBootFactory productBootFactory = Mockito.mock(ProductBootFactory.class);
        Mockito.when(productBootFactory.createPhoneHomeManager(Mockito.any())).thenReturn(null);

        BootDecision bootDecision = new BootDecision(blackDuckDecision, polarisDecision);

        ProductBoot productBoot = new ProductBoot();
        return productBoot.boot(bootDecision, detectConfiguration, productBootFactory);
    }
}
