import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

libraryDependencies ++= Seq(
  "commons-fileupload"   % "commons-fileupload" % "1.5",
  "com.google.guava"     % "guava"              % "33.4.8-jre",
  "org.antlr"            % "stringtemplate"     % "3.2.1",
  "com.google.code.gson" % "gson"               % "2.13.1",
  "org.slf4j"            % "jcl-over-slf4j"     % "2.0.17",
  "commons-io"           % "commons-io"         % "2.19.0",
  log4j,
  log4jCore,
  log4jSlf4jImpl,
  "commons-daemon" % "commons-daemon" % "1.4.1",
  "commons-codec"  % "commons-codec"  % "1.18.0",
  jacksonDataBind,
  jacksonDataFormatYaml
)

(assembly / assemblyMergeStrategy) := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

(assembly / assemblyOption) := (assembly / assemblyOption).value.withIncludeScala(false)

packageOptions += ManifestAttributes(Attributes.Name.CLASS_PATH -> ".")
