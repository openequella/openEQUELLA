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

import java.util.Properties
import cats.implicits._
import cats.data.{Validated, ValidatedNec}
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.{readBooleanProperty, readProperty}
import org.apache.logging.log4j.spi.StandardLevel
import scala.jdk.CollectionConverters._
import org.apache.logging.log4j.core.Filter.Result

sealed trait Filter

case class ThresholdFilter(level: String) extends Filter

case class RegexFilter(regex: String, onMatch: String, onMismatch: String) extends Filter

object Filter {

  /** Find out if an Appender is configured to use ThresholdFilter. If yes, build a ThresholdFilter.
    *
    * @param appender
    *   Appender for which a ThresholdFilter is built.
    * @param props
    *   Property file providing details of the ThresholdFilter.
    * @return
    *   A ThresholdFilter or None, or a list of errors captured during the build.
    */
  def getThresholdFilter(
      appender: String,
      props: Properties
  ): ValidatedNec[String, Option[ThresholdFilter]] = {
    def buildFilter(level: String) =
      Either
        .cond(
          StandardLevel.values().map(s => s.toString).contains(level),
          ThresholdFilter(level),
          "Unknown Threshold filter level"
        )
        .toValidatedNec

    readProperty(s"$appender.Threshold", props)
      .traverse(buildFilter)
  }

  /** Find out if an Appender is configured to use RegexFilter. If yes, build a list of RegexFilter.
    *
    * @param appender
    *   Appender for which a list of RegexFilter is built.
    * @param props
    *   Property file providing details of the RegexFilter.
    * @return
    *   A list of RegexFilter or None, or a list of errors captured during the build.
    */
  def getRegexFilters(
      appender: String,
      props: Properties
  ): ValidatedNec[String, Option[Seq[RegexFilter]]] = {

    def buildFilter(key: String, props: Properties): ValidatedNec[String, RegexFilter] = {
      def filterResult(value: Boolean) =
        if (value) Result.NEUTRAL.toString else Result.DENY.toString

      def acceptOnMatch =
        readBooleanProperty(s"$key.AcceptOnMatch", props)
          .toValidNec(s"Failed to find the value of AcceptOnMatch for $key")

      def stringToMatch =
        readProperty(s"$key.StringToMatch", props)
          .map(regex => s".*$regex.*")
          .toValidNec(s"Failed to find the value of StringToMatch for $key")

      props.getProperty(key) match {
        case "org.apache.log4j.varia.StringMatchFilter" =>
          (stringToMatch, acceptOnMatch).mapN((regex, onMatch) =>
            RegexFilter(
              regex = regex,
              onMatch = filterResult(onMatch),
              onMismatch = filterResult(!onMatch)
            )
          )
        case unsupported => Validated.invalidNec(s"Unsupported filter $unsupported")
      }
    }

    // Regex for the format of an Appender filter definition.
    // For example: log4j.appender.FILE.filter.1
    val filterKeyRegex = s"^$appender\\.filter\\.\\d{1}$$"

    props
      .stringPropertyNames()
      .asScala
      .filter(_.matches(filterKeyRegex))
      .map(buildFilter(_, props))
      .toList
      .sequence
      .map(Option(_).filter(_.nonEmpty))
  }

  /** Find out if an Appender is configured to any filter. If yes, build all the filters
    *
    * @param appender
    *   Appender for which a list of RegexFilter is built.
    * @param props
    *   Property file providing details of all the filters.
    * @return
    *   None if no filter is used, or a Map where key is the filter type and value is a list of the
    *   filters. Or a list of errors captured during the build.
    */
  def getFilters(
      appender: String,
      props: Properties
  ): ValidatedNec[String, Option[Map[String, Seq[Filter]]]] = {
    // Group filters by their types.
    def group(filters: Seq[Filter]) = {
      filters.groupBy(_.getClass.getSimpleName)
    }

    (getRegexFilters(appender, props), getThresholdFilter(appender, props)).mapN(
      (regexFilters, thresholdFilter) =>
        (regexFilters ++ thresholdFilter.map(Seq(_)))
          .reduceOption(_ ++ _)
          .map(group)
    )
  }
}
