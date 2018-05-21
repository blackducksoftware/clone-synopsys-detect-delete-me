package com.blackducksoftware.integration.hub.detect.extraction.bomtool.cocoapods;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.extraction.Extraction;
import com.blackducksoftware.integration.hub.detect.extraction.Extractor;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.cocoapods.parse.CocoapodsPackager;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocationFactory;

@Component
public class PodlockExtractor extends Extractor<PodlockContext> {

    @Autowired
    CocoapodsPackager cocoapodsPackager;

    @Autowired
    protected ExternalIdFactory externalIdFactory;

    @Autowired
    protected DetectCodeLocationFactory codeLocationFactory;

    @Override
    public Extraction extract(final PodlockContext context) {
        String podLockText;
        try {
            podLockText = FileUtils.readFileToString(context.podlock, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return new Extraction.Builder().exception(e).build();
        }

        DependencyGraph dependencyGraph;
        try {
            dependencyGraph = cocoapodsPackager.extractDependencyGraph(podLockText);
        } catch (final IOException e) {
            return new Extraction.Builder().exception(e).build();
        }

        final ExternalId externalId = externalIdFactory.createPathExternalId(Forge.COCOAPODS, context.directory.toString());

        final DetectCodeLocation codeLocation = codeLocationFactory.createBomCodeLocation(BomToolType.COCOAPODS, context.directory, externalId, dependencyGraph);

        return new Extraction.Builder().success(codeLocation).build();
    }

}
