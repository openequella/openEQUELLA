# openEQUELLA Docker

## Setup

Install Docker. For example, if you are on Ubuntu, follow the instructions [here|https://docs.docker.com/install/linux/docker-ce/ubuntu/].

Consider enabling the ability to run docker commands without sudo:

```sh
sudo usermod -aG docker $USER
```

Check your version of Docker (The install and build images have been tested with Docker `18.06.1-ce`):

```sh
docker --version
```

## Default Dockerfile

Starting with an installer zip (output from `Dockerfile-build`), it installs openEQUELLA and is ready to run the application. When the container starts, it keys off of configurable properties such as DB host/name/username/password, admin url, etc.

Meant for automated builds of openEQUELLA, but can be used as a quickstart to use openEQUELLA (not vetted for production use yet).

Note - the zip and the root directory in the zip are not always the same, hence the different env variables.

```sh
$ cd docker
$ docker build -t apereo/oeq-install-VER . --build-arg OEQ_INSTALL_FILE=equella-installer-VER.zip --build-arg OEQ_INSTALL_ZIP_ROOT_DIR=equella-installer-VER
$ docker run -t --name oeq -e EQ_HTTP_PORT=8080 -e EQ_ADMIN_URL=http://172.17.0.2:8080/admin/ -e EQ_HIBERNATE_CONNECTION_URL=jdbc:postgresql://your-db-host-here:5432/eqdocker -e EQ_HIBERNATE_CONNECTION_USERNAME=equellauser -e EQ_HIBERNATE_CONNECTION_PASSWORD="your-db-pw-here" oeq-install-VER
```

To access the terminal of the container:

```sh
docker exec -it oeq /bin/bash
```

## docker-build

Pulls the latest Equella repo, sets up the environment, and copies over the helper scripts to build and save the openEQUELLA installer and upgrader. While there is an Oracle JDK version of the docker build file, you need to ensure you're within the bounds of the Oracle JDK licensing terms and conditions. All examples will use the openJDK technology so as to avoid the licensing issues.

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

### Reset the oeqbuilder Container

```sh
cd ~/repo/Equella
rm -r Source/Plugins/Kaltura
git reset --hard
git checkout master
cd ../Equella-Kaltura
git reset --hard
git checkout master
```

### GZip Error With SBT

Not specifically a Docker issue, but when running SBT, if you hit the error below, in the Docker container, run `cd ~/repo/Equella ; find . -name target -exec rm -r "{}" \; ; cd ~` and retry the SBT command. Solution found on https://github.com/sbt/sbt/issues/3050.

```
Loading project definition from /home/equella/repo/Equella/project
Error wrapping InputStream in GZIPInputStream: java.util.zip.ZipException: Not in GZIP format
    at sbt.ErrorHandling$.translate(ErrorHandling.scala:10)
    ...
```

### Using a Non-Default Java Signing Cert

Using an `apereo/oeq-builder` image and a Java keystore (keystore.jks), invoke `docker run` with an additional host directory `-v /directory/location/of/java/keystore:/non-default-keystore`. There are several options on how to create and populate the build.conf. Without installing editors onto the docker image, you can add a `build.conf` with the following contents into your host directory containing the keystore, and then in the container, run `cp /non-default-keystore/build.conf /home/equella/repo/Equella/project`.

```sbt
signer {
  keystore = "/non-default-keystore/keystore.jks"
  storePassword = "<storepasswd>"
  keyPassword = "<optional>" # defaults to storePassword
  alias = "<keyalias>"
}
```

See https://github.com/equella/Equella#keystore for more details.

## Future

For ideas on how to enhance docker with openEQUELLA, please review the (GitHub issues)[https://github.com/apereo/openEQUELLA/issues?q=is%3Aissue+docker+label%3ADocker] with the `docker` label.

## Setup a local clustering environment of oEQ

- From the root use SBT to build the installer;

```
sbt installerZip
```

- Copy the installer zip from `Equella/Installer/target` to the docker folder `Equella/docker`
- Run `docker-compose up -d` from `Equella/docker`
- Run docker-compose logs | grep 'ClusterMessagingServiceImpl' and you should expect to see `[ClusterMessagingServiceImpl] Successful connection from NODE: xxxx (a string in UUID format)`
- Open `oeq.localhost/admin/` from a browser, go the oEQ Administer server page and then open Health check, you should expect to see a table which lists all node IDs
- If you have a new oEQ installer, you can run `docker-compose up -d --force-recreate --build`
- You can also specify the number of oEQ instance by running 'docker-compose up -d --scale oeq=3', here `oeq` is the service name defined in the yml file
