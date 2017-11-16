scalaVersion := "2.11.7"

javacOptions ++= Seq("-source", "1.8")

resourceDirectory in Compile := baseDirectory.value / "resources"

javaSource in Compile := baseDirectory.value / "src"
javaSource in Test := baseDirectory.value / "test"

scalaSource in Compile := baseDirectory.value / "scalasrc"

updateOptions := updateOptions.value.withCachedResolution(true)

unmanagedClasspath in Runtime += (baseDirectory in LocalProject("learningedge_config")).value

val jacksonVersion = "2.4.1"
val axis2Version = "1.6.2"
val TomcatVersion = "8.5.23"
val SwaggerVersion = "1.5.16"

libraryDependencies ++= Seq(
  "axis" % "axis" % "1.4",
  "cglib" % "cglib" % "2.2",
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-base" % jacksonVersion,
  "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % jacksonVersion,
  "com.flickr4java" % "flickr4java" % "2.16" excludeAll (
    ExclusionRule(organization = "org.apache.axis", name = "axis")
    ),
  "com.google.api-client" % "google-api-client" % "1.20.0",
  "com.google.apis" % "google-api-services-books" % "v1-rev72-1.20.0",
  "com.google.apis" % "google-api-services-youtube" % "v3-rev136-1.20.0",
  "com.google.code.findbugs" % "jsr305" % "2.0.3",
  "com.google.code.gson" % "gson" % "1.6",
  "com.google.gdata" % "core" % "1.47.1",
  "com.google.guava" % "guava" % "18.0",
  "com.google.inject" % "guice" % "3.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
  "com.google.inject.extensions" % "guice-spring" % "3.0",
  "com.ibm.icu" % "icu4j" % "4.6.1.1",
  sqlServerDep,
  "com.miglayout" % "miglayout-swing" % "4.2",
  "com.ning" % "async-http-client" % "1.9.31",
  "com.rometools" % "rome" % "1.7.2",

  "io.swagger" % "swagger-core" % SwaggerVersion,
  "io.swagger" % "swagger-annotations" % SwaggerVersion,
  "io.swagger" % "swagger-jaxrs" % SwaggerVersion,

  "com.zaxxer" % "HikariCP" % "2.6.1",
  "commons-beanutils" % "commons-beanutils" % "1.9.3",
  "commons-codec" % "commons-codec" % "1.7",
  "commons-collections" % "commons-collections" % "3.2.1",
  "commons-configuration" % "commons-configuration" % "1.9",
  "commons-daemon" % "commons-daemon" % "1.0.15",
  "commons-discovery" % "commons-discovery" % "0.5",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "commons-io" % "commons-io" % "2.4",
  "commons-lang" % "commons-lang" % "2.6",
  "dom4j" % "dom4j" % "1.6.1",
  "com.github.equella.legacy" % "itunesu-api-java" % "1.7",
  "com.github.equella.legacy" % "mets" % "1.0",
  "com.metamx" % "extendedset" % "1.5.0-mmx",
  "javax.inject" % "javax.inject" % "1",
  "javax.mail" % "mail" % "1.4.3",
  "javax.servlet" % "jstl" % "1.1.2",
  "javax.xml" % "jaxrpc" % "1.1",
  "jdom" % "jdom" % "1.0",
  "joda-time" % "joda-time" % "2.2",
  "com.github.equella.jpf" % "jpf" % "1.0.7",
  "log4j" % "log4j" % "1.2.17",
  "net.oauth.core" % "oauth" % "20100527",
  "net.oauth.core" % "oauth-provider" % "20100527",
  "net.sf.ezmorph" % "ezmorph" % "1.0.4",
  "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15",
  "net.sf.transmorph" % "transmorph" % "3.1.0",
  "org.apache.axis2" % "axis2-kernel" % axis2Version,
  "org.apache.axis2" % "axis2-adb" % axis2Version,
  "org.apache.axis2" % "axis2-transport-http" % axis2Version,
  "org.apache.axis2" % "axis2-transport-local" % axis2Version,
  "org.apache.commons" % "commons-compress" % "1.1",
  "org.apache.curator" % "curator-client" % "2.6.0",
  "org.apache.curator" % "curator-framework" % "2.6.0",
  "org.apache.curator" % "curator-recipes" % "2.6.0",
  "org.apache.cxf" % "cxf-bundle" % "2.7.6" excludeAll(
    ExclusionRule(organization = "org.apache.geronimo.specs"),
    ExclusionRule(organization = "javax.xml.bind"),
    ExclusionRule(organization = "javax.xml.soap"),
    ExclusionRule(organization = "xml-resolver"),
    ExclusionRule(organization = "asm"),
    ExclusionRule(organization = "org.springframework"),
    ExclusionRule(organization = "aopalliance"),
    ExclusionRule(organization = "org.jvnet"),
    ExclusionRule(organization = "antlr"),
    ExclusionRule(organization = "org.apache.xmlbeans"),
    ExclusionRule(organization = "javax.ws.rs"),
    ExclusionRule(organization = "org.codehaus.jettison"),
    ExclusionRule(organization = "org.eclipse.jetty"),
    ExclusionRule(organization = "org.codehaus.jra"),
    ExclusionRule(organization = "rhino"),
    ExclusionRule(organization = "org.mozilla"),
    ExclusionRule(organization = "org.apache.ws.security"),
    ExclusionRule(organization = "org.apache.santuario"),
    ExclusionRule(organization = "org.opensaml"),
    ExclusionRule(organization = "joda-time"),
    ExclusionRule(organization = "com.sun.xml.messaging.saaj"),
    ExclusionRule(organization = "xalan"),
    ExclusionRule(organization = "com.sun.xml.fastinfoset"),
    ExclusionRule(organization = "net.sf.ehcache")
  ),
  "org.apache.httpcomponents" % "httpclient" % "4.3.4",
  "org.apache.httpcomponents" % "httpcore" % "4.3.2",
  "org.apache.lucene" % "lucene-analyzers" % "3.5.0",
  "org.apache.lucene" % "lucene-core" % "3.5.0",
  "org.apache.lucene" % "lucene-queries" % "3.5.0",
  "org.apache.pdfbox" % "pdfbox" % "1.8.7",
  "org.apache.poi" % "poi-ooxml" % "3.9",
  "org.apache.rampart" % "rampart-core" % "1.6.2" excludeAll(
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.rampart" % "rampart-policy" % "1.6.2" excludeAll(
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.rampart" % "rampart-trust" % "1.6.2" excludeAll(
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.struts" % "struts-core" % "1.3.10" excludeAll (
    ExclusionRule(organization = "antlr", name = "antlr")
    ),
  "org.apache.struts" % "struts-extras" % "1.3.10",
  "org.apache.struts" % "struts-taglib" % "1.3.10",
  "org.apache.tika" % "tika-core" % "1.3",
  "org.apache.tika" % "tika-parsers" % "1.3",
  "org.apache.tomcat" % "tomcat-annotations-api" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-api" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-catalina" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-catalina-ha" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-coyote" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-jsp-api" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-juli" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-servlet-api" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-tribes" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-util" % TomcatVersion,
  "org.apache.tomcat" % "tomcat-util-scan" % TomcatVersion,
  "org.apache.ws.commons.axiom" % "axiom-api" % "1.2.13",
  "org.apache.ws.commons.axiom" % "axiom-impl" % "1.2.13",
  "org.apache.ws.security" % "wss4j" % "1.6.2",
  "org.apache.zookeeper" % "zookeeper" % "3.4.6" excludeAll (
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12")
    ),
  "org.bouncycastle" % "bcprov-jdk15on" % "1.51",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "org.codehaus.woodstox" % "stax2-api" % "3.1.3",
  "org.codehaus.woodstox" % "woodstox-core-asl" % "4.2.0",
  "org.codehaus.xfire" % "xfire-aegis" % "1.2.1",
  "org.dspace" % "cql-java" % "1.0",
  //  "org.dspace.oclc" % "oclc-srw" % "1.0.20080328",
  "org.omegat" % "jmyspell-core" % "1.0.0-beta-2",
  "org.eclipse.jetty" % "jetty-util" % "8.1.7.v20120910",
  "org.freemarker" % "freemarker" % "2.3.23",
  "com.github.equella.legacy" % "hurl" % "1.1",
  "org.javassist" % "javassist" % "3.18.2-GA",
  "org.jboss.resteasy" % "resteasy-jackson-provider" % "3.0.10.Final",
  "org.jboss.resteasy" % "resteasy-jaxrs" % "3.0.10.Final",
  "org.jboss.resteasy" % "jaxrs-api" % "3.0.10.Final",
  "org.jsoup" % "jsoup" % "1.6.1",
  xstreamDep,
  "org.opensaml" % "xmltooling" % "1.3.1" excludeAll (
    ExclusionRule(organization = "org.slf4j")
    ),
  "org.ow2.asm" % "asm" % "5.0.3",
  postgresDep,
  "org.scannotation" % "scannotation" % "1.0.3",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-log4j12" % "1.6.1",
  "org.springframework" % "spring-aop" % "2.5.5",
  "org.springframework" % "spring-context" % "2.5.5",
  "org.springframework" % "spring-context-support" % "2.5.5" excludeAll (
    ExclusionRule(organization = "jasperreports", name = "jasperreports")
    ),
  "org.springframework" % "spring-jdbc" % "2.5.5",
  "org.springframework" % "spring-tx" % "2.5.5",
  "org.springframework" % "spring-web" % "2.5.5",
//  "org.springframework" % "spring-webmvc" % "2.5.5" excludeAll (
//    ExclusionRule(organization = "jasperreports", name = "jasperreports")
//    ),
  "stax" % "stax-api" % "1.0.1",
  "taglibs" % "standard" % "1.1.2",
  "tomcat" % "jsp-api" % "5.5.23",
  "com.github.equella.legacy" % "qtiworks-jqtiplus" % "1.0-beta3" excludeAll(
    ExclusionRule(organization = "org.slf4j"),
    ExclusionRule(organization = "ch.qos.logback"),
    ExclusionRule(organization = "net.sf.saxon")
  ),
  "xml-resolver" % "xml-resolver" % "1.2",
  "org.scala-sbt" %% "io" % "1.1.0"
)

dependencyOverrides += "javax.mail" % "mail" % "1.4.3"

excludeDependencies ++= Seq(
  "com.google.guava" % "guava-jdk5",
  "asm" % "asm",
  "javax.servlet" % "servlet-api",
  "org.mortbay.jetty" % "servlet-api",
  "antlr" % "antlr",
  "stax" % "stax-api",
  "xml-apis" % "xml-apis",
  "javax.xml" % "jaxrpc-api",
  "xalan" % "xalan",
  "xerces" % "xercesImpl",
  "javax.xml.stream" % "stax-api",
  "javax.ws.rs" % "jsr311-api",
  "org.apache.ws.commons" % "XmlSchema",
  "org.apache.ws.commons.schema" % "XmlSchema",
  "woodstox" % "wstx-asl",
  "org.codehaus.woodstox" % "wstx-asl",
  "javassist" % "javassist",
  "org.sonatype.sisu.inject" % "cglib",
  "commons-logging" % "commons-logging",
  "velocity" % "velocity",
  "rhino" % "js",
  "org.mozilla" % "rhino",
  "bouncycastle" % "bcprov-jdk15",
  "org.bouncycastle" % "bcprov-jdk15",
  "org.codehaus.jackson" % "jackson-core-asl",
  "org.codehaus.jackson" % "jackson-jaxrs",
  "org.codehaus.jackson" % "jackson-mapper-asl",
  "org.codehaus.jackson" % "jackson-xc",
  "org.apache.geronimo.specs" % "geronimo-javamail_1.4_spec",
  "org.apache.geronimo.specs" % "geronimo-stax-api_1.0_spec"
)

unmanagedJars in Compile ++= oracleDriverJar.value.toSeq.classpath

run := {
  val cp = (fullClasspath in Runtime).value
  val o = ForkOptions(runJVMOptions = Seq(
    "-cp", Path.makeString(cp.files),
    "-Dequella.devmode=true", "-Dequella.autotest=true"
  ))
  Fork.java(o, Seq("com.tle.core.equella.runner.EQUELLAServer"))
}

mainClass in assembly := Some("com.tle.core.equella.runner.EQUELLAServer")

fullClasspath in assembly := (fullClasspath in Compile).value

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "axiom.xml") => MergeStrategy.first
  case PathList("javax", "wsdl", _*) => MergeStrategy.last
  case PathList("com", "ibm", "wsdl", _*) => MergeStrategy.first
  case PathList("org", "apache", "regexp", _*) => MergeStrategy.first
  case PathList("javax", "servlet", "jsp", _*) => MergeStrategy.first
  case PathList("javax", "annotation", _*) => MergeStrategy.first
  case PathList("org", "w3c", "dom", _*) => MergeStrategy.first
  case PathList("META-INF", "mailcap") => MergeStrategy.first
  case PathList("META-INF", "mimetypes.default") => MergeStrategy.first
  case PathList("META-INF", "javamail.charset.map") => MergeStrategy.first
  case PathList("javax", "activation", _*) => MergeStrategy.first
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  case PathList("junit", _*) => MergeStrategy.discard
  case PathList("org", "apache", "axis2", "transport", "http", "util", "ComplexPart.class") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val collectJars = taskKey[Set[File]]("Collect jars")

collectJars := {
  val destDir = target.value / "jars"
  IO.delete(destDir)
  IO.copy((managedClasspath in Compile).value.map(af => (af.data, destDir / af.data.getName)))
}

lazy val allPlugins = LocalProject("allPlugins")
runnerTasks(allPlugins)

upgradeZip := {
  val log = streams.value.log
  val ver = equellaVersion.value
  val outZip: File = target.value / s"tle-upgrade-${ver.majorMinor}.r${ver.commits} (${ver.majorMinor}-${ver.releaseType}).zip"
  val plugVer = ver.fullVersion
  val zipFiles = Seq(
    assembly.value -> "equella-server.jar",
    (assembly in LocalProject("UpgradeInstallation")).value -> "database-upgrader.jar",
    (assembly in LocalProject("conversion")).value -> "conversion-service.jar",
    (versionProperties in LocalProject("equella")).value -> "version.properties"
  )
  val pluginJars = writeJars.value.map(t => (t.file, s"plugins/${t.group}/${t.pluginId}-$plugVer.jar"))
  log.info(s"Creating upgrade zip ${outZip.absolutePath}")
  IO.zip(zipFiles ++ pluginJars, outZip)
  outZip
}

lazy val sourcesForZip = Def.task[Seq[(File, String)]] {
  val baseJavaSrc = (javaSource in Compile).value
  (baseJavaSrc ** "*.java").pair(rebase(baseJavaSrc, ""))
}

writeSourceZip := {
  val outZip = target.value / "equella-sources.zip"
  val allSrcs = sourcesForZip.all(ScopeFilter(inAggregates(allPlugins))).value.flatten
  sLog.value.info(s"Zipping all sources into $outZip")
  IO.zip(allSrcs, outZip)
  outZip
}
