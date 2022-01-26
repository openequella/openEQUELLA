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
import scala.util.{Failure, Success, Try}

object PropertyHelper {

  /**
    * Get a String value from the property configurations by the supplied key.
    *
    * @param key Key of a property.
    * @param props The property configuration to be read.
    * @return An Option of String.
    */
  def readProperty(key: String, props: Properties): Option[String] =
    Option(props.getProperty(key))
      .filter(_.nonEmpty)

  /**
    * Get a boolean value from the property configurations by the supplied key.
    *
    * @param key Key of a property.
    * @param props The property configuration to be read.
    * @return An Option of Boolean.
    */
  def readBooleanProperty(key: String, props: Properties): Option[Boolean] =
    readProperty(key, props).map(s =>
      Try {
        s.toBoolean
      } match {
        case Success(value) => value
        case Failure(_)     => false
    })

  /**
    * Get an Int value from the property configurations by the supplied key.
    *
    * @param key Key of a property.
    * @param props The property configuration to be read.
    * @return An Option of Int.
    */
  def readIntProperty(key: String, props: Properties): Option[Int] =
    readProperty(key, props)
      .map(s =>
        Try {
          s.toInt
        } match {
          case Success(value) => value
          case Failure(_)     => 0
      })
}
