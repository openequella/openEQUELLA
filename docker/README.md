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

## 'Default' Dockerfile
Meant for automated builds of openEQUELLA

## docker-build
Starting with a clone of the openEQUELLA repo, build the installerZip and expose it.
TODO...

## docker-install
Starting with an installer zip (output from `Dockerfile-build`), it installs openEQUELLA and is ready to run the application.  When the container starts, it keys off of configurable properties such as DB URL/name/username/password, admin url, etc.

```
$ cd docker-install
$ docker build .
$ docker run -t --name oeq -e OEQ_DB_HOST=your-db-host-here -e OEQ_DB_PORT=5432 -e OEQ_DB_NAME=eqdocker -e OEQ_DB_USERNAME=your-db-user-here -e OEQ_DB_PASSWORD="your-db-pw-here" -e OEQ_ADMIN_DOMAIN=172.17.0.2 -e OEQ_ADMIN_PORT=8080 -e OEQ_ADMIN_SUFFIX="admin/" <image-id>
```


_Note:_ Eventually, it would be helpful to have a pre-installed openEQUELLA image of each major version get pushed to DockerHub under Apereo, as well as aim for a Production ready Docker image. 
