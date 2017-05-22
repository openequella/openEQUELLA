![Travis build](https://travis-ci.org/equella/Equella.svg?branch=master)

# EQUELLA

## Building

### Prerequisites

* [SBT](http://www.scala-sbt.org/)
* JDK 8
* `equella-deps.zip` unzipped into your home directory


## Build configuration

Some aspects of the build can be configured by editing the `build.conf` file.

### Keystore

A keystore with a certificate is required to sign some of the jars in order for them to escape the Java sandbox.

By default the build will generate a self signed key which will show security warnings when launching.
In order to prevent this you will need to have a properly [signed certificate](https://www.digicert.com/code-signing/java-code-signing-guide.htm) and configure the build to use it.
In the `build.conf` file you can modify the parameters to configure your own keystore:

```conf
signer {
  keystore = "/path/to/.keystore"
  storePassword = "<storepasswd>"
  keyPassword = "<optional>" # defaults to storePassword
  alias = "<keyalias>"
}
```

**IMPORTANT**: A self registered certificate implies that the jars won't be secured and a security exception will appear when trying to launch the jars.
To avoid this it is needed to add the domain you want to trust as a security exception in your java configuration.
It can be done with the Java Control Panel or directly adding the domain in a new line in this file:
${user.home}/.java/deployment/security/exception.sites 

### Oracle database support

Download the Oracle JDBC driver relevant to your platform: [Oracle](http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html)

```conf
build.oraclejar = "/home/user/path/to/download/ojdbc6-11.2.0.3.jar"
```



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
unzip Installer/target/equella-installer-{VERSION}.zip -d ~
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
