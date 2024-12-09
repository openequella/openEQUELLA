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

import cats.data.ValidatedNec
import cats.implicits._
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.readProperty

import java.util.Properties
import scala.jdk.CollectionConverters._

case class AppenderReference(ref: String)

case class RootLogger(level: String, AppenderRef: Seq[AppenderReference])
case class NormalLogger(
    name: String,
    level: String,
    AppenderRef: Option[Seq[AppenderReference]] = None
)

case class LoggerConfig(Root: RootLogger, Logger: List[NormalLogger])

object Logger {

  /** Build the Root Logger.
    *
    * @param props
    *   Property file which provides details of the Root Logger.
    * @return
    *   A RootLogger or a list of errors captured the build.
    */
  def buildRootLogger(props: Properties): ValidatedNec[String, RootLogger] = {
    readProperty("log4j.rootLogger", props)
      .map(_.split(","))
      .map {
        case Array(level, firstAppender, others @ _*) =>
          val refs = (others :+ firstAppender).map(_.trim).map(AppenderReference)
          Right(RootLogger(level, refs))
        case _ => Left("Failed to find Appenders for Root logger")
      }
      .getOrElse(Left("Missing Root logger"))
      .toValidatedNec
  }

  /** Build a list of normal Loggers.
    *
    * @param props
    *   Property file which provides details of all the normal Loggers.
    * @return
    *   A list of NormalLogger or a list of errors captured the build.
    */
  def buildNormalLogger(props: Properties): ValidatedNec[String, List[NormalLogger]] = {
    val loggerPrefix = "log4j.logger."

    def build(key: String) = {
      val name = key.replace(loggerPrefix, "")

      props.getProperty(key).split(",").map(_.trim) match {
        case Array(level, firstAppender, others @ _*) =>
          Right(
            NormalLogger(
              name = name,
              level = level,
              AppenderRef = Option((others :+ firstAppender).map(AppenderReference))
            )
          )
        // A normal logger can have a level without an Appender.
        case Array(level) => Right(NormalLogger(name = name, level = level))
        case _            => Left(s"Missing logger level for $name")
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

  /** Build a Root Logger and a list of Normal Loggers.
    *
    * @param props
    *   Property file which provides details of all the normal Loggers.
    * @return
    *   LoggerConfig including a Root Logger and a list of Normal Loggers.
    */
  def buildLoggerConfig(props: Properties): ValidatedNec[String, LoggerConfig] =
    (buildRootLogger(props), buildNormalLogger(props))
      .mapN(LoggerConfig)
}
