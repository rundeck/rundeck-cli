# Docker Image Build

# How to

## Build Docker image

From source root perform:

```shell
./gradlew :docker:dockerBuild
```

This will build `rundeck/cli:latest` and `rundeck/cli:$VERSION` image tags.

## Run docker image

Run the image

```shell
docker run -it rundeck/cli --help
```