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

import cats.implicits._
import cats.data.{Validated, ValidatedNec}
import com.fasterxml.jackson.annotation.{JsonAnyGetter, JsonIgnoreProperties}
import com.tle.upgrade.upgraders.log4j2.Appender.getLayoutMap
import com.tle.upgrade.upgraders.log4j2.Filter.getFilters
import com.tle.upgrade.upgraders.log4j2.Layout.getLayout
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.{
  readBooleanProperty,
  readIntProperty,
  readProperty
}

import java.util.Properties
import scala.jdk.CollectionConverters._

case class RollOverStrategy(max: Int = 20)

sealed trait RollingPolicy
case class TimeBasedTriggeringPolicy(interval: Int = 1, modulate: Boolean = true)
    extends RollingPolicy
case class SizeBasedTriggeringPolicy(size: String = "10 MB") extends RollingPolicy

@JsonIgnoreProperties(Array("layout"))
sealed trait Appender {
  def layout: Layout
  @JsonAnyGetter
  def getLayout: java.util.Map[String, Layout] = getLayoutMap(layout)
}

/** Data structure to match the Log4J Console Appender configuration.
  */
case class Console(
    name: String,
    target: String,
    layout: Layout,
    Filters: Option[Map[String, Seq[Filter]]]
) extends Appender

/** Data structure to match the Log4J RollingFile Appender configuration.
  */
case class RollingFile(
    name: String,
    fileName: String,
    filePattern: String,
    immediateFlush: Boolean,
    append: Boolean,
    layout: Layout,
    Filters: Option[Map[String, Seq[Filter]]],
    Policies: Map[String, RollingPolicy],
    DefaultRollOverStrategy: RollOverStrategy
) extends Appender

object Appender {
  val appenderPrefix = "log4j.appender."

  /** Build a Map which helps generate a layout in the YAML file.
    *
    * @param layout
    *   The layout of the Appender.
    * @return
    *   A Map where key is the layout type and value is the layout.
    */
  def getLayoutMap(layout: Layout): java.util.Map[String, Layout] =
    Map(layout.getClass.getSimpleName -> layout).asJava

  def appenderName(key: String): String = key.replace(appenderPrefix, "")

  /** Build a Log4J Console Appender based on the supplied configuration.
    *
    * @param key
    *   The property key used to define the Console Appender.
    * @param props
    *   Property file which provides details of the Appender.
    * @return
    *   `ValidatedNec` where left is a list of error messages and right is the Console Appender.
    */
  def buildConsole(key: String, props: Properties): ValidatedNec[String, Console] = {
    def target =
      readProperty(s"$key.Target", props)
        // If target is "system.out", use "system_out" instead.
        .filter(_.toLowerCase != "system.out")
        .getOrElse("SYSTEM_OUT")

    (getLayout(s"$key.layout", props), getFilters(key, props))
      .mapN((layout, filters) => Console(appenderName(key), target, layout, filters))
  }

  /** Build a Log4J Rolling File Appender based on the supplied configuration.
    *
    * @param key
    *   The property key used to define the Rolling File Appender.
    * @param props
    *   Property file which provides details of the Appender.
    * @param isCustomRollingFile
    *   `true` to build OEQ custom Rolling File Appender which always has a
    *   TimeBasedTriggeringPolicy.
    * @return
    *   `ValidatedNec` where left is a list of error messages and right is a Rolling File Appender.
    */
  def buildRollingFile(
      key: String,
      props: Properties,
      isCustomRollingFile: Boolean = true
  ): ValidatedNec[String, RollingFile] = {
    def fileName =
      readProperty(s"$key.File", props)
        .toValidNec(s"Must specify a file name for $key")

    def immediateFlush =
      readBooleanProperty(s"$key.ImmediateFlush", props).getOrElse(true)

    def append =
      readBooleanProperty(s"$key.Append", props).getOrElse(true)

    def maxFileSize =
      readProperty(s"$key.MaxFileSize", props)
        .getOrElse("10MB")

    def maxBackupIndex =
      readIntProperty(s"$key.MaxBackupIndex", props)
        .getOrElse(10)

    def filePattern(fileName: String) = {
      // Regex matching a file name and an optional directory.
      // Examples:
      // 1. test.log
      // 2. ../test.log
      // 3. dir/test.log
      // 4. dir1/dir2/.../dir10/test.log
      val Regex = """(.*/)?(.*)\.(.*)$""".r

      // Log4J default date pattern.
      val defaultPattern = "%d{yyyy-MM-dd}"

      fileName match {
        case Regex(dir, filename, ext) =>
          s"${Option(dir).getOrElse("")}$defaultPattern/$filename-%i.$ext"
        case name => s"$defaultPattern/$name-%i"
      }
    }

    def triggeringPolicy: Map[String, RollingPolicy] = {
      val sizedBasedPolicy = Map(
        "SizeBasedTriggeringPolicy" -> SizeBasedTriggeringPolicy(maxFileSize)
      )

      if (isCustomRollingFile) {
        sizedBasedPolicy + ("TimeBasedTriggeringPolicy" -> TimeBasedTriggeringPolicy())
      } else {
        sizedBasedPolicy
      }
    }

    (fileName, getLayout(s"$key.layout", props), getFilters(key, props)).mapN(
      (filename, layout, filters) => {
        RollingFile(
          name = appenderName(key),
          fileName = filename,
          filePattern = filePattern(filename),
          immediateFlush = immediateFlush,
          append = append,
          layout = layout,
          Filters = filters,
          DefaultRollOverStrategy = RollOverStrategy(maxBackupIndex),
          Policies = triggeringPolicy
        )
      }
    )
  }

  // Build one Appender based on the property value.
  def buildAppender(key: String, props: Properties): ValidatedNec[String, Appender] = {
    readProperty(key, props)
      .map {
        case "org.apache.log4j.ConsoleAppender" => buildConsole(key, props)
        case "com.tle.core.equella.runner.DailySizeRollingAppender" |
            "com.dytech.common.log4j.DailySizeRollingAppender" =>
          buildRollingFile(key, props)
        case "org.apache.log4j.RollingFileAppender" => buildRollingFile(key, props, false)
        case unsupported => Validated.invalidNec(s"Unsupported Appender $unsupported")
      }
      .getOrElse(Validated.invalidNec(s"Must specify an Appender for $key"))
  }

  /** Build a list of Appenders by extracting all the Appender definition keys and creating an
    * Appender for each key.
    *
    * The format of a valid appender definition key must be "log4j.appender.XXX" where "XXX" is the
    * appender name.
    *
    * @param props
    *   The property file which provides details of all Appenders
    * @return
    *   `ValidatedNec` where left is a list of error messages and right is a Map of Appenders
    *   grouped by their types.
    */
  def buildAppenders(props: Properties): ValidatedNec[String, Map[String, Seq[Appender]]] = {
    // Group a list of Appenders by their class name.
    def groupAppenders(appenders: List[Appender]): Map[String, Seq[Appender]] =
      appenders.groupBy(_.getClass.getSimpleName)

    props
      .stringPropertyNames()
      .asScala
      .filter(_.startsWith(appenderPrefix))
      .filter(_.split("\\.").length == 3)
      .map(buildAppender(_, props))
      .toList
      .sequence
      .map(groupAppenders)
  }
}
