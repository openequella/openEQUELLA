import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

libraryDependencies ++= Seq(
  "org.apache.commons"   % "commons-fileupload2-core"  % "2.0.0-M5",
  "org.apache.commons"   % "commons-fileupload2-javax" % "2.0.0-M5",
  "commons-io"           % "commons-io"                % "2.22.0",
  "com.google.guava"     % "guava"                     % "33.7.1-jre",
  "org.antlr"            % "ST4"                       % "4.3.4",
  "com.google.code.gson" % "gson"                      % "2.14.0",
  "org.slf4j"            % "jcl-over-slf4j"            % "2.0.18",
  "commons-io"           % "commons-io"                % "2.22.0",
  log4j,
  log4jCore,
  log4jSlf4jImpl,
  "commons-daemon" % "commons-daemon" % "1.6.1",
  "commons-codec"  % "commons-codec"  % "1.22.1",
  jacksonDataBind,
  jacksonDataFormatYaml
)

(assembly / assemblyMergeStrategy) := {
  case "module-info.class" => MergeStrategy.discard
  case x                   =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

(assembly / assemblyOption) := (assembly / assemblyOption).value.withIncludeScala(false)

packageOptions += ManifestAttributes(Attributes.Name.CLASS_PATH -> ".")
