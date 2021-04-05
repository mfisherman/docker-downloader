package ch.mfisherman.docker.downloader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class CommandLineArgs {
    private static final String ARG_OWNER_SHORT = "o";
    private static final String ARG_OWNER = "owner";
    private static final String ARG_REPOSITORIES_SHORT = "r";
    private static final String ARG_REPOSITORIES = "repositories";
    private static final String ARG_ARCHITECTURES_SHORT = "a";
    private static final String ARG_ARCHITECTURES = "architectures";
    private static final String ARG_TAGS_SHORT = "t";
    private static final String ARG_TAGS = "tags";
    private static final String ARG_USER_NAME = "username";
    private static final String ARG_USER_NAME_SHORT = "u";
    private static final String ARG_PASSWORD = "password";
    private static final String ARG_PASSWORD_SHORT = "p";
    private static final String ARG_METHOD_SHORT = "m";
    private static final String ARG_METHOD = "method";
    private static final String ARG_SLEEP_SHORT = "s";
    private static final String ARG_SLEEP = "sleep";

    private static final String[] DEFAULT_ARCHITECTURES = {
            "linux/386",
            "linux/amd64",
            "linux/arm/v6",
            "linux/arm/v7",
            "linux/arm64"};
    private static final int DEFAULT_SLEEP = 30;

    private static final String METHOD_IMAGE = "image";
    private static final String METHOD_MANIFEST = "manifest";

    public final String owner;
    public final String[] repositories;
    public final String[] architectures;
    public final String[] tags;
    public final boolean prune = true;
    public final String username;
    public final String password;
    public final int sleep;
    public final DockerDownloaderFactory.DockerDownloaderType dockerDownloaderType;

    public CommandLineArgs(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(ARG_OWNER_SHORT, ARG_OWNER, true, "the user or publisher name");
        options.addOption(ARG_REPOSITORIES_SHORT, ARG_REPOSITORIES, true, "the repository name");
        options.addOption(ARG_TAGS_SHORT, ARG_TAGS, true, "the tag of the image");
        options.addOption(ARG_ARCHITECTURES_SHORT, ARG_ARCHITECTURES, true, "the architecture of the image");
        options.addOption(ARG_USER_NAME_SHORT, ARG_USER_NAME, true, "the user name (for login with the ManifestDownloader)");
        options.addOption(ARG_PASSWORD_SHORT, ARG_PASSWORD, true, "the password of the user (for login with the ManifestDownloader)");
        options.addOption(ARG_METHOD_SHORT, ARG_METHOD, true, "which download method to use (either image or manifest)");
        options.addOption(ARG_SLEEP_SHORT, ARG_SLEEP, true, "the sleep time between two downloads (in seconds)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(ARG_OWNER) && cmd.hasOption(ARG_REPOSITORIES)) {
            System.out.println("Please provide either the owner or the repository, but not both arguments");
            throw new IllegalArgumentException();
        }

        if (!(cmd.hasOption(ARG_OWNER) || cmd.hasOption(ARG_REPOSITORIES))) {
            System.out.println("Please provide either the owner or the repository");
            throw new IllegalArgumentException();
        }

        if (METHOD_IMAGE.equals(cmd.getOptionValue(ARG_METHOD))) {
            dockerDownloaderType = DockerDownloaderFactory.DockerDownloaderType.IMAGE_DOWNLOADER;
        } else if (METHOD_MANIFEST.equals(cmd.getOptionValue(ARG_METHOD))) {
            dockerDownloaderType = DockerDownloaderFactory.DockerDownloaderType.MANIFEST_DOWNLOADER;
        } else {
            System.out.println("Please provide which method you want to use (image or manifest)");
            throw new IllegalArgumentException();
        }

        owner = cmd.hasOption(ARG_OWNER) ? cmd.getOptionValue(ARG_OWNER) : "";
        repositories = cmd.hasOption(ARG_REPOSITORIES) ? cmd.getOptionValues(ARG_REPOSITORIES) : new String[] {};
        architectures = cmd.hasOption(ARG_ARCHITECTURES) ? cmd.getOptionValues(ARG_ARCHITECTURES) : DEFAULT_ARCHITECTURES;
        tags = cmd.hasOption(ARG_TAGS) ? cmd.getOptionValues(ARG_TAGS) : new String[]{};
        username = cmd.getOptionValue(ARG_USER_NAME);
        password = cmd.getOptionValue(ARG_PASSWORD);
        sleep = cmd.hasOption(ARG_SLEEP) ? Integer.parseInt(cmd.getOptionValue(ARG_SLEEP)) : DEFAULT_SLEEP;
    }
}