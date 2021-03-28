package ch.mfisherman.docker.downloader;

import ch.mfisherman.docker.downloader.image.ImageDownloader;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Start");

        CommandLineArgs commandLineArgs = new CommandLineArgs(args);
        DockerDownloader repository = new ImageDownloader(commandLineArgs);
        repository.download();
        System.out.println("End");
    }


}
