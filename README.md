# EQUELLA

[![Travis build](https://travis-ci.org/equella/Equella.svg?branch=master)](https://travis-ci.org/equella/Equella)

Builds for each openEQUELLA Release can be found on the [Releases page](https://github.com/equella/Equella/releases "EQUELLA Releases") 

More in-depth documentation can be found in [Docs](https://equella.github.io/) repo.

If you would like to contribute to EQUELLA please setup a [development environment](CONTRIBUTING.md).

# Building EQUELLA from source

* [Download required software](#download-required-software)
* [Get the code](#get-the-code)
* [Build installer](#building-the-installer)

## Download required software

**Download and install Git**

<https://git-scm.com/downloads>

In ubuntu:

```
~$ sudo apt-get install git
```

**SSH**

This guide assumes you have SSH capabilities. Be sure to add your public SSH key into the you git profile to access the code repos.

**Download and install SBT**

<http://www.scala-sbt.org/>

In ubuntu:

```
~$ echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
~$ sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
~$ sudo apt-get update
~$ sudo apt-get install sbt
```

**Install yarn**

<https://yarnpkg.com/lang/en/docs/install/>

In ubuntu (17.10):

```sh
~$ curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -
~$ echo "deb https://dl.yarnpkg.com/debian/ stable main" | sudo tee /etc/apt/sources.list.d/yarn.list
~$ sudo apt-get update && sudo apt-get install yarn
``` 

As of the time of writing the build was tested with yarn 1.3.2 and Node v6.11.4.

**Install psc-package**

<https://github.com/purescript/psc-package>

Binary releases for linux/windows/mac can be found [here](https://github.com/purescript/psc-package/releases).
The binary file must be put onto your PATH somewhere.

Latest release tested was 0.3.3.

**Download and install Java 8 JDK**

<http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html>

Oracle is the recommended and historically the supported vendor of Java to run Equella with.

In ubuntu:

```
~$ sudo add-apt-repository ppa:webupd8team/java
~$ sudo apt-get update
~$ sudo apt-get install oracle-java8-installer
```

**Download and install Image Magick binaries**

<http://www.imagemagick.org/script/binary-releases.php>

_Note: For ubuntu follow the install from Unix Source instructions:_
<https://www.imagemagick.org/script/install-source.php>

To confirm the installation directory in Ubuntu for the Equella installer, run the command:

```
~$ whereis convert
```

When installing in Windows, check “Install Legacy Utilities (e.g. convert)”.

**Download and install libav**

In ubuntu:

```
~$ sudo apt-get install libav-tools
```

To confirm the installation directory in Ubuntu for the Equella installer, run the command:

```
~$ whereis avconv
```

Once SBT and Java are installed, you may need to set a JAVA_HOME environment variable.

**Database**

* Either [PostgreSQL](https://www.postgresql.org/), SQLServer, or Oracle database.

## Get the code

### Base code

**Git Clone**

```
~$ git clone git@github.com:equella/Equella.git
```

### Optional code

There is functionality that could not be included into the core Equella code repository, but based on your business needs, may be appropriate to include.

* Oracle DB Driver
* [Kaltura](https://github.com/equella/Equella-Kaltura)

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

## Building the installer

This guide runs sbt in non-interactive mode. You can run in interactive mode to save rebuild time by first running 'sbt', and the another command such as 'compile'.

```bash
cd to the {Equella repo} directory
~$ sbt installerZip
```
