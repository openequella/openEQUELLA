import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

libraryDependencies ++= Seq(
  "org.apache.commons"   % "commons-fileupload2-core"  % "2.0.0-M4",
  "org.apache.commons"   % "commons-fileupload2-javax" % "2.0.0-M4",
  "commons-io"           % "commons-io"                % "2.20.0",
  "com.google.guava"     % "guava"                     % "33.4.8-jre",
  "org.antlr"            % "ST4"                       % "4.3.4",
  "com.google.code.gson" % "gson"                      % "2.13.1",
  "org.slf4j"            % "jcl-over-slf4j"            % "2.0.17",
  "commons-io"           % "commons-io"                % "2.20.0",
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
