package ch.mfisherman.docker.downloader;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        CommandLineArgs commandLineArgs = new CommandLineArgs(args);
        DockerDownloader repository = DockerDownloaderFactory.getDockerDownloader(commandLineArgs);
        repository.download();
        System.out.println("End");
    }
}