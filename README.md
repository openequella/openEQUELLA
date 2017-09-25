# EQUELLA Selenium Tests

Install SBT and one of the drivers for Chrome or Firefox (make sure the appropriate Browser is installed) -
[chromedriver](https://sites.google.com/a/chromium.org/chromedriver/) or [geckodriver](https://github.com/mozilla/geckodriver/releases).

Copy the `config/resources/application.conf.example` to `config/resources/application.conf` and
configure the `server.url` to point to your local EQUELLA server admin pages, for example:

```conf
server.url = "http://localhost:8080/"
server.password = systempassword
```

For Chrome you must also edit `webdriver.chrome.driver` to point to the `chromedriver` binary.


## Compiling and running the support server

Some of the tests require supplementary services which are contained in a Scala/Purescript project.
In order to build and run this service you need the node package manager installed (npm).

Install purescript, bower and pulp:
```bash
npm install -g purescript bower pulp
```
_Note:_ If you get an error message saying one (or more) of the packages failed to install, check you nodejs and npm versions.  For example, the default repos for Ubuntu (on 2017-07-21) have a bit older nodejs and npm versions, and the install failed.  Upgrading to node.js v6.11.1 and npm v3.10.10 and running with Administrator privileges allowed the install to succeed.  One possible upgrade path is to use a PPA.

Compile and run the support server:
```bash
cd IntegTester/ps
npm install
bower install
npm run build
cd ../../
sbt IntegTester/assembly
java -jar IntegTester/target/scala-2.12/IntegTester-assembly-1.0.jar &
```

## Setting up for tests

The target of your testing can either be a dev install, a standard install or an installation
created by these autotests.

**NOTE**

If you use a pre-existing install of Equella, it's important to note the autotests will
delete and re-create a set of institutions, one of which is the standard default institution 'vanilla'.
**Don't ever run the tests on a production system!**

### Local or dev installation

The EQUELLA you are testing must have some java VM options set in `manager/equellaserver-config.sh JAVA_OPTS`) to properly enable autotesting:
```
-Dequella.autotest=true
```
If you will be creating coverage reports you will also need to add the jacoco coverage agent:

```
-javaagent:{jacocojarpath}=output=tcpserver
```
You can get the path to use by running from the SBT command line:

```sbt
show coverageJar
```

### Installing from installer zip

You can install a local EQUELLA from an installer zip file if you point to it at `equella-installer-6.5.zip` in application.conf:

```conf
install {
  zip = ${HOME}"/equella/Equella/Installer/target/equella-installer-6.4.zip"
  ...
}
```

Run the installer with:

```bash
sbt installEquella
```

By default:
* It will configure an admin url of `http://localhost:8080`.
* It will be installed inside the `equella-install` folder.
* The options for autotesting and coverage will be configured already.
* It will be configured for the Postgres DB `equellatests` at `localhost:5432`, expected a username / password of `equellatests` / `password`.  These details can be changed as desired.

You can run the services scripts inside the `manager` folder of `equella-install` or you can run the `startEquella` and `stopEquella` sbt tasks.

### Install configuration and test institution importing

You can run an SBT task to configure the install options:

```bash
sbt configureInstall
```
This will set the server administration password and initialise the default schema.
If you have already done this step manually on your own install just make sure that the server admin password is set to the same thing
as `server.password` is set to.

In order to run the tests you first need the test institutions which you can install with the following command:
```bash
sbt setupForTests
```

## Running all tests

The tests are seperated into two projects, one which is ScalaCheck property
tests (`Tests`) and the other are TestNG based (`OldTests`):

```bash
sbt Tests/test OldTests/test
```

The sbt output gives you the results of the ScalaCheck tests and you can read the HTML TestNG report at:
`OldTests/target/testng/index.html`

## Creating a coverage report

Provided your installation was setup correct for jacoco coverage data collecting, you can create a HTML
report of the code coverage.

You can optionally provide a source zip location which can provide the report with the ability to click through to the source code
and examine the coverage at the source level. First you must create the source zip using EQUELLA's SBT build:

```bash
sbt writeSourceZip
```

This will print out the path to the written zip file and you can place that in your autotest `application.conf`:

```conf
install.sourcezip = ${HOME}"/equella/Equella/Source/Server/equellaserver/target/equella-sources.zip"
```

To create the actual report you must run:

```bash
sbt coverageReport
```

It will create the report at `target/coverage-report/index.html`.
