package com.blackducksoftware.integration.hub.detect.extraction.bomtool.docker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.BdioReader;
import com.blackducksoftware.integration.hub.bdio.BdioTransformer;
import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.SimpleBdioDocument;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.extraction.Extraction;
import com.blackducksoftware.integration.hub.detect.extraction.Extractor;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocationFactory;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;
import com.blackducksoftware.integration.hub.detect.util.executable.Executable;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableArgumentBuilder;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunnerException;
import com.google.gson.Gson;

@Component
public class DockerExtractor extends Extractor<DockerContext> {
    private final Logger logger = LoggerFactory.getLogger(DockerExtractor.class);

    @Autowired
    DockerProperties dockerProperties;

    @Autowired
    protected DetectFileFinder detectFileFinder;

    @Autowired
    protected DetectFileManager detectFileManager;

    @Autowired
    ExecutableManager executableManager;

    @Autowired
    ExecutableRunner executableRunner;

    @Autowired
    BdioTransformer bdioTransformer;

    @Autowired
    ExternalIdFactory externalIdFactory;

    @Autowired
    Gson gson;

    @Autowired
    protected DetectCodeLocationFactory codeLocationFactory;

    static final String DEPENDENCIES_PATTERN = "*bdio.jsonld";

    @Override
    public Extraction extract(final DockerContext context) {
        try {
            String imageArgument = null;
            String imagePiece = null;
            if (context.tar != null) {
                final File dockerTarFile = new File(context.tar);
                imageArgument = String.format("--docker.tar=%s", dockerTarFile.getCanonicalPath());
                imagePiece = detectFileFinder.extractFinalPieceFromPath(dockerTarFile.getCanonicalPath());
            }else if (context.image != null) {
                imagePiece = context.image;
                imageArgument = String.format("--docker.image=%s", context.image);
            }

            if (imageArgument == null || imagePiece == null){
                return new Extraction.Builder().failure("No docker image found.").build();
            }else {
                return executeDocker(context, imageArgument, imagePiece, context.directory, context.dockerExe, context.bashExe, context.dockerInspectorInfo);
            }
        }catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

    private Map<String, String> createEnvironmentVariables(final File dockerExe) throws IOException {
        final Map<String, String> environmentVariables = new HashMap<>();
        dockerProperties.populateEnvironmentVariables(environmentVariables, dockerExe);
        return environmentVariables;
    }

    private void importTars(final File inspectorJar, final List<File> importTars, final File directory, final Map<String, String> environmentVariables, final File bashExe) {
        try {
            for (final File imageToImport : importTars) {
                final List<String> dockerImportArguments = Arrays.asList(
                        "-c",
                        "docker load -i \"" + imageToImport.getCanonicalPath() + "\""
                        );

                final Executable dockerImportImageExecutable = new Executable(directory, environmentVariables, bashExe.toString(), dockerImportArguments);
                executableRunner.execute(dockerImportImageExecutable);
            }
        } catch (final Exception e) {
            logger.debug("Exception encountered when resolving paths for docker air gap, running in online mode instead");
            logger.debug(e.getMessage());
        }
    }



    private Extraction executeDocker(final DockerContext context, final String imageArgument, final String imagePiece, final File directory, final File dockerExe, final File bashExe, final DockerInspectorInfo dockerInspectorInfo) throws FileNotFoundException, IOException, ExecutableRunnerException {

        final File outputDirectory = detectFileManager.getOutputDirectory(context);
        final File dockerPropertiesFile = detectFileManager.getOutputFile(context, "application.properties");
        dockerProperties.populatePropertiesFile(dockerPropertiesFile, outputDirectory);

        final Map<String, String> environmentVariables = createEnvironmentVariables(dockerExe);

        final ExecutableArgumentBuilder bashArguments = new ExecutableArgumentBuilder();
        bashArguments.addArgument("-c");
        bashArguments.addArgument(dockerInspectorInfo.dockerInspectorScript.getCanonicalPath(), true);
        bashArguments.addArgumentPair("--spring.config.location", "file:" + dockerProperties.toString(), true);
        bashArguments.addArgument(imageArgument);

        if (!dockerInspectorInfo.isOffline) {
            bashArguments.insertArgumentPair(2, "--dry.run", "true");
            bashArguments.insertArgumentPair(3, "--no.prompt", "true");
            bashArguments.insertArgumentPair(4, "--jar.path", dockerInspectorInfo.offlineDockerInspectorJar.getCanonicalPath(), true);
            importTars(dockerInspectorInfo.offlineDockerInspectorJar, dockerInspectorInfo.offlineTars, outputDirectory, environmentVariables, bashExe);
        }

        final Executable dockerExecutable = new Executable(outputDirectory, environmentVariables, bashExe.toString(), bashArguments.build());
        executableRunner.execute(dockerExecutable);

        return findCodeLocations(outputDirectory, directory, imagePiece);
    }

    private Extraction findCodeLocations(final File directoryToSearch, final File directory, final String imageName) {
        final File bdioFile = detectFileFinder.findFile(directoryToSearch, DEPENDENCIES_PATTERN);
        if (bdioFile != null) {
            SimpleBdioDocument simpleBdioDocument = null;
            BdioReader bdioReader = null;
            try {
                final InputStream dockerOutputInputStream = new FileInputStream(bdioFile);
                bdioReader = new BdioReader(gson, dockerOutputInputStream);
                simpleBdioDocument = bdioReader.readSimpleBdioDocument();
            } catch (final Exception e) {

            } finally {
                IOUtils.closeQuietly(bdioReader);
            }

            final DependencyGraph dependencyGraph = bdioTransformer.transformToDependencyGraph(simpleBdioDocument.project, simpleBdioDocument.components);

            final String projectName = simpleBdioDocument.project.name;
            final String projectVersionName = simpleBdioDocument.project.version;

            final Forge dockerForge = new Forge(ExternalId.BDIO_ID_SEPARATOR, ExternalId.BDIO_ID_SEPARATOR, simpleBdioDocument.project.bdioExternalIdentifier.forge);
            final String externalIdPath = simpleBdioDocument.project.bdioExternalIdentifier.externalId;
            final ExternalId projectExternalId = externalIdFactory.createPathExternalId(dockerForge, externalIdPath);

            final DetectCodeLocation detectCodeLocation = codeLocationFactory.createDockerCodeLocation(BomToolType.DOCKER, directory, imageName, projectExternalId, dependencyGraph);
            return new Extraction.Builder().success(detectCodeLocation).projectName(projectName).projectVersion(projectVersionName).build();

        } else {
            return new Extraction.Builder().failure("No files found matching pattern [" + DEPENDENCIES_PATTERN + "]. Expected docker-inspector to produce file in " + directory.toString()).build();
        }
    }


}
