import Path.rebase

import java.time.LocalDate
import java.time.format.DateTimeFormatter

javacOptions ++= Seq("-source", "1.8")

(Compile / resourceDirectory) := baseDirectory.value / "resources"

(Compile / javaSource) := baseDirectory.value / "src"
(Test / javaSource) := baseDirectory.value / "test/java"

(Compile / scalaSource) := baseDirectory.value / "scalasrc"

updateOptions := updateOptions.value.withCachedResolution(true)

(Runtime / unmanagedClasspath) += (LocalProject("learningedge_config") / baseDirectory).value

val RestEasyVersion = "3.15.3.Final"
val SwaggerVersion  = "1.6.6"
val TomcatVersion   = "9.0.64"
val axis2Version    = "1.7.9"
val circeVersion    = "0.12.1"
val cxfVersion      = "3.5.2"
val fs2Version      = "2.5.11"
val guiceVersion    = "5.1.0"
val jsassVersion    = "5.10.4"
val jsoupVersion    = "1.14.3"
val springVersion   = "5.3.20"
val sttpVersion     = "1.7.2"
val tikaVersion     = "2.4.0"

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
  "cglib"                          % "cglib"                          % "3.3.0",
  "com.fasterxml.jackson.core"     % "jackson-core"                   % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"        % jacksonVersion,
  "com.fasterxml.jackson.core"     % "jackson-annotations"            % jacksonVersion,
  "com.fasterxml.jackson.jaxrs"    % "jackson-jaxrs-base"             % jacksonVersion,
  "com.fasterxml.jackson.jaxrs"    % "jackson-jaxrs-json-provider"    % jacksonVersion,
  jacksonDataBind,
  jacksonModuleScala,
  "io.bit3"         % "jsass"       % jsassVersion,
  "com.flickr4java" % "flickr4java" % "2.16" excludeAll (
    ExclusionRule(organization = "org.apache.axis",
                  name = "axis")
  ),
  "com.google.api-client" % "google-api-client"           % "1.35.0",
  "com.google.apis"       % "google-api-services-books"   % "v1-rev20220318-1.32.1",
  "com.google.apis"       % "google-api-services-youtube" % "v3-rev20220515-1.32.1",
  "com.google.code.gson"  % "gson"                        % "2.9.0",
  "com.google.gdata"      % "core"                        % "1.47.1",
  "com.google.guava"      % "guava"                       % "31.1-jre",
  "com.google.inject"     % "guice"                       % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    ExclusionRule(organization = "aopalliance",
                  name = "aopalliance")
  ),
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    ExclusionRule(organization = "aopalliance",
                  name = "aopalliance")
  ),
  "com.google.inject.extensions" % "guice-spring" % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    ExclusionRule(organization = "aopalliance",
                  name = "aopalliance")
  ),
  "com.ibm.icu" % "icu4j" % "71.1",
  sqlServerDep excludeAll (
    // Conflicts with RESTeasy jakarta.xml.bind-api
    ExclusionRule(organization = "javax.xml.bind"),
    // Conflicts with CXF Core
    ExclusionRule(organization = "com.sun.xml.bind"),
    ExclusionRule(organization = "com.sun.jersey")
  ),
  "com.miglayout"             % "miglayout-swing"       % "4.2",
  "com.ning"                  % "async-http-client"     % "1.9.40",
  "com.rometools"             % "rome"                  % "1.18.0",
  "io.swagger"                % "swagger-core"          % SwaggerVersion,
  "io.swagger"                % "swagger-annotations"   % SwaggerVersion,
  "io.swagger"                % "swagger-jaxrs"         % SwaggerVersion,
  "io.swagger"                %% "swagger-scala-module" % "1.0.6",
  "com.zaxxer"                % "HikariCP"              % "4.0.3",
  "commons-beanutils"         % "commons-beanutils"     % "1.9.4",
  "commons-codec"             % "commons-codec"         % "1.15",
  "commons-collections"       % "commons-collections"   % "3.2.2",
  "commons-configuration"     % "commons-configuration" % "1.10",
  "commons-daemon"            % "commons-daemon"        % "1.3.1",
  "commons-discovery"         % "commons-discovery"     % "0.5",
  "commons-httpclient"        % "commons-httpclient"    % "3.1",
  "commons-io"                % "commons-io"            % "2.11.0",
  "commons-lang"              % "commons-lang"          % "2.6",
  "dom4j"                     % "dom4j"                 % "1.6.1",
  "com.github.equella.legacy" % "itunesu-api-java"      % "1.7",
  "com.github.equella.legacy" % "mets"                  % "1.0",
  "com.metamx"                % "extendedset"           % "1.5.0-mmx",
  "javax.inject"              % "javax.inject"          % "1",
  "javax.mail"                % "mail"                  % "1.4.7",
  "javax.servlet"             % "jstl"                  % "1.2",
  "javax.xml"                 % "jaxrpc"                % "1.1",
  "jdom"                      % "jdom"                  % "1.1",
  "com.github.equella.jpf"    % "jpf"                   % "1.0.7",
  log4j,
  log4jSlf4jImpl,
  "net.oauth.core"     % "oauth"                    % "20100527",
  "net.oauth.core"     % "oauth-provider"           % "20100527",
  "net.sf.ezmorph"     % "ezmorph"                  % "1.0.6",
  "net.sf.json-lib"    % "json-lib"                 % "2.4" classifier "jdk15",
  "net.sf.transmorph"  % "transmorph"               % "3.1.3",
  "org.apache.axis2"   % "axis2-kernel"             % axis2Version,
  "org.apache.axis2"   % "axis2-adb"                % axis2Version,
  "org.apache.axis2"   % "axis2-transport-http"     % axis2Version,
  "org.apache.axis2"   % "axis2-transport-local"    % axis2Version,
  "org.apache.commons" % "commons-compress"         % "1.21",
  "org.apache.curator" % "curator-client"           % "5.2.1",
  "org.apache.curator" % "curator-framework"        % "5.2.1",
  "org.apache.curator" % "curator-recipes"          % "5.2.1",
  "org.apache.cxf"     % "cxf-rt-frontend-jaxws"    % cxfVersion,
  "org.apache.cxf"     % "cxf-rt-transports-http"   % cxfVersion,
  "org.apache.cxf"     % "cxf-rt-databinding-aegis" % cxfVersion,
  "org.apache.cxf"     % "cxf-core"                 % cxfVersion excludeAll (
    ExclusionRule(organization = "org.apache.geronimo.specs"),
    ExclusionRule(organization = "javax.xml.bind"),
    ExclusionRule(organization = "javax.xml.soap"),
    ExclusionRule(organization = "xml-resolver"),
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
  "org.apache.httpcomponents" % "httpclient"       % "4.5.13",
  "org.apache.httpcomponents" % "httpcore"         % "4.4.15",
  "org.apache.lucene"         % "lucene-analyzers" % "3.6.2",
  "org.apache.lucene"         % "lucene-core"      % "3.6.2",
  "org.apache.lucene"         % "lucene-queries"   % "3.6.2",
  "org.apache.rampart"        % "rampart-core"     % "1.6.3" excludeAll (
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
  "org.apache.tika" % "tika-core"                     % tikaVersion,
  "org.apache.tika" % "tika-parsers-standard-package" % tikaVersion excludeAll ExclusionRule(
    organization = "org.apache.logging.log4j"),
  "org.apache.tomcat"      % "tomcat-annotations-api" % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-api"             % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-catalina"        % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-catalina-ha"     % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-coyote"          % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-jsp-api"         % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-juli"            % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-servlet-api"     % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-tribes"          % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-util"            % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-util-scan"       % TomcatVersion,
  "org.apache.tomcat"      % "tomcat-ssi"             % TomcatVersion,
  "org.apache.ws.security" % "wss4j"                  % "1.6.19",
  "org.apache.zookeeper"   % "zookeeper"              % "3.7.0" excludeAll (
    ExclusionRule(organization = "org.slf4j",
                  name = "slf4j-log4j12")
  ),
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  // Removed due to deduplication issues with woodstox-core. core-asl has not been updated for years.
  //   com.fasterxml.woodstox/woodstox-core/bundles/woodstox-core-5.0.3.jar:...
  //   org.codehaus.woodstox/woodstox-core-asl/jars/woodstox-core-asl-4.4.1.jar:...
  //"org.codehaus.woodstox"  % "woodstox-core-asl" % "5.0.3",
  "org.codehaus.xfire"              % "xfire-aegis"                    % "1.2.6",
  "org.dspace"                      % "cql-java"                       % "1.0",
  "org.omegat"                      % "jmyspell-core"                  % "1.0.0-beta-2",
  "org.freemarker"                  % "freemarker"                     % "2.3.23",
  "com.github.equella.legacy"       % "hurl"                           % "1.1",
  "org.jboss.resteasy"              % "resteasy-jaxrs"                 % RestEasyVersion,
  "org.jboss.spec.javax.annotation" % "jboss-annotations-api_1.3_spec" % "2.0.1.Final",
  "org.reactivestreams"             % "reactive-streams"               % "1.0.4",
  // Upgraded to 2.0.1.Final due to a deduplication issue with jakarta.ws.rs-api
  "org.jboss.spec.javax.ws.rs"           % "jboss-jaxrs-api_2.1_spec"     % "2.0.2.Final",
  "org.eclipse.microprofile.rest.client" % "microprofile-rest-client-api" % "3.0",
  "org.eclipse.microprofile.config"      % "microprofile-config-api"      % "3.0.1",
  "javax.json.bind"                      % "javax.json.bind-api"          % "1.0",
  "org.jsoup"                            % "jsoup"                        % jsoupVersion,
  xstreamDep,
  "org.opensaml" % "xmltooling" % "1.3.1" excludeAll ExclusionRule(organization = "org.slf4j"),
  postgresDep,
  "org.scannotation"    % "scannotation"           % "1.0.3",
  "org.slf4j"           % "jcl-over-slf4j"         % "1.7.36",
  "org.slf4j"           % "slf4j-api"              % "1.7.36",
  "org.springframework" % "spring-aop"             % springVersion,
  "org.springframework" % "spring-context"         % springVersion,
  "org.springframework" % "spring-context-support" % springVersion excludeAll (
    ExclusionRule(organization = "jasperreports",
                  name = "jasperreports")
  ),
  "org.springframework" % "spring-jdbc" % springVersion,
  "org.springframework" % "spring-tx"   % springVersion,
  "org.springframework" % "spring-web"  % springVersion,
//  "org.springframework" % "spring-webmvc" % "2.5.5" excludeAll (
//    ExclusionRule(organization = "jasperreports", name = "jasperreports")
//    ),
  "stax"                      % "stax-api"          % "1.0.1",
  "taglibs"                   % "standard"          % "1.1.2",
  "com.github.equella.legacy" % "qtiworks-jqtiplus" % "1.0-beta3" excludeAll (
    ExclusionRule(organization = "org.slf4j"),
    ExclusionRule(organization = "ch.qos.logback"),
    ExclusionRule(organization = "net.sf.saxon")
  ),
  "xml-resolver"                  % "xml-resolver"              % "1.2",
  "org.scala-sbt"                 %% "io"                       % "1.6.0",
  "org.mozilla"                   % "rhino"                     % "1.7.14",
  "io.lemonlabs"                  %% "scala-uri"                % "4.0.2",
  "org.scala-lang.modules"        %% "scala-parser-combinators" % "2.1.1",
  "io.github.classgraph"          % "classgraph"                % "4.8.147",
  "com.fasterxml"                 % "classmate"                 % "1.5.1",
  "org.glassfish"                 % "javax.el"                  % "3.0.1-b12",
  "jakarta.validation"            % "jakarta.validation-api"    % "3.0.2",
  "com.github.stephenc.jcip"      % "jcip-annotations"          % "1.0-1",
  "org.jboss.spec.javax.xml.bind" % "jboss-jaxb-api_2.3_spec"   % "2.0.1.Final"
)

/*
Although very old and has vulns, axis 1.4 is required for the SRW feature and is needed when
using the very old (and unsure where the code is) oclc-srw.
See Source/Plugins/RemoteRepositories/com.equella.srw/build.sbt
 */
libraryDependencies += "axis" % "axis" % "1.4"

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
  "javax.servlet"                % "servlet-api",
  "org.mortbay.jetty"            % "servlet-api",
  "antlr"                        % "antlr",
  "stax"                         % "stax-api",
  "xml-apis"                     % "xml-apis",
  "javax.xml"                    % "jaxrpc-api",
  "xalan"                        % "xalan",
  "xerces"                       % "xercesImpl",
  "javax.activation"             % "javax.activation-api",
  "javax.activation"             % "activation",
  "javax.xml.stream"             % "stax-api",
  "javax.ws.rs"                  % "jsr311-api",
  "org.apache.ws.commons"        % "XmlSchema",
  "org.apache.ws.commons.schema" % "XmlSchema",
  "woodstox"                     % "wstx-asl",
  "org.codehaus.woodstox"        % "wstx-asl",
  "org.codehaus.woodstox"        % "woodstox-core-asl",
  "javassist"                    % "javassist",
  "org.sonatype.sisu.inject"     % "cglib",
  "commons-logging"              % "commons-logging",
  "velocity"                     % "velocity",
  "rhino"                        % "js",
  "bouncycastle"                 % "bcprov-jdk15",
  "org.bouncycastle"             % "bcprov-jdk15",
  "org.apache.geronimo.specs"    % "geronimo-javamail_1.4_spec",
  "org.apache.geronimo.specs"    % "geronimo-stax-api_1.0_spec",
  "org.jboss.spec.javax.servlet" % "jboss-servlet-api_4.0_spec",
  "taglibs"                      % "standard",
  // Spring 5 added a default logging bridge.  In oEQ, this results in
  // a [deduplicate: different file contents found in the following] error
  // ...org.slf4j/jcl-over-slf4j/jars/jcl-over-slf4j-1.7.30.jar:org/apache/commons/logging/Log.class
  // ...org.springframework/spring-jcl/jars/spring-jcl-5.3.20.jar:org/apache/commons/logging/Log.class
  // As per https://github.com/spring-projects/spring-framework/issues/20611 ,
  // since we already have logging in place, we can safely exclude the dep from spring.
  "org.springframework" % "spring-jcl",
  // Hib 5 upgrade showed the following errors.  Solution was to remove tomcat-el-api
  // - Caused by: java.lang.ClassNotFoundException: org.apache.el.ExpressionFactoryImpl from org.hibernate
  // - HV000183: Unable to initialize 'javax.el.ExpressionFactory'. Check that you have the EL dependencies on the classpath, or use ParameterMessageInterpolator instead
  "org.apache.tomcat" % "tomcat-el-api",
  // Resolves deduplication:
  // [error] com.github.stephenc.jcip/jcip-annotations/jars/jcip-annotations-1.0-1.jar:net/jcip/annotations/GuardedBy.class
  // [error] net.jcip/jcip-annotations/jars/jcip-annotations-1.0.jar:net/jcip/annotations/GuardedBy.class
  "net.jcip" % "jcip-annotations",
  // Caused by deduplication errors such as below.  Choosing jboss since it failed at compile time without it.
  //  [error] jakarta.ws.rs/jakarta.ws.rs-api/bundles/jakarta.ws.rs-api-2.1.5.jar:javax/ws/rs/sse/SseEventSource.class
  //  [error] org.jboss.spec.javax.ws.rs/jboss-jaxrs-api_2.1_spec/bundles/jboss-jaxrs-api_2.1_spec-2.0.1.Final.jar:javax/ws/rs/sse/SseEventSource.class
  "jakarta.ws.rs" % "jakarta.ws.rs-api",
  // Caused by deduplication errors such as below.  Choosing jboss since it failed at compile time without it.
  // [error] jakarta.xml.bind/jakarta.xml.bind-api/jars/jakarta.xml.bind-api-2.3.2.jar:javax/xml/bind/util/JAXBSource.class
  // [error] org.jboss.spec.javax.xml.bind/jboss-jaxb-api_2.3_spec/jars/jboss-jaxb-api_2.3_spec-2.0.0.Final.jar:javax/xml/bind/util/JAXBSource.class
  "jakarta.xml.bind" % "jakarta.xml.bind-api",
  // Caused by deduplication errors such as below.  Choosing jakarta since it's a higher version
  // [error] deduplicate: different file contents found in the following:
  // [error] com.sun.activation/jakarta.activation/jars/jakarta.activation-1.2.1.jar:com/sun/activation/viewers/TextViewer.class
  // [error] com.sun.activation/javax.activation/jars/javax.activation-1.2.0.jar:com/sun/activation/viewers/TextViewer.class
  "com.sun.activation" % "javax.activation",
  // Older log4j can be a transitive dep so exclude it
  "log4j" % "log4j"
)

run := {
  val cp = (Runtime / fullClasspath).value
  val o = ForkOptions().withRunJVMOptions(
    Vector(
      "-cp",
      Path.makeString(cp.files),
      "-Dequella.devmode=true",
      "-Dequella.autotest=true"
    ))
  Fork.java(o, Seq("com.tle.core.equella.runner.EQUELLAServer"))
}

(assembly / mainClass) := Some("com.tle.core.equella.runner.EQUELLAServer")

(assembly / fullClasspath) := (Compile / fullClasspath).value

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF",
                "org",
                "apache",
                "logging",
                "log4j",
                "core",
                "config",
                "plugins",
                "Log4j2Plugins.dat") =>
    MergeStrategy.last
  case PathList("META-INF", "jdom-info.xml")                => MergeStrategy.first
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
  // Three duplicate classes caused by upgrading tika to version 2.
  case PathList("org", "slf4j", "impl", "StaticMDCBinder.class")    => MergeStrategy.first
  case PathList("org", "slf4j", "impl", "StaticLoggerBinder.class") => MergeStrategy.first
  case PathList("org", "slf4j", "impl", "StaticMarkerBinder.class") => MergeStrategy.first

  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-frontend-jaxws/bundles/cxf-rt-frontend-jaxws-3.5.2.jar:META-INF/cxf/bus-extensions.txt
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.5.2.jar:META-INF/cxf/bus-extensions.txt
  // ...
  // As per https://github.com/johnrengelman/shadow/issues/309 , combining the files.
  case PathList("META-INF", "cxf", "bus-extensions.txt") => MergeStrategy.filterDistinctLines

  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-frontend-jaxrs/bundles/cxf-rt-frontend-jaxrs-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-frontend-jaxws/bundles/cxf-rt-frontend-jaxws-3.4.0.jar:META-INF/blueprint.handlers
  // ...
  // Different blueprint.handlers may specify different classes.  Using the first one allows testing to pass.
  case PathList("META-INF", "blueprint.handlers") => MergeStrategy.first

  // Curious that it's xml vs soap.  testing passes using the first one.
  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-bindings-soap/bundles/cxf-rt-bindings-soap-3.4.0.jar:META-INF/wsdl.plugin.xml
  //  .../org.apache.cxf/cxf-rt-bindings-xml/bundles/cxf-rt-bindings-xml-3.4.0.jar:META-INF/wsdl.plugin.xml
  // ...
  case PathList("META-INF", "wsdl.plugin.xml") => MergeStrategy.first

  // The idea is to keep the later suffix list.
  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:mozilla/public-suffix-list.txt
  //  .../org.apache.httpcomponents/httpclient/jars/httpclient-4.5.12.jar:mozilla/public-suffix-list.txt
  // ...
  case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.last

  // java2wsbeans.xml have different contents, and both look important.  Keeping the first one works with testing.
  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-databinding-aegis/bundles/cxf-rt-databinding-aegis-3.4.0.jar:META-INF/cxf/java2wsbeans.xml
  //  .../org.apache.cxf/cxf-rt-databinding-jaxb/bundles/cxf-rt-databinding-jaxb-3.4.0.jar:META-INF/cxf/java2wsbeans.xml
  // ...
  case PathList("META-INF", "cxf", "java2wsbeans.xml") => MergeStrategy.first

  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val collectJars = taskKey[Set[File]]("Collect jars")

collectJars := {
  val destDir = target.value / "jars"
  IO.delete(destDir)
  IO.copy((Compile / managedClasspath).value.map(af => (af.data, destDir / af.data.getName)))
}

lazy val allPlugins: ProjectReference = LocalProject("allPlugins")
runnerTasks(allPlugins)

additionalPlugins := {
  ((allPlugins / baseDirectory).value / "Extensions" * "*" * "plugin-jpf.xml").get.map { mf =>
    JPFRuntime(mf, Seq.empty, Seq.empty, Seq.empty, "Extensions")
  }
}

upgradeZip := {
  val log         = streams.value.log
  val ver         = equellaVersion.value
  val releaseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  val outZip
    : File    = target.value / s"tle-upgrade-${ver.major}.${ver.minor}.r${releaseDate} (${ver.semanticVersion}-${ver.releaseType}).zip"
  val plugVer = ver.fullVersion
  val zipFiles = Seq(
    assembly.value                                         -> "equella-server.jar",
    (LocalProject("UpgradeInstallation") / assembly).value -> "database-upgrader.jar",
    (LocalProject("conversion") / assembly).value          -> "conversion-service.jar",
    (LocalProject("equella") / versionProperties).value    -> "version.properties"
  )
  val pluginJars =
    writeJars.value.map(t => (t.file, s"plugins/${t.group}/${t.pluginId}-$plugVer.jar"))
  log.info(s"Creating upgrade zip ${outZip.absolutePath}")
  IO.zip(zipFiles ++ pluginJars, outZip, Option((ThisBuild / buildTimestamp).value))
  outZip
}

lazy val sourcesForZip = Def.task[Seq[(File, String)]] {
  val baseJavaSrc = (Compile / javaSource).value
  (baseJavaSrc ** "*.java").pair(rebase(baseJavaSrc, ""))
}

writeSourceZip := {
  val outZip  = target.value / "equella-sources.zip"
  val allSrcs = sourcesForZip.all(ScopeFilter(inAggregates(allPlugins))).value.flatten
  sLog.value.info(s"Zipping all sources into $outZip")
  IO.zip(allSrcs, outZip, Option((ThisBuild / buildTimestamp).value))
  outZip
}
