import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

libraryDependencies ++= Seq(
  "commons-fileupload"   % "commons-fileupload" % "1.5",
  "com.google.guava"     % "guava"              % "32.1.3-jre",
  "org.antlr"            % "stringtemplate"     % "3.2.1",
  "com.google.code.gson" % "gson"               % "2.12.1",
  "org.slf4j"            % "jcl-over-slf4j"     % "2.0.17",
  "commons-io"           % "commons-io"         % "2.16.1",
  log4j,
  log4jCore,
  log4jSlf4jImpl,
  "commons-daemon" % "commons-daemon" % "1.3.4",
  "commons-codec"  % "commons-codec"  % "1.17.0",
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
