val tikaVersion = "2.9.1"

libraryDependencies ++= Seq(
  "org.slf4j"       % "slf4j-api"                     % "2.0.13",
  "org.slf4j"       % "slf4j-simple"                  % "2.0.13",
  "org.apache.tika" % "tika-core"                     % tikaVersion,
  "org.apache.tika" % "tika-parsers-standard-package" % tikaVersion excludeAll (
    ExclusionRule(organization = "org.apache.logging.log4j"),
    ExclusionRule(organization = "org.bouncycastle")
  )
)

excludeDependencies += "commons-logging" % "commons-logging"
(assembly / assemblyOption)             := (assembly / assemblyOption).value.withIncludeScala(false)
(assembly / assemblyMergeStrategy) := {
  // Three duplicate classes caused by upgrading tika to version 2.
  case PathList("org", "slf4j", "impl", "StaticMDCBinder.class")    => MergeStrategy.first
  case PathList("org", "slf4j", "impl", "StaticLoggerBinder.class") => MergeStrategy.first
  case PathList("org", "slf4j", "impl", "StaticMarkerBinder.class") => MergeStrategy.first

  case PathList("META-INF", "cxf", "bus-extensions.txt") => MergeStrategy.first

  // Due to the error: deduplicate: different file contents found in the following:
  //  .../org.apache.cxf/cxf-core/bundles/cxf-core-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-frontend-jaxrs/bundles/cxf-rt-frontend-jaxrs-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-rs-client/bundles/cxf-rt-rs-client-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:META-INF/blueprint.handlers
  // Different blueprint.handlers may specify different classes.  Using the first one allows testing to pass.
  case PathList("META-INF", "blueprint.handlers") => MergeStrategy.first

  // The idea is to keep the later suffix list.
  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:mozilla/public-suffix-list.txt
  //  .../org.apache.httpcomponents/httpclient/jars/httpclient-4.5.12.jar:mozilla/public-suffix-list.txt
  // ...
  case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.last

  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

(assembly / mainClass) := Some("com.tle.conversion.Main")
