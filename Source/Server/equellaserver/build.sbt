import Path.rebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

javacOptions ++= Seq("-source", "1.8")

resourceDirectory in Compile := baseDirectory.value / "resources"

javaSource in Compile := baseDirectory.value / "src"
javaSource in Test := baseDirectory.value / "test/java"

scalaSource in Compile := baseDirectory.value / "scalasrc"

updateOptions := updateOptions.value.withCachedResolution(true)

unmanagedClasspath in Runtime += (baseDirectory in LocalProject("learningedge_config")).value

val jacksonVersion   = "2.11.0"
val axis2Version     = "1.6.2"
val TomcatVersion    = "8.5.56"
val SwaggerVersion   = "1.6.1"
val RestEasyVersion  = "3.5.0.Final"
val simpledbaVersion = "0.1.9"
val circeVersion     = "0.12.1"
val jsoupVersion     = "1.13.1"
val sttpVersion      = "1.7.2"
val fs2Version       = "1.0.5"
val jsassVersion     = "5.10.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "co.fs2"                         %% "fs2-io"                        % fs2Version,
  "com.softwaremill.sttp"          %% "core"                          % sttpVersion,
  "com.softwaremill.sttp"          %% "async-http-client-backend-fs2" % sttpVersion,
  "com.softwaremill.sttp"          %% "circe"                         % sttpVersion,
  "io.github.doolse"               %% "simpledba-jdbc"                % simpledbaVersion,
  "io.github.doolse"               %% "simpledba-circe"               % simpledbaVersion,
  "axis"                           % "axis"                           % "1.4",
  "cglib"                          % "cglib"                          % "2.2.2",
  "com.fasterxml.jackson.core"     % "jackson-core"                   % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"        % jacksonVersion,
  "com.fasterxml.jackson.module"   %% "jackson-module-scala"          % jacksonVersion,
  "com.fasterxml.jackson.core"     % "jackson-annotations"            % jacksonVersion,
  "com.fasterxml.jackson.core"     % "jackson-databind"               % jacksonVersion,
  "com.fasterxml.jackson.jaxrs"    % "jackson-jaxrs-base"             % jacksonVersion,
  "com.fasterxml.jackson.jaxrs"    % "jackson-jaxrs-json-provider"    % jacksonVersion,
  "io.bit3"                        % "jsass"                          % jsassVersion,
  "com.flickr4java"                % "flickr4java"                    % "2.16" excludeAll (
    ExclusionRule(organization = "org.apache.axis",
                  name = "axis")
  ),
  "com.google.api-client"        % "google-api-client"           % "1.30.9",
  "com.google.apis"              % "google-api-services-books"   % "v1-rev20200204-1.30.9",
  "com.google.apis"              % "google-api-services-youtube" % "v3-rev20200423-1.30.9",
  "com.google.code.findbugs"     % "jsr305"                      % "2.0.3",
  "com.google.code.gson"         % "gson"                        % "2.8.6",
  "com.google.gdata"             % "core"                        % "1.47.1",
  "com.google.guava"             % "guava"                       % "18.0",
  "com.google.inject"            % "guice"                       % "3.0",
  "com.google.inject.extensions" % "guice-assistedinject"        % "3.0",
  "com.google.inject.extensions" % "guice-spring"                % "3.0",
  "com.ibm.icu"                  % "icu4j"                       % "4.8.2",
  sqlServerDep excludeAll (
    // Conflicts with RESTeasy jakarta.xml.bind-api
    ExclusionRule(organization = "javax.xml.bind")
  ),
  "com.miglayout"             % "miglayout-swing"       % "4.2",
  "com.ning"                  % "async-http-client"     % "1.9.40",
  "com.rometools"             % "rome"                  % "1.13.1",
  "io.swagger"                % "swagger-core"          % SwaggerVersion,
  "io.swagger"                % "swagger-annotations"   % SwaggerVersion,
  "io.swagger"                % "swagger-jaxrs"         % SwaggerVersion,
  "io.swagger"                %% "swagger-scala-module" % "1.0.6",
  "com.zaxxer"                % "HikariCP"              % "2.7.9",
  "commons-beanutils"         % "commons-beanutils"     % "1.9.4",
  "commons-codec"             % "commons-codec"         % "1.14",
  "commons-collections"       % "commons-collections"   % "3.2.2",
  "commons-configuration"     % "commons-configuration" % "1.10",
  "commons-daemon"            % "commons-daemon"        % "1.2.2",
  "commons-discovery"         % "commons-discovery"     % "0.5",
  "commons-httpclient"        % "commons-httpclient"    % "3.1",
  "commons-io"                % "commons-io"            % "2.7",
  "commons-lang"              % "commons-lang"          % "2.6",
  "dom4j"                     % "dom4j"                 % "1.6.1",
  "com.github.equella.legacy" % "itunesu-api-java"      % "1.7",
  "com.github.equella.legacy" % "mets"                  % "1.0",
  "com.metamx"                % "extendedset"           % "1.5.0-mmx",
  "javax.inject"              % "javax.inject"          % "1",
  "javax.mail"                % "mail"                  % "1.4.7",
  "javax.servlet"             % "jstl"                  % "1.1.2",
  "javax.xml"                 % "jaxrpc"                % "1.1",
  "jdom"                      % "jdom"                  % "1.0",
  "com.github.equella.jpf"    % "jpf"                   % "1.0.7",
  "log4j"                     % "log4j"                 % "1.2.17",
  "net.oauth.core"            % "oauth"                 % "20100527",
  "net.oauth.core"            % "oauth-provider"        % "20100527",
  "net.sf.ezmorph"            % "ezmorph"               % "1.0.6",
  "net.sf.json-lib"           % "json-lib"              % "2.4" classifier "jdk15",
  "net.sf.transmorph"         % "transmorph"            % "3.1.3",
  "org.apache.axis2"          % "axis2-kernel"          % axis2Version,
  "org.apache.axis2"          % "axis2-adb"             % axis2Version,
  "org.apache.axis2"          % "axis2-transport-http"  % axis2Version,
  "org.apache.axis2"          % "axis2-transport-local" % axis2Version,
  "org.apache.curator"        % "curator-client"        % "2.13.0",
  "org.apache.curator"        % "curator-framework"     % "2.13.0",
  "org.apache.curator"        % "curator-recipes"       % "2.13.0",
  "org.apache.cxf"            % "cxf-bundle"            % "2.7.6" excludeAll (
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
    ExclusionRule(organization = "com.sun.xml.messaging.saaj"),
    ExclusionRule(organization = "xalan"),
    ExclusionRule(organization = "com.sun.xml.fastinfoset"),
    ExclusionRule(organization = "net.sf.ehcache")
  ),
  "org.apache.httpcomponents" % "httpclient"       % "4.5.12",
  "org.apache.httpcomponents" % "httpcore"         % "4.4.13",
  "org.apache.lucene"         % "lucene-analyzers" % "3.6.2",
  "org.apache.lucene"         % "lucene-core"      % "3.6.2",
  "org.apache.lucene"         % "lucene-queries"   % "3.6.2",
  "org.apache.poi"            % "poi-ooxml"        % "3.9",
  "org.apache.rampart"        % "rampart-core"     % "1.6.2" excludeAll (
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.rampart" % "rampart-policy" % "1.6.2" excludeAll (
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.rampart" % "rampart-trust" % "1.6.2" excludeAll (
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.struts" % "struts-core" % "1.3.10" excludeAll (
    ExclusionRule(organization = "antlr",
                  name = "antlr")
  ),
  "org.apache.struts"           % "struts-extras"          % "1.3.10",
  "org.apache.struts"           % "struts-taglib"          % "1.3.10",
  "org.apache.tika"             % "tika-core"              % "1.3",
  "org.apache.tika"             % "tika-parsers"           % "1.3",
  "org.apache.tomcat"           % "tomcat-annotations-api" % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-api"             % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-catalina"        % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-catalina-ha"     % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-coyote"          % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-jsp-api"         % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-juli"            % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-servlet-api"     % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-tribes"          % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-util"            % TomcatVersion,
  "org.apache.tomcat"           % "tomcat-util-scan"       % TomcatVersion,
  "org.apache.ws.commons.axiom" % "axiom-api"              % "1.2.13",
  "org.apache.ws.commons.axiom" % "axiom-impl"             % "1.2.13",
  "org.apache.ws.security"      % "wss4j"                  % "1.6.19",
  "org.apache.zookeeper"        % "zookeeper"              % "3.4.6" excludeAll (
    ExclusionRule(organization = "org.slf4j",
                  name = "slf4j-log4j12")
  ),
  "org.ccil.cowan.tagsoup" % "tagsoup"           % "1.2.1",
  "org.codehaus.woodstox"  % "stax2-api"         % "3.1.4",
  "org.codehaus.woodstox"  % "woodstox-core-asl" % "4.4.1",
  "org.codehaus.xfire"     % "xfire-aegis"       % "1.2.6",
  "org.dspace"             % "cql-java"          % "1.0",
  //  "org.dspace.oclc" % "oclc-srw" % "1.0.20080328",
  "org.omegat"                           % "jmyspell-core"                  % "1.0.0-beta-2",
  "org.freemarker"                       % "freemarker"                     % "2.3.23",
  "com.github.equella.legacy"            % "hurl"                           % "1.1",
  "org.javassist"                        % "javassist"                      % "3.18.2-GA",
  "org.jboss.resteasy"                   % "resteasy-jaxrs"                 % RestEasyVersion,
  "org.jboss.spec.javax.annotation"      % "jboss-annotations-api_1.2_spec" % "1.0.2.Final",
  "org.jboss.logging"                    % "jboss-logging"                  % "3.4.1.Final",
  "org.jboss.logging"                    % "jboss-logging-annotations"      % "2.2.1.Final",
  "org.jboss.logging"                    % "jboss-logging-processor"        % "2.2.1.Final",
  "org.reactivestreams"                  % "reactive-streams"               % "1.0.3",
  "org.jboss.spec.javax.ws.rs"           % "jboss-jaxrs-api_2.1_spec"       % "1.0.0.Final",
  "org.eclipse.microprofile.rest.client" % "microprofile-rest-client-api"   % "1.0.1",
  "org.eclipse.microprofile.config"      % "microprofile-config-api"        % "1.4",
  "javax.json.bind"                      % "javax.json.bind-api"            % "1.0",
  "org.jsoup"                            % "jsoup"                          % jsoupVersion,
  xstreamDep,
  "org.opensaml" % "xmltooling" % "1.3.1" excludeAll (
    ExclusionRule(organization = "org.slf4j")
  ),
  "org.ow2.asm" % "asm" % "5.2",
  postgresDep,
  "org.scannotation"    % "scannotation"           % "1.0.3",
  "org.slf4j"           % "jcl-over-slf4j"         % "1.7.30",
  "org.slf4j"           % "slf4j-api"              % "1.7.30",
  "org.slf4j"           % "slf4j-log4j12"          % "1.7.30",
  "org.springframework" % "spring-aop"             % "2.5.6",
  "org.springframework" % "spring-context"         % "2.5.6",
  "org.springframework" % "spring-context-support" % "2.5.5" excludeAll (
    ExclusionRule(organization = "jasperreports",
                  name = "jasperreports")
  ),
  "org.springframework" % "spring-jdbc" % "2.5.6",
  "org.springframework" % "spring-tx"   % "2.5.6",
  "org.springframework" % "spring-web"  % "2.5.6",
//  "org.springframework" % "spring-webmvc" % "2.5.5" excludeAll (
//    ExclusionRule(organization = "jasperreports", name = "jasperreports")
//    ),
  "stax"                      % "stax-api"          % "1.0.1",
  "taglibs"                   % "standard"          % "1.1.2",
  "tomcat"                    % "jsp-api"           % "5.5.23",
  "com.github.equella.legacy" % "qtiworks-jqtiplus" % "1.0-beta3" excludeAll (
    ExclusionRule(organization = "org.slf4j"),
    ExclusionRule(organization = "ch.qos.logback"),
    ExclusionRule(organization = "net.sf.saxon")
  ),
  "xml-resolver"           % "xml-resolver"              % "1.2",
  "org.scala-sbt"          %% "io"                       % "1.3.4",
  "org.mozilla"            % "rhino"                     % "1.7.12",
  "io.lemonlabs"           %% "scala-uri"                % "1.5.1",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "io.bit3"                % "jsass"                     % "5.10.3",
  "io.github.classgraph"   % "classgraph"                % "4.8.86"
)

libraryDependencies ++= {
  if (bundleOracleDriver.value) {
    oracleDriverMavenCoordinate.value
  } else {
    Seq.empty
  }
}
dependencyOverrides += "javax.mail" % "mail" % "1.4.7"

excludeDependencies ++= Seq(
  "com.google.guava"             % "guava-jdk5",
  "asm"                          % "asm",
  "javax.servlet"                % "servlet-api",
  "org.mortbay.jetty"            % "servlet-api",
  "antlr"                        % "antlr",
  "stax"                         % "stax-api",
  "xml-apis"                     % "xml-apis",
  "javax.xml"                    % "jaxrpc-api",
  "xalan"                        % "xalan",
  "xerces"                       % "xercesImpl",
  "javax.activation"             % "activation",
  "javax.xml.stream"             % "stax-api",
  "javax.ws.rs"                  % "jsr311-api",
  "org.apache.ws.commons"        % "XmlSchema",
  "org.apache.ws.commons.schema" % "XmlSchema",
  "woodstox"                     % "wstx-asl",
  "org.codehaus.woodstox"        % "wstx-asl",
  "javassist"                    % "javassist",
  "org.sonatype.sisu.inject"     % "cglib",
  "commons-logging"              % "commons-logging",
  "velocity"                     % "velocity",
  "rhino"                        % "js",
  "bouncycastle"                 % "bcprov-jdk15",
  "org.bouncycastle"             % "bcprov-jdk15",
  "org.apache.geronimo.specs"    % "geronimo-javamail_1.4_spec",
  "org.apache.geronimo.specs"    % "geronimo-stax-api_1.0_spec",
  "org.jboss.spec.javax.servlet" % "jboss-servlet-api_3.1_spec"
)

run := {
  val cp = (fullClasspath in Runtime).value
  val o = ForkOptions().withRunJVMOptions(
    Vector(
      "-cp",
      Path.makeString(cp.files),
      "-Dequella.devmode=true",
      "-Dequella.autotest=true"
    ))
  Fork.java(o, Seq("com.tle.core.equella.runner.EQUELLAServer"))
}

mainClass in assembly := Some("com.tle.core.equella.runner.EQUELLAServer")

fullClasspath in assembly := (fullClasspath in Compile).value

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "axiom.xml")                    => MergeStrategy.first
  case PathList("javax", "wsdl", _*)                        => MergeStrategy.last
  case PathList("com", "ibm", "wsdl", _*)                   => MergeStrategy.first
  case PathList("org", "apache", "regexp", _*)              => MergeStrategy.first
  case PathList("javax", "servlet", "jsp", _*)              => MergeStrategy.first
  case PathList("javax", "annotation", _*)                  => MergeStrategy.first
  case PathList("org", "w3c", "dom", _*)                    => MergeStrategy.first
  case PathList("META-INF", "mailcap")                      => MergeStrategy.first
  case PathList("META-INF", "mimetypes.default")            => MergeStrategy.first
  case PathList("META-INF", "javamail.charset.map")         => MergeStrategy.first
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("javax", "activation", _*)                  => MergeStrategy.first
  case PathList("org", "xmlpull", "v1", _*)                 => MergeStrategy.first
  case PathList("junit", _*)                                => MergeStrategy.discard
  case PathList("org", "apache", "axis2", "transport", "http", "util", "ComplexPart.class") =>
    MergeStrategy.first
  // Safe to do at least in JDK 8
  case "module-info.class" => MergeStrategy.discard
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

lazy val allPlugins: ProjectReference = LocalProject("allPlugins")
runnerTasks(allPlugins)

additionalPlugins := {
  ((baseDirectory in allPlugins).value / "Extensions" * "*" * "plugin-jpf.xml").get.map { mf =>
    JPFRuntime(mf, Seq.empty, Seq.empty, Seq.empty, "Extensions")
  }
}

upgradeZip := {
  val log         = streams.value.log
  val ver         = equellaVersion.value
  var releaseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  val outZip
    : File    = target.value / s"tle-upgrade-${ver.major}.${ver.minor}.r${releaseDate} (${ver.semanticVersion}-${ver.releaseType}).zip"
  val plugVer = ver.fullVersion
  val zipFiles = Seq(
    assembly.value                                          -> "equella-server.jar",
    (assembly in LocalProject("UpgradeInstallation")).value -> "database-upgrader.jar",
    (assembly in LocalProject("conversion")).value          -> "conversion-service.jar",
    (versionProperties in LocalProject("equella")).value    -> "version.properties"
  )
  val pluginJars =
    writeJars.value.map(t => (t.file, s"plugins/${t.group}/${t.pluginId}-$plugVer.jar"))
  log.info(s"Creating upgrade zip ${outZip.absolutePath}")
  IO.zip(zipFiles ++ pluginJars, outZip)
  outZip
}

lazy val sourcesForZip = Def.task[Seq[(File, String)]] {
  val baseJavaSrc = (javaSource in Compile).value
  (baseJavaSrc ** "*.java").pair(rebase(baseJavaSrc, ""))
}

writeSourceZip := {
  val outZip  = target.value / "equella-sources.zip"
  val allSrcs = sourcesForZip.all(ScopeFilter(inAggregates(allPlugins))).value.flatten
  sLog.value.info(s"Zipping all sources into $outZip")
  IO.zip(allSrcs, outZip)
  outZip
}
