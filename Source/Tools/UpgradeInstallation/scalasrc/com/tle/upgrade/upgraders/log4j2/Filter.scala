package com.tle.upgrade.upgraders.log4j2

import java.util.Properties
import cats.implicits._
import cats.data.{Validated, ValidatedNec}
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.{readBooleanProperty, readProperty}
import org.apache.logging.log4j.spi.StandardLevel
import scala.collection.JavaConverters._
import org.apache.logging.log4j.core.Filter.Result

sealed trait Filter

case class ThresholdFilter(level: String) extends Filter

case class RegexFilter(regex: String, onMatch: String, onMismatch: String) extends Filter

object Filter {
  def getThresholdFilter(appender: String,
                         props: Properties): ValidatedNec[String, Option[ThresholdFilter]] = {
    def buildFilter(level: String) =
      Either
        .cond(StandardLevel.values().map(s => s.toString).contains(level),
              ThresholdFilter(level),
              "Unknown Threshold filter level")
        .toValidatedNec

    readProperty(s"${appender}.Threshold", props)
      .traverse(buildFilter)
  }

  def getRegexFilters(appender: String,
                      props: Properties): ValidatedNec[String, Option[Seq[RegexFilter]]] = {

    def buildFilter(key: String, props: Properties): ValidatedNec[String, RegexFilter] = {
      def filterResult(value: Boolean) =
        if (value) Result.NEUTRAL.toString else Result.DENY.toString

      def acceptOnMatch =
        readBooleanProperty(s"${key}.AcceptOnMatch", props)
          .toValidNec(s"Failed to find the value of AcceptOnMatch for ${key}")

      def stringToMatch =
        readProperty(s"${key}.StringToMatch", props)
          .map(regex => s".*${regex}.*")
          .toValidNec(s"Failed to find the value of StringToMatch for ${key}")

      props.getProperty(key) match {
        case "org.apache.log4j.varia.StringMatchFilter" =>
          (stringToMatch, acceptOnMatch).mapN {
            case (regex, onMatch) =>
              RegexFilter(regex = regex,
                          onMatch = filterResult(onMatch),
                          onMismatch = filterResult(!onMatch))
          }
        case unsupported => Validated.invalidNec(s"Unsupported filter ${unsupported}")
      }
    }
    // Regex for the format of an Appender filter definition.
    // For example: log4j.appender.FILE.filter.1
    val filterKeyRegex = s"^${appender}\\.filter\\.\\d{1}$$"

    props
      .stringPropertyNames()
      .asScala
      .filter(_.matches(filterKeyRegex))
      .map(buildFilter(_, props))
      .toList
      .sequence
      .map(Option(_).filter(_.nonEmpty))
  }

  def getFilters(appender: String,
                 props: Properties): ValidatedNec[String, Option[Map[String, Seq[Filter]]]] = {
    (getRegexFilters(appender, props), getThresholdFilter(appender, props)).mapN {
      case (regexFilters, thresholdFilter) =>
        // Group filters by their types.
        def group(filters: Seq[Filter]) = {
          filters.groupBy(_.getClass.getSimpleName)
        }

        (regexFilters ++ thresholdFilter.map(Seq(_)))
          .reduceOption(_ ++ _)
          .map(group)

    }
  }
}
