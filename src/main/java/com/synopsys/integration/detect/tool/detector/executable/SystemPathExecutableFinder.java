/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.tool.detector.executable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Finds an executable on the system path.
public class SystemPathExecutableFinder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DirectoryExecutableFinder executableFinder;

    public SystemPathExecutableFinder(DirectoryExecutableFinder executableFinder) {
        this.executableFinder = executableFinder;
    }

    public File findExecutable(final String executable) {
        final String systemPath = System.getenv("PATH");
        List<File> systemPathLocations = Arrays.stream(systemPath.split(File.pathSeparator))
                                             .map(File::new)
                                             .collect(Collectors.toList());

        File found = executableFinder.findExecutable(executable, systemPathLocations);
        if (found == null) {
            logger.debug(String.format("Could not find the executable: %s while searching through: %s", executable, systemPath));
        }
        return found;
    }
}
