# New build system

The new build uses SBT which is a build tool which is very flexible
and has a large set of useful plugins available. You can customize
pretty much any aspect of your build process using Scala scripts.

Plugins are global to the build but can be turned on/off on a
per project basis.

The root build is located in the following files:

* `build.sbt`
  * `project/*.scala`
  * `project/plugins.sbt`

Located in the "project" folder is a series of SBT AutoPlugins which are
responsible for replicating some of what the ant build used to do:

* `JPFScanPlugin` - scanning for plugin projects
* `JPFPlugin` - default folder layout and settings for JPF plugin projects
* `JPFRunnerPlugin` - collecting plugins for deployment or running

The root plugin manually defines the sub-project location and their inter-project
dependencies:

* `equellaserver` - contains the server bootstrap code and contains the
dependency list for the server, produces the upgrade zip
* `Installer` - produces the installer zip
* `adminTool` - contains the admin console client launcher
* `UpgradeInstallation` - the installation upgrader which is part of the upgrade zip
* `UpgradeManager` - the upgrade manager web app
* `allPlugins` - an aggregate project which can be used for building all the JPF plugins

## IntelliJ

Due to the enourmous number of projects, when importing into IntelliJ the required memory usage will
be higher than the default, so you'll probably need to increase the memory (`Help -> Edit custom VM Options...`)

You will also need to increase the default maximum memory allocation for SBT when doing the import:
(`Build Tools -> SBT -> Maximum Heap size`)

4096MB should be enough.

## Speeding up the build during dev

If you are editing the build files you can temporarily disable all the non-essential plugins to
speed up your dev/reload/test process by editing (or creating) the `project/build.conf` file to have the setting:

```
plugin.whitelist = []
```

