platformcommon.jar is picked up from this folder by sbt's default `unmanagedBase`, and is the only
thing supplying com.tle.common.* — Check, Pair, PathUtils, Utils, URLUtils, NameValue and
com.tle.common.util.ExecUtils — which around 90 files in this project import.

It is a hand-made Eclipse export, dated 2015, of classes this repository still builds from source in
Platform/Plugins/com.tle.platform.common. Nothing regenerates it, and it has never been updated. It
should be replaced by a dependency on that project, at which point this folder can go.

It used to live in a subfolder named `adminjars`, alongside four further hand-exported jars
supporting the fest-swing admin console tests. Those tests were removed: they were wired into no
suite that anything ran, drove an 11-year-old prebuilt copy of the console, could not run headless,
and fest-swing itself is abandoned.
