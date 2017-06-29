# EQUELLA Selenium Tests

Install SBT and one of the drivers for Chrome or Firefox -
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

Install purescript and bower:
```bash
npm install -g purescript
npm install -g bower
```

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

The EQUELLA you are testing must be start with the following properties set:
```
-Dequella.dev=true -Dequella.autotest=true
```

If you install using the sbt tasks in this project those properties will be setup automatically in the `equellaserver-config.sh`
file for you.

In order to run the tests you first need the test institutions which you can install with:
```bash
sbt setupForTests
```

**NOTE**

If you use a pre-existing install of Equella, it's important to note the autotests will
delete and re-create a set of institutions, one of which is the standard default institution 'vanilla'.
**Don't ever run the tests on a production system!**


## Running all tests

The tests are seperated into two projects, one which is ScalaCheck property
tests (`Tests`) and the other are TestNG based (`OldTests`):

```bash
sbt Tests/test OldTests/test
```

The sbt output gives you the results of the ScalaCheck tests and you can read the HTML TestNG report at:
`OldTests/target/testng/index.html`


## Installing from installer zip

You can install a local EQUELLA from an installer zip file if you point to it at `equella-installer-6.4.zip`:


```conf
install.zip = ${HOME}"/equella/Equella/Installer/target/equella-installer-6.4.zip"
```

Run the installer with:

```bash
sbt installEquella
```

By default it will install with an admin url of `http://localhost:8080`.

It will be installed inside the `equella-install` folder. You can run the services scripts
inside the `manager` folder or you can run the `startEquella` and `stopEquella` sbt tasks.

