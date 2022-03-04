package com.synopsys.integration.detectable.detectables.bitbake.unit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.annotations.UnitTest;
import com.synopsys.integration.detectable.detectable.util.EnumListFilter;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.transform.BitbakeGraphTransformer;
import com.synopsys.integration.detectable.util.graph.GraphAssert;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

@UnitTest
public class BitbakeGraphTransformerTest {
    @Test
    public void parentHasChild() {
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        BitbakeGraph bitbakeGraph = new BitbakeGraph();
        bitbakeGraph.addNode("example", "1:75-r50", "meta");
        bitbakeGraph.addNode("foobar", "12", "meta");
        bitbakeGraph.addChild("example", "foobar");

        Map<String, List<String>> recipeToLayerMap = new HashMap<>();
        recipeToLayerMap.put("example", Arrays.asList("meta"));
        recipeToLayerMap.put("foobar", Arrays.asList("meta"));

        BitbakeGraphTransformer bitbakeGraphTransformer = new BitbakeGraphTransformer(new ExternalIdFactory(), EnumListFilter.excludeNone());

        DependencyGraph dependencyGraph = bitbakeGraphTransformer.transform(bitbakeGraph, recipeToLayerMap, null);

        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.YOCTO, dependencyGraph);

        ExternalId example = graphAssert.hasDependency(externalIdFactory.createYoctoExternalId("meta", "example", "1:75-r50"));
        ExternalId foobar = graphAssert.hasDependency(externalIdFactory.createYoctoExternalId("meta", "foobar", "12"));
        graphAssert.hasParentChildRelationship(example, foobar);
    }

    @Test
    public void ignoredNoVersionRelationship() {
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        BitbakeGraph bitbakeGraph = new BitbakeGraph();
        bitbakeGraph.addNode("example", "75", "meta");
        bitbakeGraph.addNode("foobar", null, "meta");
        bitbakeGraph.addChild("example", "foobar");

        Map<String, List<String>> recipeToLayerMap = new HashMap<>();
        recipeToLayerMap.put("example", Arrays.asList("meta"));
        recipeToLayerMap.put("foobar", Arrays.asList("meta"));

        BitbakeGraphTransformer bitbakeGraphTransformer = new BitbakeGraphTransformer(new ExternalIdFactory(), EnumListFilter.excludeNone());
        DependencyGraph dependencyGraph = bitbakeGraphTransformer.transform(bitbakeGraph, recipeToLayerMap, null);

        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.YOCTO, dependencyGraph);
        graphAssert.hasRootSize(1);
        ExternalId externalId = graphAssert.hasDependency(externalIdFactory.createYoctoExternalId("meta", "example", "75"));
        graphAssert.hasRelationshipCount(externalId, 0);
    }

    @Test
    public void ignoredNoVersion() {
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        BitbakeGraph bitbakeGraph = new BitbakeGraph();
        bitbakeGraph.addNode("example", null, "meta");

        Map<String, List<String>> recipeToLayerMap = new HashMap<>();
        recipeToLayerMap.put("example", Arrays.asList("meta"));

        BitbakeGraphTransformer bitbakeGraphTransformer = new BitbakeGraphTransformer(new ExternalIdFactory(), EnumListFilter.excludeNone());
        DependencyGraph dependencyGraph = bitbakeGraphTransformer.transform(bitbakeGraph, recipeToLayerMap, null);

        GraphAssert graphAssert = new GraphAssert(Forge.YOCTO, dependencyGraph);
        graphAssert.hasNoDependency(externalIdFactory.createYoctoExternalId("meta", "example", null));
        graphAssert.hasRootSize(0);
    }
}
