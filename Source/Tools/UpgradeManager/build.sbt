import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

libraryDependencies ++= Seq(
  "commons-fileupload"   % "commons-fileupload" % "1.4",
  "com.google.guava"     % "guava"              % "31.0.1-jre",
  "org.antlr"            % "stringtemplate"     % "3.2.1",
  "com.google.code.gson" % "gson"               % "2.8.9",
  "org.slf4j"            % "jcl-over-slf4j"     % "1.7.33",
  "commons-io"           % "commons-io"         % "2.11.0",
  log4j,
  log4jSlf4jImpl,
  "commons-daemon" % "commons-daemon" % "1.2.4",
  "commons-codec"  % "commons-codec"  % "1.15"
)

(assembly / assemblyOption) := (assembly / assemblyOption).value.copy(includeScala = false)

packageOptions += ManifestAttributes(Attributes.Name.CLASS_PATH -> ".")
