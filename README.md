# openEQUELLA

[![Travis build](https://travis-ci.com/openequella/openEQUELLA.svg?branch=develop)](https://travis-ci.com/openequella/openEQUELLA)

openEQUELLA is a digital repository that provides a single platform to house your teaching and
learning, research, media, and library content.

Builds for each openEQUELLA Release can be found on the [Releases
page](https://github.com/equella/Equella/releases "EQUELLA Releases"). The latest stable versions
(with their changelogs) can also be retrieved from the version server at
<https://version.openequella.net/>.

(NOTE: The current stable version - starting from 2019.1 - is built from `master`, where as active
development is undertaken on the repository's default branch `develop`. Therefore `develop` is
considered the project's 'unstable' branch.)

The project's homepage and documentation can be found at <https://openequella.github.io/>.

If you would like to contribute to openEQUELLA please review the [Contributor
Guidelines](CONTRIBUTING.md) - which also include details of how to get in touch. We welcome pull
requests and issue reports. And if you'd like to assist with documentation, please head on over to
the documentation repository at <https://github.com/equella/equella.github.io>.

Below you'll find further information for developers wishing to work with the source code.

# Building openEQUELLA from source

- [Download required software](#download-required-software)
- [Get the code](#get-the-code)
- [Build installer](#building-the-installer)

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

**Install Node/NPM**

<https://nodejs.org/>

As of the time of writing the build was tested Node v12.16.1 and NPM v6.13.4.

**Download and install Java 8 JDK**

<http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html>

Oracle is the recommended and historically the supported vendor of Java to run openEquella with.

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

To confirm the installation directory in Ubuntu for the openEquella installer, run the command:

```
~$ whereis convert
```

When installing in Windows, check “Install Legacy Utilities (e.g. convert)”.

**Download and install libav**

In ubuntu:

```
~$ sudo apt-get install libav-tools
```

To confirm the installation directory in Ubuntu for the openEquella installer, run the command:

```
~$ whereis avconv
```

Once SBT and Java are installed, you may need to set a JAVA_HOME environment variable.

**Database**

- Either [PostgreSQL](https://www.postgresql.org/), SQLServer, or Oracle database.

## Get the code

### Base code

**Git Clone**

```
~$ git clone git@github.com:openequella/openEQUELLA.git
```

### Optional code

There is functionality that could not be included into the core openEquella code repository, but based on your business needs, may be appropriate to include.

- Oracle DB Driver
- [Kaltura](https://github.com/equella/Equella-Kaltura)

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
\${user.home}/.java/deployment/security/exception.sites

## Building the installer

This guide runs sbt in non-interactive mode. You can run in interactive mode to save rebuild time by first running 'sbt', and the another command such as 'compile'.

```bash
cd to the {Equella repo} directory
~$ sbt installerZip
```
