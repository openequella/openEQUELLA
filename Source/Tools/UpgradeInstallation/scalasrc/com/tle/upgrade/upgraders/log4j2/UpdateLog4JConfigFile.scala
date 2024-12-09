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
import com.tle.upgrade.UpgradeResult
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.upgraders.log4j2.Appender.buildAppenders
import com.tle.upgrade.upgraders.log4j2.Logger.buildLoggerConfig
import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}

/** Data structure for the Log4j2 configuration in YAML format.
  *
  * @param status
  *   Log4j internal logging level.
  * @param appenders
  *   A map where key is an Appender type and value is a list of the Appender configuration.
  * @param Loggers
  *   The logger configuration including root logger and normal loggers.
  */
case class Configuration(
    status: String,
    appenders: Map[String, Seq[Appender]],
    Loggers: LoggerConfig
)

/** This migration is intended to update Log4J configuration files to use the new syntax and YAML
  * format.
  *
  * Files to be updated:
  *   1. 'learningedge-log4j.properties' under folder `learningedge-config`; 2. 'log4j.properties'
  *      under folder `manager`; 3. 'upgrader-log4j.properties' under folder `manager`.
  *
  * The result of this migration is to have three YAML files in above folders alongside the original
  * configuration files.
  *
  * All the errors captured during the update will be printed out and the default configurations
  * files will be copied and pasted to above folders.
  */
class UpdateLog4JConfigFile extends AbstractUpgrader {
  val mapper = new ObjectMapper(new YAMLFactory())
    .registerModule(DefaultScalaModule)
    .setSerializationInclusion(Include.NON_ABSENT)
    .configure(SerializationFeature.WRAP_ROOT_VALUE, true)

  override def getId: String = "UpdateLog4JConfigFile"

  override def isBackwardsCompatible: Boolean = false

  override def upgrade(result: UpgradeResult, installDir: File): Unit = {
    val oldDefaultConfigFileName = "log4j.properties"
    val newDefaultConfigFileName = "log4j2.yaml"

    def yamlFileName(propertyFile: File) = {
      val fileName = propertyFile.getName
      if (fileName == oldDefaultConfigFileName) {
        newDefaultConfigFileName
      } else {
        fileName.replace(".properties", ".yaml")
      }
    }

    def copyDefaultConfigFile(propertyFile: File): Unit = {
      val name = yamlFileName(propertyFile)
      Files.copy(
        getClass.getClassLoader.getResourceAsStream(s"com/tle/upgrade/upgraders/$name"),
        Paths.get(s"${propertyFile.getParent}/$name"),
        StandardCopyOption.REPLACE_EXISTING
      )
    }

    def buildYamlFile(propertyFile: File): Unit = {
      val props = loadProperties(propertyFile)
      (buildAppenders(props), buildLoggerConfig(props)).mapN(Configuration("warn", _, _)) match {
        case Invalid(e) =>
          copyDefaultConfigFile(propertyFile)
          result.info(
            s"Failed to update Log4J configuration for file ${propertyFile.getName} due to \n ${e
                .mkString_("\n")}"
          )
        case Valid(config) =>
          mapper.writeValue(new File(propertyFile.getParent, yamlFileName(propertyFile)), config)
          result.info(s"Successfully update Log4J configuration for file ${propertyFile.getName}")
      }
    }

    List(
      new File(installDir, Constants.LEARNINGEDGE_CONFIG_FOLDER + "/learningedge-log4j.properties"),
      new File(installDir, Constants.MANAGER_FOLDER + "/" + oldDefaultConfigFileName),
      new File(installDir, Constants.MANAGER_FOLDER + "/upgrader-log4j.properties")
    ).foreach(buildYamlFile)
  }
}
