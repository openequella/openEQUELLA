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
import scala.collection.JavaConverters._

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

case class Console(name: String,
                   target: String,
                   layout: Layout,
                   Filters: Option[Map[String, Seq[Filter]]])
    extends Appender

case class RollingFile(name: String,
                       fileName: String,
                       filePattern: String,
                       immediateFlush: Boolean,
                       append: Boolean,
                       layout: Layout,
                       Filters: Option[Map[String, Seq[Filter]]],
                       Policies: Map[String, RollingPolicy],
                       DefaultRollOverStrategy: RollOverStrategy)
    extends Appender

object Appender {
  val appenderPrefix = "log4j.appender."

  def getLayoutMap(layout: Layout): java.util.Map[String, Layout] =
    Map(layout.getClass.getSimpleName -> layout).asJava

  def appenderName(key: String): String = key.replace(appenderPrefix, "")

  def buildConsole(key: String, props: Properties): ValidatedNec[String, Console] = {
    def target =
      readProperty(s"${key}.Target", props)
      // If target is "system.out", use "system_out" instead.
        .filter(_.toLowerCase != "system.out")
        .getOrElse("SYSTEM_OUT")

    (getLayout(s"${key}.layout", props), getFilters(key, props))
      .mapN((layout, filters) => Console(appenderName(key), target, layout, filters))
  }

  def buildRollingFile(key: String, props: Properties): ValidatedNec[String, RollingFile] = {
    def fileName =
      readProperty(s"${key}.File", props)
        .toValidNec(s"Must specify a file name for ${key}")

    def immediateFlush =
      readBooleanProperty(s"${key}.ImmediateFlush", props).getOrElse(true)

    def append =
      readBooleanProperty(s"${key}.Append", props).getOrElse(true)

    def maxFileSize =
      readProperty(s"${key}.MaxFileSize", props)
        .getOrElse("10MB")

    def maxBackupIndex =
      readIntProperty(s"${key}.MaxBackupIndex", props)
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
          s"${Option(dir).getOrElse("")}${defaultPattern}/${filename}-%i.${ext}"
        case name => s"${defaultPattern}/${name}-%i"
      }
    }

    (fileName, getLayout(s"${key}.layout", props), getFilters(key, props)).mapN(
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
          Policies = Map("TimeBasedTriggeringPolicy" -> TimeBasedTriggeringPolicy(),
                         "SizeBasedTriggeringPolicy" -> SizeBasedTriggeringPolicy(maxFileSize))
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
            "com.dytech.common.log4j.DailySizeRollingAppender" |
            "org.apache.log4j.RollingFileAppender" =>
          buildRollingFile(key, props)
        case unsupported => Validated.invalidNec(s"Unsupported Appender ${unsupported}")
      }
      .getOrElse(Validated.invalidNec(s"Must specify an Appender for $key"))
  }

  // Build a list of Appenders by extracting all the Appender definition keys and creating
  // an Appender for each key.
  // The format of a valid appender definition key must be "log4j.appender.XXX" where
  // "XXX" is the appender name.
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
