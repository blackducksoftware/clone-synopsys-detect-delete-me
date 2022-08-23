package com.synopsys.integration.detectable.detectables.bazel.pipeline.step.parse;

import java.net.MalformedURLException;

import org.jetbrains.annotations.NotNull;

public class GithubUrlParser {
    private final String url;

    // "https://github.com/gflags/gflags/archive/v2.2.2.tar.gz"
    // "https://github.com/google/glog/archive/v0.4.0.tar.gz"
    public GithubUrlParser(String url) {
        this.url = url;
    }

    public String getOrganization() throws MalformedURLException {
        int indexOrganization = getIndexOrganization();
        return getOrganization(indexOrganization);
    }

    @NotNull
    private String getOrganization(int indexOrganization) {
        String organizationRepoPath = url.substring(indexOrganization);
        int indexSlashAfterOrganization = organizationRepoPath.indexOf('/');
        return organizationRepoPath.substring(0, indexSlashAfterOrganization);
    }

    public String getRepo() throws MalformedURLException {
        int indexRepo = getIndexRepo();
        String repoEtc = url.substring(indexRepo);
        int slashAfterRepo = repoEtc.indexOf('/');
        return repoEtc.substring(0, slashAfterRepo);
    }

    public String getVersion() throws MalformedURLException {
        int indexSlashArchive = url.indexOf("/archive/");
        if (indexSlashArchive < 0) {
            throw new MalformedURLException("Missing archive between repo and filename");
        }
        int indexFilename = indexSlashArchive + "/archive/".length();
        int totalLength = url.length();
        if (indexFilename > totalLength) {
            throw new MalformedURLException("Missing filename");
        }
        String filename = url.substring(indexFilename);
        int indexExtension = getIndexExtension(filename);
        return filename.substring(0, indexExtension);
    }

    private int getIndexRepo() throws MalformedURLException {
        int indexOrganization = getIndexOrganization();
        String organization = getOrganization(indexOrganization);
        return getIndexRepo(indexOrganization, organization);
    }

    private int getIndexOrganization() throws MalformedURLException {
        int indexPostSchemeColon = url.indexOf("://github.com/");
        if (indexPostSchemeColon < 1) {
            throw new MalformedURLException("Missing scheme://github.com/ prefix");
        }
        return indexPostSchemeColon + "://github.com/".length();
    }

    private int getIndexRepo(int indexOrganization, String organization) throws MalformedURLException {
        return indexOrganization + organization.length() + 1;
    }

    private int getIndexExtension(String filename) throws MalformedURLException {
        if (filename.endsWith(".tar.gz")) {
            return filename.length() - ".tar.gz".length();
        }
        if (filename.endsWith(".zip")) {
            return filename.length() - ".zip".length();
        }
        if (filename.endsWith(".gz")) {
            return filename.length() - ".gz".length();
        }
        throw new MalformedURLException("Unrecognized file extension");
    }
}
