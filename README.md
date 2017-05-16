# EQUELLA

## Building

### Prerequisites

* [SBT](http://www.scala-sbt.org/)
* JDK 8
* `equella-deps.zip` unzipped into your home directory

### Building

```bash
sbt installerZip
```

### Running a dev instance

Ensure you have your `Dev/learningedge-config` setup.

```bash
sbt compile equellaserver/run
```

## Installing

### Prerequisites

* [libav tools](https://libav.org/)
* [ImageMagick](https://www.imagemagick.org/)
* Either [PostgreSQL](https://www.postgresql.org/), SQLServer or Oracle database.

### Ubuntu pre-install with postgres

```bash
sudo apt install libav-tools imagemagick postgresql-9.5
sudo su postgres -c psql
CREATE USER equellauser WITH PASSWORD '<password>';
CREATE DATABASE equella OWNER equellauser;
```

### Unzip and run installer

Unzip the installer:

```bash
unzip product/equella-installer-{VERSION}.zip -d ~
```

Run the GUI installer:

```bash
cd ~/equella-installer-{VERSION}
java -jar enterprise-install.jar
```

Follow the prompts ensuring you have entered the correct
* JDK directory
* Database type/host/username/password
* Path to imagemagick
* Path to avconv

### Running the manager and starting the app

Firstly if on unix ensure that the manager and scripts are executable:

```bash
cd {install_location}/manager
chmod +x equellaserver manager jsvc
```

Start up the manager:

```bash
cd {install_location}/manager
./manager start
```

Use your browser to login to the EQUELLA manager (default is http://localhost:3000)

From here you can click the "start" button to start the EQUELLA app server. Once it has started you can log in to the server admin using the hostname and port you configured in the installer and finish the installation process.
