/*
 * common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.common.util.finder;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FileFinder {
    // Find with predicate
    @Nullable
    default File findFile(File directoryToSearch, Predicate<File> filter) {
        return findFile(directoryToSearch, filter, false, 0);
    }

    @Nullable
    default File findFile(File directoryToSearch, Predicate<File> filter, boolean followSymLinks, int depth) {
        List<File> files = findFiles(directoryToSearch, filter, followSymLinks, depth);
        if (files != null && files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, Predicate<File> filter) {
        return findFiles(directoryToSearch, filter, false, 0);
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, Predicate<File> filter, boolean followSymLinks, int depth) {
        return findFiles(directoryToSearch, filter, followSymLinks, depth, true);
    }

    @NotNull
    List<File> findFiles(File directoryToSearch, Predicate<File> filter, boolean followSymLinks, int depth, boolean findInsideMatchingDirectories);

    // Find with file pame patterns
    @Nullable
    default File findFile(File directoryToSearch, String filenamePattern) {
        return findFile(directoryToSearch, filenamePattern, false, 0);
    }

    @Nullable
    default File findFile(File directoryToSearch, String filenamePattern, boolean followSymLinks, int depth) {
        List<File> files = findFiles(directoryToSearch, Collections.singletonList(filenamePattern), followSymLinks, depth);
        if (files != null && files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, String filenamePattern) {
        return findFiles(directoryToSearch, Collections.singletonList(filenamePattern), false, 0);
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, String filenamePattern, boolean followSymLinks, int depth) {
        return findFiles(directoryToSearch, Collections.singletonList(filenamePattern), followSymLinks, depth);
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, List<String> filenamePatterns) {
        return findFiles(directoryToSearch, filenamePatterns, false, 0);
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, List<String> filenamePatterns, boolean followSymLinks, int depth) {
        return findFiles(directoryToSearch, filenamePatterns, followSymLinks, depth, true);
    }

    @NotNull
    default List<File> findFiles(File directoryToSearch, List<String> filenamePatterns, boolean followSymLinks, int depth, boolean findInsideMatchingDirectories) {
        Predicate<File> wildcardFilter = file -> {
            WildcardFileFilter filter = new WildcardFileFilter(filenamePatterns);
            return filter.accept(file);
        };
        return findFiles(directoryToSearch, wildcardFilter, followSymLinks, depth, findInsideMatchingDirectories);
    }

}
