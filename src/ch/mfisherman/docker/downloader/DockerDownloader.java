package ch.mfisherman.docker.downloader;

import java.util.Arrays;
import java.util.List;

public abstract class DockerDownloader {

    final CommandLineArgs args;

    public DockerDownloader(CommandLineArgs args) {
        this.args = args;
    }

    void download() {
        List<String> repositories;
        if (args.user == null || args.user.isEmpty()) {
            repositories = Arrays.asList(args.repositories);
        } else {
            repositories = getRepositories(args.user);
        }

        download(repositories, Arrays.asList(args.tags), Arrays.asList(args.platforms), args.prune);
    }

    void download(List<String> repositories, List<String> tags, List<String> platforms, boolean prune) {
        for (String repository : repositories) {
            List<String> currentTags = tags;
            if(currentTags == null ) {
                currentTags = getTags(repository);
            }
            for (String tag : currentTags) {
                for (String platform : platforms) {
                    download(repository, tag, platform);
                    if (prune) {
                        prune();
                    }
                }
            }
        }
    }

    abstract public List<String> getRepositories(String user);

    abstract public List<String> getTags(String repositoryName);

    abstract public void download(String repository, String tag, String platform);

    abstract public void prune();
}