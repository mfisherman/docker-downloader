# Docker Downloader

[Docker Hub](https://hub.docker.com) will enforce a retention policy by mid of 2021.
An image of a free user will be deleted if said images has not downloaded for six months. This has been outlined for example in [this blog post](https://www.docker.com/blog/docker-hub-image-retention-policy-delayed-and-subscription-updates/).
However, free users can download up to 200 images in a six hour period.
Wouldn't it be great if there would be an easy tool that would simply download all the images hosted on Docker Hub?
This is exactly what this project is about: it


## Getting Started

These instructions will give you a copy of the project up and running on
your local machine for development and testing purposes.
The project is not meant to be deployed in any production system.


### Prerequisites

Requirements for the software and other tools to build, test and push 
- Java JDK 11
- Maven
- Optional: IntelliJ


### Installing

- Clone the git repository: `git clone https://github.com/mfisherman/docker-downloader.git`
- Build the project with Maven: `cd docker-downloader && mvn clean compile assembly:single`
- The .jar file is built in the `target` folder: `/target/docker-downloader-1.0-SNAPSHOT-jar-with-dependencies.jar`

Optional, you can open the project using IntelliJ

### Command Line Arguments

There are a few command line arguments that are required. The most important ones are:

- `m` or `-method` (required): there are two ways to download the images: using docker (to download the images) or pure HTTPS requests (to download the manifest files). It is recommended to use `manifest`.
- `-o` or `-owner` (optional): the owner of the repositories that you want to download (e.g. mfisherman)
- `-r` or `-repositories` (optional): the repository you want to download (e.g. mfisherman/texlive). Either the owner or the repositories argument must be given
- `-a` or `-architectures` (optional): the architectures of the images you want to download
- `t` or `tags` (optional): the tags you want to download
- `u` or `username` (optional/required): if the method `manifest`is selected, you must provide the username to download the image
- `p`or `password` (optional/required): if the method `manifest` is selected, you must provide the password for the user to download the image
- `s` or `sleep` (optional): the sleep time between two downloads


### Example

The following downloads all images (to be precise: their manifest) of the owner mfisherman using the user with the name mfisherman.

`java -jar target/docker-downloader-1.0-SNAPSHOT-jar-with-dependencies.jar  -owner=mfisherman -username=mfisherman -password=**** -method=manifest -sleep=30`


## Running the tests

There are no tests - sorry for that.


## Deployment

The project is not meant to be deployed to any production system.


## Contributing

Open a bug ticket or a pull request if you want to contribute in any way.


## Versioning

No versioning needed so far.


## License

This project is licensed under the [Aladdin Free Public License (AFPL)](LICENSE).
See the [LICENSE](LICENSE) file for details.


