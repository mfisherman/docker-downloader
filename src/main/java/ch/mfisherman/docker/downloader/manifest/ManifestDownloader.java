package ch.mfisherman.docker.downloader.manifest;

import ch.mfisherman.docker.downloader.CommandLineArgs;
import ch.mfisherman.docker.downloader.DockerDownloader;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ManifestDownloader extends DockerDownloader {

    final static String DOCKER_HUB_BASE_URL = "https://hub.docker.com/v2/";
    final static String DOCKER_AUTH_BASE_URL = "https://auth.docker.io/";
    final static String DOCKER_REGISTRY_BASE_URL = "https://registry-1.docker.io/v2/";

    final String authToken;
    final DockerHubClient dockerHubClient;
    final DockerAuthClient dockerAuthClient;
    final DockerRegistryClient dockerRegistryClient;
    final String password;
    final String username;

    public ManifestDownloader(CommandLineArgs args) throws Exception {
        super(args);

        Retrofit retrofitDockerHub = new Retrofit.Builder()
                .baseUrl(DOCKER_HUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Retrofit retrofitAuthClient = new Retrofit.Builder()
                .baseUrl(DOCKER_AUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Retrofit retrofitRegistryClient = new Retrofit.Builder()
                .baseUrl(DOCKER_REGISTRY_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dockerHubClient = retrofitDockerHub.create(DockerHubClient.class);
        dockerAuthClient = retrofitAuthClient.create(DockerAuthClient.class);
        dockerRegistryClient = retrofitRegistryClient.create(DockerRegistryClient.class);
        password = args.password;
        username = args.username;

        DockerHubClient.User user = new DockerHubClient.User(username, password);
        CompletableFuture<DockerHubClient.Token> response = dockerHubClient.authenticateUser(user);

        DockerHubClient.Token token = response.get();
        authToken = token.token;
    }

    @Override
    public List<String> getSupportedArchitectures(List<String> allArchitectures) {
        return Collections.singletonList(allArchitectures.get(0));
    }

    public List<String> getRepositories(String user) {
        CompletableFuture<DockerHubClient.Repositories> response = dockerHubClient.getRepositories(user, authToken);
        try {
            DockerHubClient.Repositories repositories = response.get();
            if (repositories.results != null) {
                return repositories.results.stream().map(repository -> repository.namespace + "/" + repository.name).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getTags(String repositoryName) {
        CompletableFuture<DockerHubClient.Tags> response = dockerHubClient.getTags(repositoryName, authToken);
        try {
            DockerHubClient.Tags tags = response.get();
            if (tags.results != null) {
                return tags.results.stream().map(tag -> tag.name).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void download(String repository, String tag, String architecture) {
        CompletableFuture<DockerAuthClient.Token> authResponse = dockerAuthClient.getToken("registry.docker.io",
                "repository:" + repository + ":pull",
                "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));

        try {
            String token = authResponse.get().token;

            CompletableFuture<DockerRegistryClient.Manifest> downloadResponse =
                    dockerRegistryClient.getManifest(repository, tag, "Bearer " + token);

            DockerRegistryClient.Manifest manifest = downloadResponse.get();
            System.out.println("Downloaded manifest: " + manifest.name + ":" + manifest.tag + " (" + manifest.architecture + ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prune() {
        // No operation needed.
    }

    public interface DockerHubClient {
        @POST("users/login")
        CompletableFuture<Token> authenticateUser(@Body User user);

        @GET("repositories/{user}")
        CompletableFuture<Repositories> getRepositories(@Path("user") String user, @Header("Authorization") String token);

        @GET("repositories/{repository}/tags/")
        CompletableFuture<Tags> getTags(@Path("repository") String repository, @Header("Authorization") String token);

        class Token {
            String token;
        }

        class Repositories {
            List<Repository> results;

            static class Repository {
                String user;
                String name;
                String namespace;
                int pull_count;
            }
        }

        class Tags {
            List<Tag> results;

            static class Tag {
                String tag_status;
                String name;
            }
        }

        class User {
            public User(String username, String password) {
                this.username = username;
                this.password = password;
            }

            String username;
            String password;
        }
    }

    public interface DockerAuthClient {
        @GET("token")
        CompletableFuture<Token> getToken(@Query("service") String service, @Query("scope") String scope,
                                                 @Header("Authorization") String baiscAuth);

        class Token {
            String token;
            String access_token;
        }
    }

    public interface DockerRegistryClient {
        @GET("{repository}/manifests/{tag}")
        CompletableFuture<Manifest> getManifest(@Path("repository") String repository, @Path("tag") String tag,
                                                       @Header("Authorization") String bearerToken);

        class Manifest {
            String name;
            String tag;
            String architecture;
        }

    }
}