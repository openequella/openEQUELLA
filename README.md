# EQUELLA

## Building

### Prerequisites

* [SBT](http://www.scala-sbt.org/)
* JDK 8
* `equella-deps.zip` unzipped into your home directory

**NOTE: KEYSTORE**
A keystore with a certificate is required to sign some of the jars or the build will fail.
In the build.properties.in file you can modify the parameters to configure your own keystore:
    
 * The route to the keystore file, by default the root of the user home folder: `tle.signer.certificate=${user.home}/equella.keystore`
 * Set to false to avoid the self-generation of the keystore:  `tle.signer.generateKeystore=false`
 * The alias: `tle.signer.alias=keystorealias`
 * The keystore pass: `tle.signer.password=keystorepass`
 * The key pass (if any): `tle.signer.keypass=keypass`

There is an option (by default if there is not keystore in the route defined) to self-geneate a keystore file if you don't have one and `tle.signer.generateKeystore` is true.
You can configure the other parameters too in the case you want to set an specifc password, alias, or parameters for your certificate.

**IMPORTANT**: A self registered certificate implies that the jars won't be secured and a security exception will appear when trying to launch the jars.
To avoid this it is needed to add the domain you want to trust as a security exception in your java configuration.
It can be done with the Java Control Panel or directly adding the domain in a new line in this file:
${user.home}/.java/deployment/security/exception.sites 


### Building

```bash
sbt upgradeZip
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

Unzip the installer and copy initial build into it:

```bash
unzip product/equella-{VERSION}-installer-{VERSION}.zip -d ~
cp product/tle-upgrade-{VERSION}.zip ~/equella-{VERSION}-installer-{VERSION}/manager/updates/
```

Run the GUI installer:

```bash
cd ~/equella-{VERSION}-installer-{VERSION}
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
