import Path.rebase
import sbt.Package.ManifestAttributes

import java.time.LocalDate
import java.time.format.DateTimeFormatter

(Compile / resourceDirectory) := baseDirectory.value / "resources"

(Compile / javaSource) := baseDirectory.value / "src"
(Test / javaSource)    := baseDirectory.value / "test/java"

(Compile / scalaSource) := baseDirectory.value / "scalasrc"

updateOptions := updateOptions.value.withCachedResolution(true)

(Runtime / unmanagedClasspath) += (LocalProject("learningedge_config") / baseDirectory).value

val RestEasyVersion   = "3.15.6.Final"
val SwaggerVersion    = "1.6.15"
val TomcatVersion     = "9.0.104"
val axis2Version      = "1.8.2"
val circeVersion      = "0.14.5"
val curatorVersion    = "5.8.0"
val cxfVersion        = "3.6.6"
val fs2Version        = "2.5.12"
val guiceVersion      = "5.1.0"
val jsassVersion      = "5.11.1"
val jsoupVersion      = "1.19.1"
val prometheusVersion = "0.16.0"
val sttpVersion       = "2.3.0"
val tikaVersion       = "2.9.3"
val luceneVersion     = "9.12.1"
val nettyVersion      = "4.2.0.Final"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
libraryDependencies += "io.circe" %% "circe-generic-extras" % "0.14.4"

val prometheusGroup = "io.prometheus"
libraryDependencies ++= Seq(
  prometheusGroup % "simpleclient",
  prometheusGroup % "simpleclient_hotspot",
  prometheusGroup % "simpleclient_servlet"
).map(_ % prometheusVersion)

// Libraries needed for JWT validation in LTI 1.3 / OpenID connect
libraryDependencies ++= Seq(
  "com.auth0" % "java-jwt" % "4.5.0",
  "com.auth0" % "jwks-rsa" % "0.22.1"
)

libraryDependencies ++= Seq(
  "co.fs2"                        %% "fs2-io"                        % fs2Version,
  "com.softwaremill.sttp.client"  %% "core"                          % sttpVersion,
  "com.softwaremill.sttp.client"  %% "async-http-client-backend-fs2" % sttpVersion,
  "com.softwaremill.sttp.client"  %% "circe"                         % sttpVersion,
  "cglib"                          % "cglib"                         % "3.3.0",
  "com.fasterxml.jackson.core"     % "jackson-core"                  % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"       % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"         % jacksonVersion,
  "com.fasterxml.jackson.core"     % "jackson-annotations"           % jacksonVersion,
  "com.fasterxml.jackson.jaxrs"    % "jackson-jaxrs-base"            % jacksonVersion,
  "com.fasterxml.jackson.jaxrs"    % "jackson-jaxrs-json-provider"   % jacksonVersion exclude (
    "javax.xml.bind",
    "jaxb-api"
  ),
  jacksonDataBind,
  jacksonModuleScala,
  "io.bit3"         % "jsass"       % jsassVersion,
  "com.flickr4java" % "flickr4java" % "3.0.9" excludeAll (
    ExclusionRule(organization = "org.apache.axis", name = "axis")
  ),
  "com.google.api-client" % "google-api-client"           % "2.5.1",
  "com.google.apis"       % "google-api-services-books"   % "v1-rev20240214-2.0.0",
  "com.google.apis"       % "google-api-services-youtube" % "v3-rev20240514-2.0.0",
  "com.google.code.gson"  % "gson"                        % "2.13.1",
  "com.google.guava"      % "guava"                       % "32.1.3-jre",
  "com.google.inject"     % "guice"                       % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    ExclusionRule(organization = "aopalliance", name = "aopalliance")
  ),
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    ExclusionRule(organization = "aopalliance", name = "aopalliance")
  ),
  "com.google.inject.extensions" % "guice-spring" % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    ExclusionRule(organization = "aopalliance", name = "aopalliance")
  ),
  "com.ibm.icu" % "icu4j" % "77.1",
  sqlServerDep excludeAll (
    // Conflicts with RESTeasy jakarta.xml.bind-api
    ExclusionRule(organization = "javax.xml.bind"),
    // Conflicts with CXF Core
    ExclusionRule(organization = "com.sun.xml.bind"),
    ExclusionRule(organization = "com.sun.jersey")
  ),
  "org.asynchttpclient" % "async-http-client"    % "2.12.4",
  "com.rometools"       % "rome"                 % "2.1.0",
  "io.swagger"          % "swagger-core"         % SwaggerVersion,
  "io.swagger"          % "swagger-annotations"  % SwaggerVersion,
  "io.swagger"          % "swagger-jaxrs"        % SwaggerVersion,
  "io.swagger"         %% "swagger-scala-module" % "1.0.6",
  // Exclude slf4j due to issue: https://github.com/brettwooldridge/HikariCP/issues/1746
  "com.zaxxer" % "HikariCP" % "6.3.0" excludeAll ExclusionRule(organization = "org.slf4j"),
  "commons-beanutils"         % "commons-beanutils"     % "1.10.1",
  "commons-codec"             % "commons-codec"         % "1.18.0",
  "commons-collections"       % "commons-collections"   % "3.2.2",
  "commons-configuration"     % "commons-configuration" % "1.10",
  "commons-daemon"            % "commons-daemon"        % "1.4.1",
  "commons-discovery"         % "commons-discovery"     % "0.5",
  "commons-httpclient"        % "commons-httpclient"    % "3.1",
  "commons-io"                % "commons-io"            % "2.19.0",
  "commons-lang"              % "commons-lang"          % "2.6",
  "com.github.equella.legacy" % "itunesu-api-java"      % "1.7",
  "com.github.equella.legacy" % "mets"                  % "1.0",
  "com.metamx"                % "extendedset"           % "1.5.0-mmx",
  "javax.inject"              % "javax.inject"          % "1",
  "javax.mail"                % "mail"                  % "1.4.7",
  "javax.servlet"             % "jstl"                  % "1.2",
  "javax.xml"                 % "jaxrpc"                % "1.1",
  "com.github.equella.jpf"    % "jpf"                   % "1.0.7",
  log4j,
  log4jCore,
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
  "org.apache.commons" % "commons-compress"         % "1.27.1",
  "org.apache.curator" % "curator-client"           % curatorVersion,
  "org.apache.curator" % "curator-framework"        % curatorVersion,
  "org.apache.curator" % "curator-recipes"          % curatorVersion,
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
  "org.apache.httpcomponents" % "httpclient"             % "4.5.14",
  "org.apache.httpcomponents" % "httpcore"               % "4.4.16",
  "org.apache.lucene"         % "lucene-core"            % luceneVersion,
  "org.apache.lucene"         % "lucene-analysis-common" % luceneVersion,
  "org.apache.lucene"         % "lucene-queryparser"     % luceneVersion,
  "org.apache.lucene"         % "lucene-queries"         % luceneVersion,
  "org.apache.lucene"         % "lucene-backward-codecs" % luceneVersion,
  "org.apache.rampart"        % "rampart-core"           % "1.6.3" excludeAll (
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces"),
    ExclusionRule(organization = "org.bouncycastle")
  ),
  "org.apache.rampart" % "rampart-policy" % "1.6.2" excludeAll (
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces")
  ),
  "org.apache.rampart" % "rampart-trust" % "1.6.2" excludeAll (
    ExclusionRule(organization = "org.apache.xalan"),
    ExclusionRule(organization = "org.apache.xerces"),
    ExclusionRule(organization = "org.bouncycastle")
  ),
  "org.apache.tika" % "tika-core"                     % tikaVersion,
  "org.apache.tika" % "tika-parsers-standard-package" % tikaVersion excludeAll (
    ExclusionRule(organization = "org.apache.logging.log4j"),
    ExclusionRule(organization = "org.bouncycastle")
  ),
  "org.apache.tomcat"                    % "tomcat-annotations-api"         % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-api"                     % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-catalina"                % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-catalina-ha"             % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-coyote"                  % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-jsp-api"                 % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-juli"                    % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-servlet-api"             % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-tribes"                  % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-util"                    % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-util-scan"               % TomcatVersion,
  "org.apache.tomcat"                    % "tomcat-ssi"                     % TomcatVersion,
  "org.bouncycastle"                     % "bcprov-jdk18on"                 % "1.80",
  "org.ccil.cowan.tagsoup"               % "tagsoup"                        % "1.2.1",
  "org.codehaus.xfire"                   % "xfire-aegis"                    % "1.2.6",
  "org.dspace"                           % "cql-java"                       % "1.0",
  "org.omegat"                           % "jmyspell-core"                  % "1.0.0-beta-2",
  "org.freemarker"                       % "freemarker"                     % "2.3.23",
  "com.github.equella.legacy"            % "hurl"                           % "1.1",
  "org.jboss.resteasy"                   % "resteasy-jaxrs"                 % RestEasyVersion,
  "org.jboss.spec.javax.annotation"      % "jboss-annotations-api_1.3_spec" % "2.0.1.Final",
  "org.reactivestreams"                  % "reactive-streams"               % "1.0.4",
  "org.jboss.spec.javax.ws.rs"           % "jboss-jaxrs-api_2.1_spec"       % "2.0.2.Final",
  "org.eclipse.microprofile.rest.client" % "microprofile-rest-client-api"   % "3.0.1",
  "org.eclipse.microprofile.config"      % "microprofile-config-api"        % "3.1",
  "javax.json.bind"                      % "javax.json.bind-api"            % "1.0",
  "org.jsoup"                            % "jsoup"                          % jsoupVersion,
  xstreamDep,
  postgresDep,
  "org.scannotation" % "scannotation"   % "1.0.3",
  "org.slf4j"        % "jcl-over-slf4j" % "2.0.17",
  "org.slf4j"        % "slf4j-api"      % "2.0.17",
  springAop,
  springWeb,
  springContext,
  "org.springframework" % "spring-context-support" % springVersion excludeAll (
    ExclusionRule(organization = "jasperreports", name = "jasperreports")
  ),
  "org.springframework"       % "spring-jdbc"       % springVersion,
  "org.springframework"       % "spring-tx"         % springVersion,
  "stax"                      % "stax-api"          % "1.0.1",
  "taglibs"                   % "standard"          % "1.1.2",
  "com.github.equella.legacy" % "qtiworks-jqtiplus" % "1.0-beta3" excludeAll (
    ExclusionRule(organization = "org.slf4j"),
    ExclusionRule(organization = "ch.qos.logback"),
    ExclusionRule(organization = "net.sf.saxon")
  ),
  "xml-resolver"                  % "xml-resolver"             % "1.2",
  "org.scala-sbt"                %% "io"                       % "1.10.5",
  "org.mozilla"                   % "rhino"                    % "1.8.0",
  "io.lemonlabs"                 %% "scala-uri"                % "4.0.3",
  "org.scala-lang.modules"       %% "scala-parser-combinators" % "2.4.0",
  "io.github.classgraph"          % "classgraph"               % "4.8.179",
  "com.fasterxml"                 % "classmate"                % "1.7.0",
  "org.glassfish"                 % "javax.el"                 % "3.0.1-b12",
  "jakarta.validation"            % "jakarta.validation-api"   % "3.1.1",
  "com.github.stephenc.jcip"      % "jcip-annotations"         % "1.0-1",
  "org.jboss.spec.javax.xml.bind" % "jboss-jaxb-api_2.3_spec"  % "2.0.1.Final"
)

libraryDependencies ++= {
  if (bundleOracleDriver.value) {
    oracleDriverMavenCoordinate.value
  } else {
    Seq.empty
  }
}
dependencyOverrides ++= Seq(
  "javax.mail" % "mail"                % "1.4.7",
  "io.netty"   % "netty-common"        % nettyVersion,
  "io.netty"   % "netty-buffer"        % nettyVersion,
  "io.netty"   % "netty-codec"         % nettyVersion,
  "io.netty"   % "netty-handler"       % nettyVersion,
  "io.netty"   % "netty-transport"     % nettyVersion,
  "io.netty"   % "netty-codec-socks"   % nettyVersion,
  "io.netty"   % "netty-handler-proxy" % nettyVersion,
  "io.netty"   % "netty-codec-http"    % nettyVersion
)

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
  // ...org.springframework/spring-jcl/jars/spring-jcl-5.3.23.jar:org/apache/commons/logging/Log.class
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
    )
  )
  Fork.java(o, Seq("com.tle.core.equella.runner.EQUELLAServer"))
}

(assembly / mainClass) := Some("com.tle.core.equella.runner.EQUELLAServer")

(assembly / fullClasspath) := (Compile / fullClasspath).value

(assembly / assemblyMergeStrategy) := {
  case PathList(
        "META-INF",
        "org",
        "apache",
        "logging",
        "log4j",
        "core",
        "config",
        "plugins",
        "Log4j2Plugins.dat"
      ) =>
    MergeStrategy.last
  case PathList("META-INF", "jdom-info.xml")                => MergeStrategy.first
  case PathList("META-INF", "axiom.xml")                    => MergeStrategy.first
  case PathList("javax", "wsdl", _*)                        => MergeStrategy.last
  case PathList("javax", "xml", "soap", _*)                 => MergeStrategy.first
  case PathList("javax", "transaction", _*)                 => MergeStrategy.first
  case PathList("javax", "jws", _*)                         => MergeStrategy.first
  case PathList("com", "ibm", "wsdl", _*)                   => MergeStrategy.first
  case PathList("org", "apache", "regexp", _*)              => MergeStrategy.first
  case PathList("javax", "servlet", "jsp", _*)              => MergeStrategy.first
  case PathList("javax", "servlet", _*)                     => MergeStrategy.last
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
  //  .../org.apache.cxf/cxf-rt-frontend-jaxws/bundles/cxf-rt-frontend-jaxws-3.5.5.jar:META-INF/cxf/bus-extensions.txt
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.5.5.jar:META-INF/cxf/bus-extensions.txt
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
  // Apache tika 2.8 brings in bouncycastle:bcprov-jdk18on, which causes SBT Deduplicate issues.
  // For example:
  //   Jar name = bcprov-jdk15on-1.51.jar, jar org = org.bouncycastle, entry target = org/bouncycastle/crypto/ec/CustomNamedCurves$2.class
  //   Jar name = bcprov-jdk18on-1.73.jar, jar org = org.bouncycastle, entry target = org/bouncycastle/crypto/ec/CustomNamedCurves$2.class
  // Keep the later one to use the newer version of bcprov.
  case PathList("org", "bouncycastle", _*) => MergeStrategy.last
  // The advice for native-image property files is to name them specific to the group and module to
  // which the belong. However, it seems Google (google-auth-library-oauth2-http-1.23.0.jar) and
  // Oracle (ojdbc8-23.3.0.23.09.jar) themselves have failed to follow this advice. A simple rename
  // though would still allow them to be detected.
  // Advice: https://docs.oracle.com/en/graalvm/enterprise/21/docs/reference-manual/native-image/BuildConfiguration/#embedding-a-configuration-file
  case PathList("META-INF", "native-image", _*) => MergeStrategy.rename
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

// In order to work properly with Java 21, Some libraries like Lucene v9 requires flag 'Multi-Release' to
// be true in the manifest file.
assembly / packageOptions += ManifestAttributes("Multi-Release" -> "true")

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
  val outZip: File =
    target.value / s"tle-upgrade-${ver.major}.${ver.minor}.r${releaseDate} (${ver.semanticVersion}-${ver.releaseType}).zip"
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
  val baseJavaSrc  = (Compile / javaSource).value
  val baseScalaSrc = (Compile / scalaSource).value
  (baseJavaSrc ** "*.java").pair(rebase(baseJavaSrc, "")) ++
    (baseScalaSrc ** "*.scala").pair(rebase(baseScalaSrc, ""))
}

writeSourceZip := {
  val outZip  = target.value / "equella-sources.zip"
  val allSrcs = sourcesForZip.all(ScopeFilter(inAggregates(allPlugins))).value.flatten
  sLog.value.info(s"Zipping all sources into $outZip")
  IO.zip(allSrcs, outZip, Option((ThisBuild / buildTimestamp).value))
  outZip
}
