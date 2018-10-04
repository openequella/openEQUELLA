# openEQUELLA Docker

## Setup
Install Docker.  For example, if you are on Ubuntu, follow the instructions [here|https://docs.docker.com/install/linux/docker-ce/ubuntu/].  

Consider enabling the ability to run docker commands without sudo:
```
sudo usermod -aG docker $USER
```

Check your version of Docker (The install and build images have been tested with Docker `18.06.1-ce`):
```
docker --version
```

## Default Dockerfile
Starting with an installer zip (output from `Dockerfile-build`), it installs openEQUELLA and is ready to run the application.  When the container starts, it keys off of configurable properties such as DB host/name/username/password, admin url, etc.

Meant for automated builds of openEQUELLA, but can be used as a quickstart to use openEQUELLA (not vetted for production use yet).

Note - the zip and the root directory in the zip are not always the same, hence the different env variables.  

```
$ cd docker
$ docker build -t apereo/oeq-install-VER . --build-arg OEQ_INSTALL_FILE=equella-installer-VER.zip --build-arg OEQ_INSTALL_ZIP_ROOT_DIR=equella-installer-VER
$ docker run -t --name oeq -e OEQ_DB_HOST=your-db-host-here -e OEQ_DB_PORT=5432 -e OEQ_DB_NAME=eqdocker -e OEQ_DB_USERNAME=your-db-user-here -e OEQ_DB_PASSWORD="your-db-pw-here" -e OEQ_ADMIN_DOMAIN=172.17.0.2 -e OEQ_ADMIN_PORT=8080 -e OEQ_ADMIN_SUFFIX="admin/" oeq-install-VER
```

## docker-build
Pulls the latest Equella repo, and sets up the environment and helper scripts to build and save the openEQUELLA installer and upgrader.
```
$ cd docker/docker-build
$ docker build -t apereo/oeq-builder -f Dockerfile-oraclejdk . 

$ docker run -it --name oeqbuilder -v /home/user/temp/oeqbuilder-ws:/artifacts apereo/oeq-builder
```
Build the upgrader and save it to the host directory
```
cd /home/equella
sh build-upgrader.sh
sh move-upgrader-to-host.sh
```

Separately, you can also build the upgrader and save it to the host directory
```
cd /home/equella
sh build-installer.sh
sh move-installer-to-host.sh
```

### Reset the oeqbuilder Container
```
cd ~/repo/Equella
rm -r Source/Plugins/Kaltura
git reset --hard
git checkout master
cd ../Equella-Kaltura
git reset --hard
git checkout master
```

### GZip Error With SBT
Not specifically a Docker issue, but when running SBT, if you hit the error below, in the Docker container, run `cd ~/repo/Equella ; find . -name target -exec rm -r "{}" \; ; cd ~` and retry the SBT command.  Solution found on https://github.com/sbt/sbt/issues/3050.
```
Loading project definition from /home/equella/repo/Equella/project
Error wrapping InputStream in GZIPInputStream: java.util.zip.ZipException: Not in GZIP format
    at sbt.ErrorHandling$.translate(ErrorHandling.scala:10)
    ...
```

### Using a Non-Default Java Signing Cert
Using an `apereo/oeq-builder` image and a Java keystore (keystore.jks), invoke `docker run` with an additional host directory `-v /directory/location/of/java/keystore:/non-default-keystore`.  There are several options on how to create and populate the build.conf.  Without installing editors onto the docker image, you can add a `build.conf` with the following contents into your host directory containing the keystore, and then in the container, run `cp /non-default-keystore/build.conf /home/equella/repo/Equella/project`.
```
signer {
  keystore = "/non-default-keystore/keystore.jks"
  storePassword = "<storepasswd>"
  keyPassword = "<optional>" # defaults to storePassword
  alias = "<keyalias>"
}
```

See https://github.com/equella/Equella#keystore for more details.


## Future
It would be helpful to have a flag for the default Dockerfile to place the filestore on a host directory.

It would be helpful to have a pre-installed openEQUELLA image of each major version get pushed to DockerHub under Apereo, as well as aim for a Production ready Docker image. 

