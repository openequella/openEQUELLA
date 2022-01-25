package com.tle.upgrade.upgraders.log4j2

import cats.data.{Validated, ValidatedNec}
import cats.implicits._
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.readProperty

import java.util.Properties
import scala.collection.JavaConverters._

case class AppenderReference(ref: String)

case class RootLogger(level: String, AppenderRef: Seq[AppenderReference])
case class NormalLogger(name: String,
                        level: String,
                        AppenderRef: Option[Seq[AppenderReference]] = None)

case class LoggerConfig(Root: RootLogger, Logger: List[NormalLogger])

object Logger {
  private def buildRootLogger(props: Properties): ValidatedNec[String, RootLogger] = {
    readProperty("log4j.rootLogger", props)
      .map(_.split(","))
      .map {
        case Array(level, firstAppender, others @ _*) =>
          val refs = (others :+ firstAppender).map(_.trim).map(AppenderReference)
          Right((RootLogger(level, refs)))
        case _ => Left("Failed to find Appenders for Root logger")
      }
      .getOrElse(Left("Missing Root logger"))
      .toValidatedNec
  }

  private def buildNormalLogger(props: Properties): ValidatedNec[String, List[NormalLogger]] = {
    val loggerPrefix = "log4j.logger."

    def build(key: String) = {
      val name = key.replace(loggerPrefix, "")

      props.getProperty(key).split(",").map(_.trim) match {
        case Array(level, firstAppender, others @ _*) =>
          Right(
            NormalLogger(name = name,
                         level = level,
                         AppenderRef = Option((others :+ firstAppender).map(AppenderReference))))
        // A normal logger can have a level without an Appender.
        case Array(level) => Right(NormalLogger(name = name, level = level))
        case _            => Left(s"Missing logger level for ${name}")
      }
    }

    props
      .stringPropertyNames()
      .asScala
      .filter(_.startsWith(loggerPrefix))
      .map(build)
      .toList
      .traverse(_.toValidatedNec)
  }

  def buildLoggerConfig(props: Properties): ValidatedNec[String, LoggerConfig] =
    (buildRootLogger(props), buildNormalLogger(props))
      .mapN(LoggerConfig)
}
