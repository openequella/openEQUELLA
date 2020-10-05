val tikaVersion = "1.24.1"

libraryDependencies ++= Seq(
  "org.slf4j"       % "slf4j-api"    % "1.7.30",
  "org.slf4j"       % "slf4j-simple" % "1.7.30",
  "org.apache.tika" % "tika-core"    % tikaVersion,
  "org.apache.tika" % "tika-parsers" % tikaVersion
)

excludeDependencies += "commons-logging" % "commons-logging"
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "cxf", "bus-extensions.txt") => MergeStrategy.first

  // TODO [SpringHib5] needs a deeper review of blueprint.handlers
  // Due to the error: deduplicate: different file contents found in the following:
  //  .../org.apache.cxf/cxf-core/bundles/cxf-core-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-frontend-jaxrs/bundles/cxf-rt-frontend-jaxrs-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-rs-client/bundles/cxf-rt-rs-client-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:META-INF/blueprint.handlers
  case PathList("META-INF", "blueprint.handlers") => MergeStrategy.first

  // OK to do in Java 8 - interesting that the global case for module-info.class didn't pick up the bouncy castle files
  // deduplicate: different file contents found in the following:
  //  .../org.bouncycastle/bcmail-jdk15on/jars/bcmail-jdk15on-1.65.jar:META-INF/versions/9/module-info.class
  //  .../org.bouncycastle/bcpkix-jdk15on/jars/bcpkix-jdk15on-1.65.jar:META-INF/versions/9/module-info.class
  //  .../org.bouncycastle/bcprov-jdk15on/jars/bcprov-jdk15on-1.65.jar:META-INF/versions/9/module-info.class
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first

  // TODO [SpringHib5] needs a deeper review of public-suffix-list.  I want to keep the later one.
  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:mozilla/public-suffix-list.txt
  //  .../org.apache.httpcomponents/httpclient/jars/httpclient-4.5.12.jar:mozilla/public-suffix-list.txt
  // ...
  case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.first

  // OK to do in Java 8
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.tle.conversion.Main")
