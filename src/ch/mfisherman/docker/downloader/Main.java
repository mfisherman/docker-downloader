package ch.mfisherman.docker.downloader;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.cli.*;

import java.net.URI;
import java.net.http.HttpClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    private static final String ARG_USER_SHORT = "u";
    private static final String ARG_USER = "user";
    private static final String ARG_REPOSITORIES_SHORT = "r";
    private static final String ARG_REPOSITORIES = "repositories";
    private static final String ARG_PLATFORMS_SHORT = "p";
    private static final String ARG_PLATFORMS = "platforms";
    private static final String ARG_TAGS_SHORT = "t";
    private static final String ARG_TAGS = "tags";

    private static final String[] DEFAULT_PLATFORMS = {
            "linux/386",
            "linux/amd64",
            "linux/arm/v6",
            "linux/arm/v7",
            "linux/arm64"};
    private static final String API_VERSION = "v1";

    public static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        options.addOption(ARG_USER_SHORT, ARG_USER, true, "the user or publisher name");
        options.addOption(ARG_REPOSITORIES_SHORT, ARG_REPOSITORIES, true, "the repository name");
        options.addOption(ARG_TAGS_SHORT, ARG_TAGS, true, "the tag of the image");
        options.addOption(ARG_PLATFORMS_SHORT, ARG_PLATFORMS, true, "the platform of the image");

        CommandLine cmd;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (cmd.hasOption(ARG_USER) && cmd.hasOption(ARG_REPOSITORIES)) {
            System.out.println("Please provide either the user or the repository, but not both arguments");
            return null;
        }

        if (!(cmd.hasOption(ARG_USER) || cmd.hasOption(ARG_REPOSITORIES))) {
            System.out.println("Please provide either the user or the repository");
            return null;
        }
        return cmd;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CommandLine cmd = parseArgs(args);
        if (cmd == null) {
            return;
        }

        System.out.println("Start");
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        downloadAndRemoveAll(dockerClient, cmd);
        System.out.println("End");
    }

    private static void downloadAndRemoveAll(DockerClient dockerClient, CommandLine cmd) throws IOException, InterruptedException {
        if (cmd.hasOption(ARG_USER)) {
            downloadAndRemoveAllForUser(dockerClient, cmd, cmd.getOptionValue(ARG_USER));
        } else {
            downloadAndRemoveAllForRepositories(dockerClient, cmd, Arrays.asList(cmd.getOptionValues(ARG_REPOSITORIES)));
        }
    }

    private static void downloadAndRemoveAllForUser(DockerClient dockerClient, CommandLine cmd, String user) throws IOException, InterruptedException {
        List<SearchItem> items = dockerClient.searchImagesCmd(user).exec();
        List<String> repositories = items.stream().map(SearchItem::getName).collect(Collectors.toList());
        downloadAndRemoveAllForRepositories(dockerClient, cmd, repositories);
    }

    private static void downloadAndRemoveAllForRepositories(DockerClient dockerClient, CommandLine cmd, List<String> repositories) throws IOException, InterruptedException {
        for (String repository : repositories) {
            List<String> dockerTags = getTags(repository, cmd);
            if (!dockerTags.isEmpty()) {
                downloadAndPrune(dockerClient, cmd, repository, dockerTags);
            }
        }
    }

    private static List<String> getTags(String repositoryName, CommandLine cmd) throws IOException, InterruptedException {
        if(cmd.hasOption(ARG_TAGS)) {
            return Arrays.asList(cmd.getOptionValues(ARG_TAGS));
        }

        HttpClient client = HttpClient.newHttpClient();
        String DOCKER_REGISTRY = "https://registry.hub.docker.com";
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(DOCKER_REGISTRY + "/" + API_VERSION + "/repositories/" + repositoryName + "/tags"))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<DockerTag>>() {
        }.getType();
        ArrayList<DockerTag> tags = gson.fromJson(response.body(), type);

        List<String> result = new ArrayList<>();
        for (DockerTag tag : tags) {
            result.add(tag.name);
        }
        return result;
    }

    private static void downloadAndPrune(DockerClient dockerClient, CommandLine cmd, String imageName, List<String> tags) {
        for (String tag : tags) {
            String[] platforms = cmd.hasOption(ARG_PLATFORMS) ? cmd.getOptionValues(ARG_PLATFORMS) : Main.DEFAULT_PLATFORMS;
            for (String platform : platforms) {
                 download(dockerClient, imageName, tag, platform);
                 prune(dockerClient);
            }
        }
    }

    private static void download(DockerClient dockerClient, String image, String tag, String platform) {
        try {
            System.out.println("Download: image=" + image + ", tag=" + tag + ", platform=" + platform);
            dockerClient.pullImageCmd(image).withTag(tag).withPlatform(platform).exec(new PullImageResultCallback())
                    .awaitCompletion(10, TimeUnit.SECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void prune(DockerClient dockerClient) {
        System.out.println("Prune images");
        dockerClient.pruneCmd(PruneType.IMAGES);
    }

    static class DockerTag {
        String layer;
        String name;
    }
}