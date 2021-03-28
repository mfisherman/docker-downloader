package ch.mfisherman.docker.downloader.image;

import ch.mfisherman.docker.downloader.CommandLineArgs;
import ch.mfisherman.docker.downloader.DockerDownloader;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ImageDownloader extends DockerDownloader {

    private static final String API_VERSION = "v1";

    private final DockerClient dockerClient;

    public ImageDownloader(CommandLineArgs args) {
        super(args);
        dockerClient = DockerClientBuilder.getInstance().build();
    }

    public List<String> getRepositories(String user) {
        List<SearchItem> items = dockerClient.searchImagesCmd(user).exec();
        return items.stream().map(SearchItem::getName).collect(Collectors.toList());
    }

    public List<String> getTags(String repositoryName) {

        HttpClient client = HttpClient.newHttpClient();
        String DOCKER_REGISTRY = "https://registry.hub.docker.com";
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(DOCKER_REGISTRY + "/" + API_VERSION + "/repositories/" + repositoryName + "/tags"))
                .header("accept", "application/json")
                .build();

        try {
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
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void download(String repository, String tag, String platform) {
        try {
            System.out.println("Download: repository=" + repository + ", tag=" + tag + ", platform=" + platform);
            dockerClient.pullImageCmd(repository).withTag(tag).withPlatform(platform).exec(new PullImageResultCallback())
                    .awaitCompletion(10, TimeUnit.SECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void prune() {
        System.out.println("Prune images");
        dockerClient.pruneCmd(PruneType.IMAGES);
    }

    static class DockerTag {
        String layer;
        String name;
    }
}