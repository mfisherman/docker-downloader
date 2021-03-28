package ch.mfisherman.docker.downloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.List;

public class CommandLineArgs {
    private static final String ARG_USER_SHORT = "u";
    private static final String ARG_USER = "user";
    private static final String ARG_REPOSITORIES_SHORT = "r";
    private static final String ARG_REPOSITORIES = "repositories";
    private static final String ARG_PLATFORMS_SHORT = "p";
    private static final String ARG_PLATFORMS = "platforms";
    private static final String ARG_TAGS_SHORT = "t";
    private static final String ARG_TAGS = "tags";
    private static final String ARG_PRUNE_SHORT = "c";
    private static final String ARG_PRUNE = "cleanup";

    private static final String[] DEFAULT_PLATFORMS = {
            "linux/386",
            "linux/amd64",
            "linux/arm/v6",
            "linux/arm/v7",
            "linux/arm64"};

    final String user;
    final String[] repositories;
    final String[] platforms;
    final String[] tags;
    final boolean prune = true;

    public CommandLineArgs(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(ARG_USER_SHORT, ARG_USER, true, "the user or publisher name");
        options.addOption(ARG_REPOSITORIES_SHORT, ARG_REPOSITORIES, true, "the repository name");
        options.addOption(ARG_TAGS_SHORT, ARG_TAGS, true, "the tag of the image");
        options.addOption(ARG_PLATFORMS_SHORT, ARG_PLATFORMS, true, "the platform of the image");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(ARG_USER) && cmd.hasOption(ARG_REPOSITORIES)) {
            System.out.println("Please provide either the user or the repository, but not both arguments");
            throw new IllegalArgumentException();
        }

        if (!(cmd.hasOption(ARG_USER) || cmd.hasOption(ARG_REPOSITORIES))) {
            System.out.println("Please provide either the user or the repository");
            throw new IllegalArgumentException();
        }

        user = cmd.getOptionValue(ARG_USER);
        repositories = cmd.getOptionValues(ARG_REPOSITORIES);
        platforms = cmd.hasOption(ARG_PLATFORMS) ? cmd.getOptionValues(ARG_PLATFORMS) : DEFAULT_PLATFORMS;
        tags = cmd.getOptionValues(ARG_TAGS);
    }

    public CommandLineArgs(String user, String[] repositories, String[] platforms, String[] tags) {
        this.user = user;
        this.repositories = repositories;
        this.platforms = platforms;
        this.tags = tags;
    }
}