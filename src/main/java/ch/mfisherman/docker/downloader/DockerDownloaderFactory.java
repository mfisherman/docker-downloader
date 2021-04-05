package ch.mfisherman.docker.downloader;

import ch.mfisherman.docker.downloader.image.ImageDownloader;
import ch.mfisherman.docker.downloader.manifest.ManifestDownloader;

public class DockerDownloaderFactory {

    enum DockerDownloaderType {
        IMAGE_DOWNLOADER, MANIFEST_DOWNLOADER
    }

    public static DockerDownloader getDockerDownloader(CommandLineArgs args) throws Exception{

        if(DockerDownloaderType.IMAGE_DOWNLOADER.equals(args.dockerDownloaderType)) {
            return new ImageDownloader(args);
        }
        if(DockerDownloaderType.MANIFEST_DOWNLOADER.equals(args.dockerDownloaderType)) {
            return new ManifestDownloader(args);
        }
        throw new IllegalArgumentException();
    }


}
