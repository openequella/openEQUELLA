# EQUELLA Selenium Tests

Install SBT and one of the drivers for Chrome or Firefox (make sure the appropriate Browser is installed) -
[chromedriver](https://sites.google.com/a/chromium.org/chromedriver/) or [geckodriver](https://github.com/mozilla/geckodriver/releases).
Ensure that the path of these drivers is set in the PATH environment variable.

Copy the `config/resources/application.conf.example` to `config/resources/application.conf` and
configure the `server.url` to point to your local EQUELLA server admin pages, for example:

```conf
server.url = "http://localhost:8080/"
server.password = systempassword
```

For Chrome you must also edit `webdriver.chrome.driver` to point to the `chromedriver` binary.


## Support server

Some of the tests require supplementary services which are contained in a Scala/Purescript [project](IntegTester/).
The services are started up as part of the tests, however you will need to have `yarn` installed globally before
the Javascript client side of these services can be built. The support server contains two services, one for simulating an LMS integration and the other a simple service for echoing HTTP requests.

## Setting up for tests

The target of your testing can either be a dev install, a standard install or an installation
created by these autotests.

**NOTE**

If you use a pre-existing install of Equella, it's important to note the autotests will
delete and re-create a set of institutions, one of which is the standard default institution 'vanilla'.
**Don't ever run the tests on a production system!**

### Local or dev installation

The EQUELLA you are testing must have some java VM options set in the `JAVA_OPTS` in `manager/equellaserver-config.sh`) to properly enable autotesting:

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

You can install a local EQUELLA from an installer zip file if you point to it at `equella-installer-{version number}.zip` in application.conf:

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

To manually create this database (assuming in a debian environment - or derivative):

```bash
sudo -u postgres psql
 CREATE DATABASE equellatests;
 CREATE USER equellatests WITH PASSWORD 'password';
 GRANT ALL PRIVILEGES ON equellatests TO equellatests; 
```

You can run the services scripts inside the `manager` folder of `equella-install` or you can run the `startEquella` and `stopEquella` sbt tasks.

**NOTE**

Ensure before starting openEQUELLA that `equella-install/learningedge-config/optional-config.properties` contains the correct `exiftool.path` and the `libav.path` so that the tests that 
involve them run properly. LibAV has been replaced with FFMPeg, though you can use symbolic links to map the functions together. 

```bash
sudo ln -s /usr/bin/ffprobe /usr/bin/avprobe
sudo ln -s /usr/bin/ffplay /usr/bin/avplay
sudo ln -s /usr/bin/ffmpeg /usr/bin/avconv
```

### Install configuration and test institution importing

You can run an SBT task to configure the install options:

```bash
sbt configureInstall
```

This will set the server administration password and initialise the default schema.
If you have already done this step manually on your own install just make sure that the server admin password is set to the same thing
as `server.password` is set to, and skip this step. Running this command after setting the admin password and default schema will simply error out.

In order to run the tests you first need the test institutions which you can install with the following command:

```bash
sbt setupForTests
```

## Running all tests

Before running the tests, ensure that the `application.conf` file contains the following:

```conf
tests {
  suitenames = ["testng-codebuild.yaml"]
  parallel = true
}
```
This will set the TestNG suite to be the same as is run on the AWS CodeBuild server.

The tests are seperated into two projects, one which is ScalaCheck property
tests (`Tests`) and the other are TestNG based (`OldTests`):

```bash
sbt Tests/test OldTests/test
```

The sbt output gives you the results of the ScalaCheck tests and you can read the HTML TestNG report at:
`OldTests/target/testng/index.html`

You can expect the autotests to run for at least 30-45 minutes, depending on your hardware. It is recommended to not interact with the computer while this is running, as various browser windows will pop up and should not be touched.
If  you don't wish to see the windows popping up, headless mode is available when using either Firefox or Chrome as your browser. To enable this uncomment the corresponding flag in `config/resources/application.conf`

```conf
webdriver {
  chrome {
    driver = ${HOME}"/bin/chromedriver"
    headless = true
  }
  firefox {
//    headless = true
  }
}
```

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
