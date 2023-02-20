import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

libraryDependencies ++= Seq(
  "commons-fileupload"   % "commons-fileupload" % "1.5",
  "com.google.guava"     % "guava"              % "31.1-jre",
  "org.antlr"            % "stringtemplate"     % "3.2.1",
  "com.google.code.gson" % "gson"               % "2.10.1",
  "org.slf4j"            % "jcl-over-slf4j"     % "2.0.6",
  "commons-io"           % "commons-io"         % "2.11.0",
  log4j,
  log4jCore,
  log4jSlf4jImpl,
  "commons-daemon" % "commons-daemon" % "1.3.3",
  "commons-codec"  % "commons-codec"  % "1.15",
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
