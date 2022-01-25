package com.tle.upgrade.upgraders.log4j2

import cats.data.ValidatedNec
import cats.implicits._
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.{readBooleanProperty, readProperty}

import java.util.Properties

sealed trait Layout
case class PatternLayout(Pattern: String) extends Layout
case class HTMLLayout(title: Option[String],
                      datePattern: Option[String] = None,
                      locationInfo: Option[Boolean])
    extends Layout

object Layout {
  def getLayout(layoutKey: String, props: Properties): ValidatedNec[String, Layout] = {
    readProperty(layoutKey, props)
      .map {
        case "org.apache.log4j.PatternLayout" =>
          readProperty(s"${layoutKey}.ConversionPattern", props)
            .map(PatternLayout)
            .toRight(s"Failed to find layout pattern for ${layoutKey}")

        case "org.apache.log4j.HTMLLayout" | "com.dytech.common.log4j.HTMLLayout2" |
            "com.tle.core.equella.runner.HTMLLayout3" =>
          val title = readProperty(s"${layoutKey}.title", props)
          val locationInfo =
            readBooleanProperty(s"${layoutKey}.LocationInfo", props)
          val datePattern = readProperty(s"${layoutKey}.datePattern", props)
          Right(HTMLLayout(title, datePattern, locationInfo))

        case unsupported => Left(s"Unsupported layout $unsupported")
      }
      .getOrElse(Left(s"Failed to find layout for $layoutKey"))
      .toValidatedNec
  }
}
