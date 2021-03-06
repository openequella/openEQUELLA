val springVersion = "5.3.3"

libraryDependencies ++= Seq(
  "com.google.guava"         % "guava"           % "18.0",
  "com.github.insubstantial" % "flamingo"        % "7.3",
  "com.miglayout"            % "miglayout-swing" % "4.2",
  "org.springframework"      % "spring-web"      % springVersion,
  "org.springframework"      % "spring-aop"      % springVersion
)

dependsOn(platformSwing, LocalProject("com_tle_common_applet"))

packageOptions in assembly += Package.ManifestAttributes(
  "Application-Name"                       -> "EQUELLA File Manager",
  "Permissions"                            -> "all-permissions",
  "Codebase"                               -> "*",
  "Application-Library-Allowable-Codebase" -> "*",
  "Caller-Allowable-Codebase"              -> "*"
)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  // Added due to a [deduplicate: different file contents found in the following] error against:
  // org.springframework/spring-context/jars/spring-context-3.2.18.RELEASE.jar:overview.html
  // org.springframework/spring-web/jars/spring-web-3.2.18.RELEASE.jar:overview.html
  case x if x.contains("overview.html") => MergeStrategy.first
  // Post SpringHib5 upgrade, the following error was thrown on build:
  // deduplicate: different file contents found in the following:
  // [error] .../com.fasterxml/classmate/bundles/classmate-1.5.1.jar:module-info.class
  // [error] .../com.sun.istack/istack-commons-runtime/jars/istack-commons-runtime-3.0.7.jar:module-info.class
  // [error] .../com.sun.xml.fastinfoset/FastInfoset/jars/FastInfoset-1.2.15.jar:module-info.class
  // [error] .../javax.xml.bind/jaxb-api/jars/jaxb-api-2.3.1.jar:module-info.class
  // [error] .../org.glassfish.jaxb/jaxb-runtime/jars/jaxb-runtime-2.3.1.jar:module-info.class
  // [error] .../org.glassfish.jaxb/txw2/jars/txw2-2.3.1.jar:module-info.class
  // [error] .../org.jvnet.staxex/stax-ex/jars/stax-ex-1.8.jar:module-info.class
  // As per https://stackoverflow.com/questions/54834125/sbt-assembly-deduplicate-module-info-class , discarding is OK for Java 8
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
