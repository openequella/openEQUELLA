# Contributing

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

We welcome contributions via both the raising of issues and submitting pull requests. But before you
do, please take a moment to consult the below.

## Chatting

If before any of the below you'd like to discuss an issue or some code, then come and chat with
us on one of the following platforms:

- [Users mailing list](https://groups.google.com/a/apereo.org/forum/#!forum/equella-users) - Most
  active, best for more usage centric questions
- [Developer mailing list](https://groups.google.com/a/apereo.org/forum/#!forum/equella-dev) - For
  discussing work on the code
- \#Equella channel in [Apereo's slack](https://apereo.slack.com/)

## Submitting Issues

When you submit an issue, please take the time to provide as much info as you can. Key bits are:

1. What operating system
2. What Java version
3. What browser version
4. A concise description of your issue
5. More details of the issue, including any error logs etc.

## Contributing Code / Pull Requests

As per standard Apereo projects: If you wish to contribute to openEQUELLA itself, you should first
subscribe to the openEQUELLA Google Group as outlined above and discuss your contributions with
the other developers. You will also need to sign a
[Contributor License Agreement](https://www.apereo.org/about/governance/licensing).

But after that, as per usual GitHub, please first fork the project and then branch off `develop` or a
stable branch (see below) to use a 'feature' branch. Branches should follow the naming convention of:

    feature/<issue#>-<short_desc>

Or if for a bug, you can choose to:

    bugfix/<issue#>-<short_desc>

For example:

    feature/123-make_coffee

or:

    bugfix/124-fix_burnt_coffee

If you're unsure which to use, default to using `feature/<issue#>-<short_desc>`.

Please ensure you provide [quality commit messages](https://chris.beams.io/posts/git-commit/),
and in the PR please detail what testing you have undertaken. Also, ideally provide some tests as
part of your PR and/or details of any manual tests you performed.

### Git Workflow and Layout

The project is mostly aligned with the [Git Flow
workflow](https://nvie.com/posts/a-successful-git-branching-model/). Each new piece of work has a
new branch created. Code reviews/PRs then take place on this branch, which on completion are merged
back to `develop`.

There are a number of Git tools which provide Git Flow tooling. Most dedicated Git GUI's provide
support (such as SourceTree and GitKraken), but perhaps the simplest approach (assuming you're
comfortable with the Git CLI) is to install the [gitflow git
extension](https://github.com/nvie/gitflow). Most linux distributions provide a package to install
this (recommended).

With Git Flow this essentially means our `develop` branch is our unstable branch where we're working
towards the next version. And `master` is our currently stable branch. We also have historically
stable branches which we use for back porting and have the name format of `stable-<version>` -
e.g. `stable-6.6`.

Historically some branches did not follow that format, so if you're looking for an older
version:

| Version | Branch       |
| ------- | ------------ |
| 6.6     | `stable-6.6` |
| 6.5     | `stable`     |
| 6.4     | `6.4`        |

Subsequent revision releases are done from the matching stable branches. And if suitable,
fixes are merged to other versions - and especially to `master`.

NOTE: There are problems with building 6.4 as it still has references to the old commercial build
environment. Effort was undertaken in 6.5 to remove these linkages.

## Setting up a development environment

Note: There are a couple of changes to the build process, and some discussion on Scala / Java and
the frontend that should be understood when working on openEQUELLA code - please take a look at this
[Google Group
thread](https://groups.google.com/a/apereo.org/forum/#!topic/equella-users/bLV_XXQFOTI) and this
[issue ticket](https://github.com/openequella/openEQUELLA/issues/437). This page will be updated once the
React UI code is a bit more solidified.

**Setup note for Ubuntu 20.04**

Notice these instructions assume a person is starting with a fresh machine.
Therefore there'll be additional information which may seem redundant to someone who already has a significantly established development machine.

```bash
sudo apt update
sudo apt install curl
```

### Install SDKMAN
https://sdkman.io/

We suggest using SDKMAN for installing (and managing multiple versions of) JDK, Scala, Gradle - any other JVM bits.
Please follow the installation instructions provided by the tool.

### Install Java
Since January 2024, Java 21 is required to build openEQUELLA. Temurin is the recommended openJDK distribution.
To install that with SDKMAN:

```bash
sdk install java 21.0.1-tem
```

### Install NVM

https://github.com/nvm-sh/nvm

We suggest using NVM to manage different NodeJS versions.
Please follow the installation instructions provided by the tool.

### Install NodeJS / NPM

openEQUELLA has a `.nvmrc` in the root of its repository, as a result installation of the correct version of NodeJS and NPM is as simple as
running the following when in the root of your openEQUELLA clone:

```
nvm install
```

### Install a local database

To run openEQUELLA locally for development and testing, you'll need a local database. By far the easiest to setup
is PostgreSQL. The two main ways to do this are:

- Simply install the package as part of your distro; or
- (Recommended) run an instance in docker to keep it nice and self contained.

Once you have an instance running, make sure you create a database with a user for access:

    CREATE DATABASE equella;
    CREATE USER equellauser WITH PASSWORD 'password';
    GRANT ALL PRIVILEGES ON DATABASE "equella" to equellauser;

### Install build-essential

https://packages.ubuntu.com/impish/build-essential

It will install everything required for compiling basic software written in C and C++. We do need this to build openEquella.

```bash
sudo apt-get install build-essential
```

### Install Image Magick

A suite of software (and specifically, command line tools) used by openEQUELLA for querying and manipulating
image attachments. A key requirement for when contributing items with image attachments.

```
sudo apt install imagemagick
```

### Install FFmpeg

```
sudo apt install ffmpeg
```

## Build openEquella in a terminal

Make sure everything is setup correctly and openEquella can be built on your machine.

```
cd /path/to/openEquella
```

setup node and npm version

```
nvm use
```

### Pre-commit hook

openEQUELLA provides [a script to set up git pre-commit hooks](https://github.com/typicode/husky) to [format the code](#code-formatters) you have [modified before committing](https://github.com/okonet/lint-staged).
To set it up you must run the installer once (from the root dir):

```bash
npm ci
```

### Create dev configuration settings

openEQUELLA requires a configuration folder (`learningedge-config`) in order to start and there
is an sbt task which will generate configuration files suitable for running with your dev environment:

```bash
./sbt prepareDevConfig
```

This will create a configuration in the `{openEQUELLA repo}/Dev/learningedge-config` folder which you can
modify for your needs, in particular you will need to configure `hibernate.properties` to point to
the database that you have created for openEQUELLA.

The default admin url will be: `http://localhost:8080/`

### Updating plugin library jars for dev mode

When running the server in dev mode, the server runner doesn't have access to the SBT build
information, so it can't find the jar libraries which some of the plugins require, so an extra SBT
task is required to copy the jars into a known location for the runner. This task is run by the
`prepareDevConfig` task too.

```bash
./sbt jpfWriteDevJars
```

### Running SBT task to generate non-java resources

When you build openEQUELLA from within IntelliJ, it will only compile Scala/Java sources and copy
resources from the resource folders, it won't run any of the scripts that generate resoureces (such
as compile code to Javascript), in order to do this you can run:

```bash
./sbt resources
```

### Running a dev server

Ensure you have your `Dev/learningedge-config` setup.

```bash
./sbt compile equellaserver/run
```

Alternatively you can run the server from your IDE by running the class:

`com.tle.core.equella.runner.EQUELLAServer`

Inside the `Source/Server/equellaserver` project.

Ensure that your runner settings compiles the whole project before running:

- IntelliJ - `Before Launch -> Build Project`

### Running the Admin Console

Once you have successfully started the server with the above commands, you can then launch the Admin Console
to manage it. To do so from the command line with SBT, run:

```bash
./sbt compile adminTool/run
```

or run `com.tle.client.harness.ClientLauncher` in the `Source/Server/adminTool` project.

### Building distribution packages

To create the various distribution packages (installer / upgrader):

```bash
./sbt installerZip
```

## Working with the JS and TS code

There are two key parts of JS/TS code in openEQUELLA. There is the code for the New UI which is a
React based SPA; and then there is the legacy JS code which supports AJAX functions when the system
is in Legacy UI mode.

### New UI / React SPA

The New UI consists of two modules:

- The React based SPA located in the `react-front-end` directory; and
- The REST Module which is located in the `oeq-ts-rest-api` directory.

For the most part, if you wish to work on the new UI, you need go into `oeq-ts-rest-api` directory and execute:

```bash
npm ci
npm run build
````

After building the REST module (`oeq-ts-rest-api`), then you can go into the `react-front-end` directory and execute:

```bash
npm ci
npm run dev
```

The `run dev` command will build the React App and place the output into the directory from which
openEQUELLA serves its web resources.

Further to this, you may also wish to utilise the Storybook setup when working on UI  components.
This can be done by:

```bash
npm run storybook
```

Once built, it will launch the endpoint for Storybook into your browser.

Lastly, to run the Jest tests, you can go into either of the modules directories and run:

```bash
npm run test
```

(NOTE: to run the tests for the REST module you need a locally running instance of openEQUELLA with
the required institutions.)

### Legacy UI

In the `Source/Plugins/Core/com.equella.core/resource/web/js` directory you will find the JS code
used by the various Legacy UI pages which have AJAX functionality. Further in
`Source/Plugins/Core/com.equella.core/test/js` you will find the unit test setup for any of the
newer JS files.

### Other

There are also a couple of other places which utilise JS, TS and even Purescript. The main ones
being:

- The support for Swagger UI at `Source/Plugins/Core/com.equella.core/swaggerui`
- The IntegTester used for the integration tests and found at `/autotest/IntegTester/front-end`

## IDE

The source isn't tied to a particular IDE, so it should be buildable
with any IDE which has an SBT integration. Having said that IntelliJ is
what most developers are using.

### IntelliJ - latest

To install it, follow the instructions provided by JetBrains at https://www.jetbrains.com/help/idea/installation-guide.html

Import as an SBT project and use the default settings.

If you get compile errors in the IDE, but standalone `./sbt compile` works, do an sbt refresh from the
IntelliJ `SBT tool window`.

#### Increase the memory heap

Due to the heavy memory requirements of the SBT based build, it is recommended to setup IntelliJ's maximum heap to 4GiB. Documentation on how to do this can be found at <https://www.jetbrains.com/help/idea/increasing-memory-heap.html>.

#### Plugins recommended

In order to speed up the IDE. We recommend only enable relevant plugins.
But make sure you enable the Scala plugin as well as support for Typescript and Gradle (you have Java by default).

Plugins can be configured in File -> settings -> Plugins.

### Eclipse - Scala IDE 4.6

You must use the [sbteclipse](https://github.com/typesafehub/sbteclipse) plug-in to write projects
out Eclipse projects and you must configure it with the following settings:

```sbtshell
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

EclipseKeys.withBundledScalaContainers := false
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.ManagedClasses
EclipseKeys.eclipseOutput := Some("target/scala-2.11/classes/")
```

## Code Formatters

All code should be formatted using the following tools.
Assuming you've followed the above steps to install the pre-commit hooks this will be done automatically for you at commit time.
However, if you don't, then the CI pipelines will fail for your pull requests.

- Scala - [scalafmt](https://scalameta.org/scalafmt/)
- Java - [Google Java Format](https://github.com/google/google-java-format)
- Typescript/JS/Markdown/YAML + more... - [Prettier](https://prettier.io/)

Each formatter has various IDE plugins, in particular IntelliJ is well supported:

- [Google Java Format](https://plugins.jetbrains.com/plugin/8527-google-java-format)
- scalafmt is built in
- [Prettier](https://plugins.jetbrains.com/plugin/10456-prettier)

## Build configuration

Some aspects of the build can be configured by editing the `build.conf` file. This is typically though only done for CI builds.

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

## SBT Notes

The new build uses SBT (very flexible and has a large set of useful plug-ins available). You can
customize pretty much any aspect of your build process using Scala scripts.

Plug-ins are global to the build but can be turned on/off on a per project basis.

The root build is located in the following files:

- `build.sbt`
- `project/*.scala`
- `project/plugins.sbt`

Located in the "project" folder is a series of SBT AutoPlugins which are responsible for replicating
some of what the ant build used to do:

- `JPFScanPlugin` - scanning for plug-in projects
- `JPFPlugin` - default folder layout and settings for JPF plug-in projects
- `JPFRunnerPlugin` - collecting plug-ins for deployment or running

The root plug-in manually defines the sub-project location and their inter-project dependencies:

- `equellaserver` - contains the server bootstrap code and contains the dependency list for the
  server, produces the upgrade zip
- `InstallerZip` - produces the installer zip
- `UpgradeZip` - produces the upgrade zip
- `UpgradeInstallation` - the installation upgrader which is part of the upgrade zip
- `UpgradeManager` - the upgrade manager web app
- `conversion` - the conversion service
- `allPlugins` - an aggregate project which can be used for building all the JPF plug-ins
- `adminTool` - contains the admin console client launcher

#### Misc

If you get a deduplicate dependencies on commons logging, SLF4J has a moonlighting jar that
says it's commons logging. Use the build.sbt directive of exclude dependencies like the
adminConsole does.
