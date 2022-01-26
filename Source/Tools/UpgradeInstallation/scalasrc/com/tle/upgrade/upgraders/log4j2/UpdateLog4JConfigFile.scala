/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.upgrade.upgraders.log4j2

import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import com.dytech.edge.common.Constants
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tle.common.util.EquellaConfig
import com.tle.upgrade.{PropertyFileModifier, UpgradeResult}
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.upgraders.log4j2.Appender.buildAppenders
import com.tle.upgrade.upgraders.log4j2.Logger.buildLoggerConfig

import java.io.File
import java.util.Properties

/**
  * Data structure for the Log4j2 configuration in YAML format.
  *
  * @param status Log4j internal logging level.
  * @param appenders A map where key is an Appender type and value is a list of the Appender configuration.
  * @param Loggers The logger configuration including root logger and normal loggers.
  */
case class Configuration(
    status: String,
    appenders: Map[String, Seq[Appender]],
    Loggers: LoggerConfig
)

class UpdateLog4JConfigFile extends AbstractUpgrader {
  val mapper = new ObjectMapper(new YAMLFactory())
    .registerModule(DefaultScalaModule)
    .setSerializationInclusion(Include.NON_ABSENT)
    .configure(SerializationFeature.WRAP_ROOT_VALUE, true)

  override def getId: String = "UpdateLog4JConfigFile"

  override def isBackwardsCompatible: Boolean = false

  override def upgrade(result: UpgradeResult, installDir: File): Unit = {
    def buildYamlFile(file: File): Unit = {
      val props = loadProperties(file)
      (buildAppenders(props), buildLoggerConfig(props)).mapN(Configuration("warn", _, _)) match {
        case Invalid(e) => result.info(e.mkString_("\n"))
        case Valid(config) =>
          result.info("Successfully convert Log4J configuration!")
          mapper.writeValue(new File(file.getParent, file.getName.replace(".properties", ".yaml")),
                            config);
      }
    }

    List(
      new File(installDir, Constants.LEARNINGEDGE_CONFIG_FOLDER + "/learningedge-log4j.properties"),
      new File(installDir, Constants.MANAGER_FOLDER + "/log4j.properties"),
      new File(installDir, Constants.MANAGER_FOLDER + "/upgrader-log4j.properties")
    ).foreach(buildYamlFile)
  }
}

object main extends App {
  val f = new File("/home/penghai/Edalex/forks/2021",
                   Constants.MANAGER_FOLDER + "/upgrader-log4j.properties")
  println("name: " + f.getName)
  println("name: " + f.getParent)
}
