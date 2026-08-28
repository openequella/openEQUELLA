name := "graphql-client"

ThisBuild / scalaVersion := "2.13.18"
// No version is declared: this library is consumed from source by the main openEQUELLA build and is
// not published, so a version here would be a number nobody has a reason to keep accurate. See the
// README for what would have to change for that to be worth revisiting.
ThisBuild / organization     := "io.github.openequella"
ThisBuild / organizationName := "openEQUELLA GraphQL Client"

lazy val root = (project in file("."))
  .settings(
    name := "graphql-client"
  )

libraryDependencies ++= Seq(
  "com.github.ghostdogpr" %% "caliban-client" % "3.1.5",
  "io.scalaland"          %% "chimney"        % "1.11.0",
  "org.typelevel"         %% "cats-core"      % "2.13.0",
  "com.lihaoyi"           %% "upickle"        % "4.4.3",
  // Add Scala Test
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  "org.scalatest" %% "scalatest"       % "3.2.20" % Test
)

scalacOptions ++= Seq(
  "-Werror",
  "-Wunused",
  "-Xlint"
)

enablePlugins(CalibanPlugin)
enablePlugins(AutomateHeaderPlugin)

headerLicense := Some(
  HeaderLicense.Custom("""|Licensed to The Apereo Foundation under one or more contributor license
       |agreements. See the NOTICE file distributed with this work for additional
       |information regarding copyright ownership.
       |
       |The Apereo Foundation licenses this file to you under the Apache License,
       |Version 2.0, (the "License"); you may not use this file except in compliance
       |with the License. You may obtain a copy of the License at:
       |
       |    http://www.apache.org/licenses/LICENSE-2.0
       |
       |Unless required by applicable law or agreed to in writing, software
       |distributed under the License is distributed on an "AS IS" BASIS,
       |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       |See the License for the specific language governing permissions and
       |limitations under the License.
       |""".stripMargin)
)

// Scapegoat Configuration
// - Ignore the code generated files
scapegoatIgnoredFiles := Seq(".*/src/main/scala/io/github/openequella/graphql/client/.*")

// SBT task to download the latest GraphQL schema from local server
lazy val downloadSchema =
  taskKey[Unit]("Download the latest GraphQL schema from the local openEQUELLA server")

downloadSchema := {
  import java.net.URI
  import java.net.http.{HttpClient, HttpRequest, HttpResponse}
  import java.nio.file.{Files, Paths, StandardOpenOption}
  import java.util.Properties
  import scala.util.Using

  /** Load a property value with the following precedence (lowest to highest):
    *   1. Default value
    *   2. `local.properties` file in the project root directory
    *   3. CLI -D system property
    */
  def loadProperty(key: String, default: String): String = {
    val fromFile: Option[String] =
      Option(Paths.get("local.properties").toFile)
        .filter(_.exists())
        .flatMap { file =>
          Using(new java.io.FileInputStream(file)) { stream =>
            val props = new Properties()
            props.load(stream)
            Option(props.getProperty(key))
          }.toOption.flatten
        }

    sys.props.get(key).orElse(fromFile).getOrElse(default)
  }

  val institutionUrl = loadProperty("oeq.institution.url", "http://localhost:8080/vanilla")
  val url            = s"$institutionUrl/graphql/schema"
  val targetDir      = "src/main/resources"
  val targetFile     = s"$targetDir/schema.graphql"

  // Ensure the target directory exists
  Files.createDirectories(Paths.get(targetDir))

  val client  = HttpClient.newHttpClient()
  val request = HttpRequest
    .newBuilder()
    .uri(URI.create(url))
    .GET()
    .build()

  val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
  val log      = streams.value.log
  if (response.statusCode() == 200) {
    Files.write(
      Paths.get(targetFile),
      response.body(),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
    log.info(s"Downloaded schema from $url to $targetFile")
  } else {
    sys.error(s"Failed to download schema from $url. Status: ${response.statusCode()}")
  }
}
