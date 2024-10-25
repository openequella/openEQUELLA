name := "graphql-client"

ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.openequella"
ThisBuild / organizationName := "openEQUELLA GraphQL Client"

lazy val root = (project in file("."))
  .settings(
    name := "graphql-client"
  )

libraryDependencies ++= Seq(
  "com.github.ghostdogpr"         %% "caliban-client" % "2.5.1",
  "com.softwaremill.sttp.client3" %% "zio"            % "3.9.7",
  // Add Scala Test
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  "org.scalatest"  %% "scalatest"      % "3.2.19" % Test,
)

scalacOptions ++= Seq(
  "-Werror",
  "-Wunused",
  "-Xlint",
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
       |""".stripMargin))
