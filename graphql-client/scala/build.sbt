name := "graphql-client"

ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / version          := "0.6.0-SNAPSHOT"
ThisBuild / organization     := "io.github.openequella"
ThisBuild / organizationName := "openEQUELLA GraphQL Client"

lazy val root = (project in file("."))
  .settings(
    name := "graphql-client"
  )

libraryDependencies ++= Seq(
  "com.github.ghostdogpr" %% "caliban-client" % "3.0.0",
  "io.scalaland"          %% "chimney"        % "1.9.0",
  "org.typelevel"         %% "cats-core"      % "2.13.0",
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

  val url        = "http://localhost:8080/vanilla/graphql/schema"
  val targetDir  = "src/main/resources"
  val targetFile = s"$targetDir/schema.graphql"

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
