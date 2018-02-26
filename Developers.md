# Setting up a development environment

Note:  There are a couple of changes to the build process, and some discussion on Scala / Java and the frontend that should be understood when working on Equella code - please take a look at this [Google Group thread](https://groups.google.com/a/apereo.org/forum/#!topic/equella-users/bLV_XXQFOTI) and this [issue ticket](https://github.com/equella/Equella/issues/437).  This page will be updated once the React UI code is a bit more solidified.

## IDE
The source isn't tied to a particular IDE, so it should be buildable 
with any IDE which has an SBT integration. Having said that IntelliJ is 
what most developers are using.

### IntelliJ - latest

Due to the enourmous number of projects, when importing into IntelliJ the required memory usage will be higher than the default, so you'll probably need to increase the memory (`Help -> Edit custom VM Options...`). 2048MB is the current recommendation.

You will also need to increase the default maximum memory allocation for SBT when doing the import: (`Build Tools -> SBT -> Maximum Heap size`). Currently 4096MB is recommended.

### Eclipse - Scala IDE 4.6

You must use the [sbteclipse](https://github.com/typesafehub/sbteclipse) plugin to write projects out Eclipse projects 
and you must configure it with the following settings: 

``` sbtshell
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

EclipseKeys.withBundledScalaContainers := false
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.ManagedClasses
EclipseKeys.eclipseOutput := Some("target/scala-2.11/classes/")
```

### Create dev configuration settings

EQUELLA requires a configuration folder (`learningedge-config`) in order to start and there
is an sbt task which will generate configuration files suitable for running with your dev environment:

```bash
sbt prepareDevConfig
```

This will create a configuration in the `{Equella repo}/Dev/learningedge-config` folder which you can
modify for your needs, in particular you will need to configure `hibernate.properties` to point to
the database that you have created for EQUELLA. 

The default admin url will be: `http://localhost:8080/`

### Updating plugin library jars for dev mode

When running the server in dev mode, the server runner doesn't have access to the SBT build information, so it
can't find the jar libraries which some of the plugins require, so an extra SBT task is required to copy the jars
into a known location for the runner. This task is run by the `prepareDevConfig` task too. 

```bash
~$ sbt jpfWriteDevJars
```

### Running SBT task to generate non-java resources

When you build EQUELLA from within IntelliJ, it will only compile Scala/Java sources and copy resources 
from the resource folders, it won't run any of the scripts that generate resoureces 
(such as compile code to Javascript), in order to do this you can run:

```bash
~$ sbt resources
```

### Running a dev server

Ensure you have your `Dev/learningedge-config` setup.

```bash
~$ sbt compile equellaserver/run
```

Alternatively you can run the server from your IDE by running the class:

`com.tle.core.equella.runner.EQUELLAServer`
 
Inside the `Source/Server/equellaserver` project. 

Ensure that your runner settings compiles the whole project before running: 

* IntelliJ - `Before Launch -> Build Project` 

### Running the admin tool

Ensure you have your server running and know it's 

```bash
~$ sbt compile adminTool/run
```

or run `com.tle.client.harness.ClientLauncher` in the `Source/Server/adminTool` project.

### Developing the JS code

In the `Source/Plugins/Core/com.equella.core/js` directory you will find a yarn/npm 
project which compiles Purescript/Typescript/Sass into JS and CSS. Currently there are number 
of separate JS bundles which are generated and you could develope them easier by running a yarn 
"watched build" script. E.g. to develop the settings page:

```sh
~/Source/Plugins/Core/com.equella.core/js$ yarn run dev:settings
```

This will build the javascript bundle to the correct location for running a dev EQUELLA and will 
watch for source changes and re-build if required.

## SBT Notes
The new build uses SBT (very flexible and has a large set of useful plugins available). You can customize pretty much any aspect of your build process using Scala scripts.

Plugins are global to the build but can be turned on/off on a per project basis.

The root build is located in the following files:

* `build.sbt`
* `project/*.scala`
* `project/plugins.sbt`

Located in the "project" folder is a series of SBT AutoPlugins which are responsible for replicating some of what the ant build used to do:

* `JPFScanPlugin` - scanning for plugin projects
* `JPFPlugin` - default folder layout and settings for JPF plugin projects
* `JPFRunnerPlugin` - collecting plugins for deployment or running

The root plugin manually defines the sub-project location and their inter-project dependencies:

* `equellaserver` - contains the server bootstrap code and contains the dependency list for the server, produces the upgrade zip
* `InstallerZip` - produces the installer zip
* `UpgradeZip` - produces the upgrade zip
* `UpgradeInstallation` - the installation upgrader which is part of the upgrade zip
* `UpgradeManager` - the upgrade manager web app
* `conversion` - the conversion service
* `allPlugins` - an aggregate project which can be used for building all the JPF plugins
* `adminTool` - contains the admin console client launcher

### Misc
if you get a deduplicate dependencies on commons logging, SLF4J has a moonlighting jar that 
says it's commons logging.  Use the build.sbt directive of exclude dependencies like the 
adminConsole does.


