# openEQUELLA Docker

## Overview

Included in this directory are two main setups using Docker. The first (alongside this README) is
related to the building of a docker image used for running openEQUELLA in Docker. The second is
the building of a Docker image which has everything you need to build openEQUELLA and even run
its end-to-end tests.

It is assumed for both you have a working local Docker install. Please follow the documentation
online for your operating system.

There is also effectively a third setup two, this assists in running a openEQUELLA cluster using
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
3. Follow the instructions further down to do it with docker.

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

## Docker Image: Building openEQUELLA in Docker

In the `docker-build` directory there are three `Dockerfile`s representing two broad ways of
building openEQUELLA:

- Artefact only build (`Dockerfile-oraclejdk` and `Dockerfile-openjdk-*`)
- Full build with support for execute of test suite (`Dockerfile`)

### Artefact only build

Pulls the latest openEQUELLA repo, sets up the environment, and copies over the helper scripts to
build and save the openEQUELLA installer and upgrader. While there is an Oracle JDK version of the
docker build file, you need to ensure you're within the bounds of the Oracle JDK licensing terms and
conditions. All examples will use the openJDK technology so as to avoid the licensing issues.

```sh
$ cd docker/docker-build
$ docker build -t apereo/oeq-builder -f Dockerfile-openjdk-2018.2 .

$ docker run -it --name oeqbuilder -v /home/user/temp/oeqbuilder-ws:/artifacts apereo/oeq-builder
```

Build the upgrader and save it to the host directory

```sh
cd /home/equella
sh build-upgrader.sh
sh move-upgrader-to-host.sh
```

Separately, you can also build the upgrader and save it to the host directory

```sh
cd /home/equella
sh build-installer.sh
sh move-installer-to-host.sh
```

#### Reset the oeqbuilder Container

```sh
cd ~/repo/Equella
rm -r Source/Plugins/Kaltura
git reset --hard
git checkout master
cd ../Equella-Kaltura
git reset --hard
git checkout master
```

#### GZip Error With SBT

Not specifically a Docker issue, but when running SBT, if you hit the error below, in the Docker container, run `cd ~/repo/Equella ; find . -name target -exec rm -r "{}" \; ; cd ~` and retry the SBT command. Solution found on https://github.com/sbt/sbt/issues/3050.

```
Loading project definition from /home/equella/repo/Equella/project
Error wrapping InputStream in GZIPInputStream: java.util.zip.ZipException: Not in GZIP format
    at sbt.ErrorHandling$.translate(ErrorHandling.scala:10)
    ...
```

#### Using a Non-Default Java Signing Cert

Using an `apereo/oeq-builder` image and a Java keystore (keystore.jks), invoke `docker run` with an additional host directory `-v /directory/location/of/java/keystore:/non-default-keystore`. There are several options on how to create and populate the build.conf. Without installing editors onto the docker image, you can add a `build.conf` with the following contents into your host directory containing the keystore, and then in the container, run `cp /non-default-keystore/build.conf /home/equella/repo/Equella/project`.

```sbt
signer {
  keystore = "/non-default-keystore/keystore.jks"
  storePassword = "<storepasswd>"
  keyPassword = "<optional>" # defaults to storePassword
  alias = "<keyalias>"
}
```

See <https://github.com/openequella/openEQUELLA#keystore> for more details.

### Full build

This focuses on the default `Dockerfile` in the `docker-build` directory. The intention being that
the resultant image can be used for completely fresh builds and also the execution of the end-to-end
tests. This could also be used for an image to run a CI type build, and so in a similar thought can
also be used to diagnose CI build issues.

It solely uses OpenJDK using the openjdk image as its base. And for the installation of dependent
build tools (SBT and Node/NPM) it relies on wrappers (`./sbt`) and scripts (`nvm` with `.nvmrc`) to
ensure the correct ones are used. Thereby in theory always being in sync with what is currently in
the repository.

As part of the build it clones the openEQUELLA repository and uses the `.nvmrc` to install an
initial version of Node/NVM. However, SBT and it's dependencies will only be installed at the first
execution of `./sbt` - this minimises the image size.

The image has a Postgres server all setup with a database and user matching those in the default
autotest configuration. This assists with being able to run openEQUELLA in the container and then
the end-to-end selenium tests. To support this, Google Chrome and the matching driver is also
installed in the build image. (Note: Due to the way Google does its Chrome releases, we're unable
to pin this to a version, so whatever is the 'stable' version at time of building the docker image
will be used.)

**Note:** The matching chromedriver is installed with the helper script
`/usr/local/bin/install-chromedriver` and the build has it place `chromedriver` at
`/usr/local/bin/chromedriver`. You need to make sure if you wish to run the tests you configure
accordingly.

#### Building the image

Building is very simple. Assuming you'd like to call the image `oeq-full-build` you would:

    docker build -t oeq-full-build .

#### Running the image

To then run the image from the previous build step, you:

    docker run -it --rm oeq-full-build

Take note of the command line options there:

- `-it` provides for an interactive terminal (required to use the provided shell)
- `--rm` completely removes the container when you exit (this is optional, and depends what you're
  doing)

#### Building openEQUELLA

When you connect to the running instance you should find yourself in a directory which is a fresh
`git clone` of the repository. So here you can now execute all the usual commands. To get yourself
an installerZip - which can then be used to start an openEQUELLA and run the tests similar to how
the CI builds do, you'd execute:

    ./sbt installerZip

The first time around this can take a while, as it has no cache for your maven/ivy artefacts and so
you'll see it needs to download _everything_ (including SBT and its dependencies). But just like
your local machine, after that's done it will have them for future runs. (This is something to
consider when using the `--rm` option when running the image.)

#### Running tests

Mainly you would refer to how the CI scripts run the tests, but essentially you need to take the
steps:

1. Make sure you have environment variables setup - especially pointing to the configuration file
2. Use the `autotest` tasks to setup a local openEQUELLA with the installer ZIP you built
3. Run the tests

To set up the minimum of environment variable you'd consider doing:

```bash
export AUTOTEST_CONFIG=autotest/codebuild.conf  # set the configuration file to control things
export EQ_EXIFTOOL_PATH=/usr/bin/exiftool
export OLD_TEST_NEWUI=true                      # true if you want the tests in New UI mode
```

(You may want to modify the config file to where is says the chrome driver is.)

Next to utilise the `autotest` project tasks to establish an environment:

    ./sbt "project autotest" installEquella startEquella configureInstall setupForTests

Assuming that all passed, then you can run tests with:

    ./sbt "project autotest" Tests/test Tests/Serial/test OldTests/test

## Docker Compose: Running an openEQUELLA cluster in Docker

First you will need an openEQUELLA installer ZIP. Please following the instructions earlier in this
guide.

Next, a couple of pre-requisites:

- You need two directories in the `docker` directory of your git clone. Make sure the user docker
  will be running as has write permissions. They are:
  - `filestore`; and
  - `traefik.toml`
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

### Updating

If you have a new oEQ installer, you can run `docker compose up -d --force-recreate --build`

### Changing the size of the cluster

You can also specify the number of oEQ instances by running `docker compose up -d --scale oeq=3`,
here `oeq` is the service name defined in the `docker-compose.yml` file.

## Future

For ideas on how to enhance docker with openEQUELLA, please review the
(GitHub issues)[https://github.com/openequella/openEQUELLA/issues?q=is%3Aissue+docker+label%3ADocker]
with the `docker` label.
