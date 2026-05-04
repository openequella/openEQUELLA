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

package com.tle.web.remoting.graphql.schema

import java.time.LocalDateTime
import java.util.Date
import scala.jdk.CollectionConverters._

/** This package holds all the custom types for the GraphQL API.
  */
package object types {

  /** Converts a Java `Date` to a Scala `LocalDateTime`. This is useful for converting Dates from
    * the Java world (e.g. from the database) to the Scala world; keeping in mind they may be null.
    */
  def toLocalDateTime(date: Date): Option[LocalDateTime] =
    Option(date).map(_.toInstant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime)

  /** Converts a nullable Java `Collection` to a Scala `List`, applying a mapping function to each
    * element. Returns an empty list if the Java collection is `null`.
    */
  def convertJavaList[A, B](list: java.util.Collection[A])(f: A => B): List[B] =
    Option(list).map(_.asScala.toList.map(f)).getOrElse(List.empty)
}
