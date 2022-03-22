package com.synopsys.integration.detectable.detectables.swift.lock;

import java.io.File;
import java.io.IOException;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.Requirements;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.PackageResolvedNotFoundDetectableResult;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "Swift", forge = "Swift.org", requirementsMarkdown = "File: Package.swift, Package.resolved")
public class SwiftPackageResolvedDetectable extends Detectable {
    public static final String PACKAGE_RESOLVED_FILENAME = "Package.resolved";

    private final FileFinder fileFinder;
    private final PackageResolvedExtractor packageResolvedExtractor;

    private File foundPackageResolvedFile;

    public SwiftPackageResolvedDetectable(DetectableEnvironment environment, FileFinder fileFinder, PackageResolvedExtractor packageResolvedExtractor) {
        super(environment);
        this.fileFinder = fileFinder;
        this.packageResolvedExtractor = packageResolvedExtractor;
    }

    @Override
    public DetectableResult applicable() {
        Requirements requirements = new Requirements(fileFinder, environment);
        requirements.file("Package.swift");
        return requirements.result();
    }

    @Override
    public DetectableResult extractable() throws DetectableException {
        Requirements requirements = new Requirements(fileFinder, environment);
        foundPackageResolvedFile = requirements.file(PACKAGE_RESOLVED_FILENAME, () -> new PackageResolvedNotFoundDetectableResult(environment.getDirectory().getAbsolutePath()));
        return requirements.result();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) throws ExecutableFailedException, IOException {
        return packageResolvedExtractor.extract(foundPackageResolvedFile, environment.getDirectory());
    }

}