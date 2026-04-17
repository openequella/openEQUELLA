# openEQUELLA Docker

## Overview

This directory contains two important Docker setups:
1. The `Dockerfile` that takes a pre-built installer ZIP and produces a Docker image for running
openEQUELLA.
2. The `docker-build/` directory contains Dockerfiles, helper scripts, and [README.md](docker-build/README.md) for building openEQUELLA inside Docker —
including artefact-only builds and full builds with end-to-end test support.

It is assumed for both you have a working local Docker install. Please follow the documentation
online for your operating system.

There is also effectively a third setup too, this assists in running an openEQUELLA cluster using
Docker Compose. To use this, you will also need to have installed Docker Compose.

## Docker Image: openEQUELLA in Docker

First you'll be guided through the building of an image, follow that guidance on running the image
directly with `docker run`.

### Obtain an installer ZIP

To build the docker image which runs openEQUELLA, you first need a copy of the installer for the
version you wish to run in the image. This is typically done by getting access to a ZIP of the
installer.

There are three main ways to get an installer ZIP:

1. Download for the [releases page](https://github.com/openequella/openEQUELLA/releases) on GitHub;
2. Build one from source (i.e. using `./sbt installerZip` from the root directory); or
3. Follow the instructions in the [docker-build/README.md](docker-build/README.md) to do it with
   Docker.

Once that is complete, copy the resultant installer ZIP so that it is alongside the `Dockerfile`
and give it a simple name like `installer.zip`.

### Building

Starting with an installer ZIP, it installs openEQUELLA and is ready to run the application. When
the container starts, it keys off of configurable properties such as DB host/name/username/password,
admin url, etc.

This can be used as a quickstart to use openEQUELLA (not vetted for production use yet). And you'll
see there are also docker compose files showing how you can use it to spin up an openEQUELLA
cluster.

To build the image:

```sh
$ docker build -t openequella/openequella:<version> . --build-arg OEQ_INSTALL_FILE=installer.zip
```

Then to run the image:

```sh
$ docker run -t --name oeq \
    -e EQ_HTTP_PORT=8080 \
    -e EQ_ADMIN_URL=http://172.17.0.2:8080/admin/ \
    -e EQ_HIBERNATE_CONNECTION_URL=jdbc:postgresql://your-db-host-here:5432/eqdocker \
    -e EQ_HIBERNATE_CONNECTION_USERNAME=equellauser \
    -e EQ_HIBERNATE_CONNECTION_PASSWORD="your-db-pw-here" \
    openequella/openequella:<version>
```

NOTE: In the above `:<version>` can be omitted and docker will automatically use the `latest` tag.

To access the terminal of the running container:

```sh
docker exec -it oeq /bin/bash
```

## Docker Compose: Running an openEQUELLA cluster in Docker

First you will need an openEQUELLA installer ZIP. Please follow the instructions earlier in this
guide.

Next, a couple of pre-requisites:

- You need one directory in the `docker` directory of your git clone. Make sure the user docker
  will be running as has write permissions:
  - `filestore`
- Additionally, you will need to add an entry to your `/etc/hosts` file pointing `127.0.0.1` to
  `oeq.localhost`

After that, it's time to start the cluster:

- From the root of your git clone, copy the installer zip from `Installer/target` to the `docker`
  folder with the name `installer.zip`
- Change into the `docker` folder and run `docker compose up`
- (optional) Run `docker compose logs | grep 'ClusterMessagingServiceImpl'` and you should expect to
  see `[ClusterMessagingServiceImpl] Successful connection from NODE: xxxx (a string in UUID format)`
- Open <http://oeq.localhost/admin/> from a browser where you'll  have to complete the installation.
  (NOTE: the trailing backslash is key.)
- Following that you can go to the Administer server page and then open Health check where you
  should expect to see a table which lists all node IDs. By default, there is only one, but below
  you can find instructions for increasing the number.
- (optional) Access the Traefik dashboard at <http://localhost:8081/dashboard/> to monitor routing
  and load balancing. (NOTE: the trailing slash is required.)

### Updating

If you have a new oEQ installer, you can run `docker compose up -d --force-recreate --build`

### Changing the size of the cluster

You can also specify the number of oEQ instances by running `docker compose up -d --scale oeq=3`,
here `oeq` is the service name defined in the `docker-compose.yml` file.

## Future

For ideas on how to enhance docker with openEQUELLA, please review the
[GitHub issues](https://github.com/openequella/openEQUELLA/issues?q=is%3Aissue+docker+label%3ADocker)
with the `docker` label.
