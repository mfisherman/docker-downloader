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
        if (args.owner.isEmpty()) {
            repositories = Arrays.asList(args.repositories);
        } else {
            repositories = getRepositories(args.owner);
        }

        List<String> architectures = getSupportedArchitectures(Arrays.asList(args.architectures));
        if(architectures == null || architectures.isEmpty()) {
            System.out.println("No supported architectures selected");
            return;
        }

        download(repositories, Arrays.asList(args.tags), architectures, args.prune);
    }

    void download(List<String> repositories, List<String> tags, List<String> architectures, boolean prune) {
        for (String repository : repositories) {
            List<String> currentTags = tags;
            if(currentTags == null || currentTags.isEmpty()) {
                currentTags = getTags(repository);
            }
            for (String tag : currentTags) {
                for (String architecture : architectures) {
                    download(repository, tag, architecture);
                    if (prune) {
                        prune();
                    }

                    try {
                        System.out.println("Sleep for " + args.sleep + " seconds");
                        Thread.sleep(args.sleep * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }

    abstract public List<String> getSupportedArchitectures(List<String> allArchitectures);

    abstract public List<String> getRepositories(String user);

    abstract public List<String> getTags(String repositoryName);

    abstract public void download(String repository, String tag, String architecture);

    abstract public void prune();
}