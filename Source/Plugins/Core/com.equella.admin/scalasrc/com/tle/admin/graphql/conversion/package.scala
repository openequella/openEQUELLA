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

package com.tle.admin.graphql

import java.time.LocalDateTime
import java.util
import java.util.Date
import scala.jdk.CollectionConverters._

/** This package contains support for converting types between the GraphQL schema and internal
  * classes used in the AdminConsole.
  */
package object conversion {

  /** Extension method to allow functional conversion with infix notation.
    *
    * @param a
    *   The value to convert
    */
  implicit class Converter[A](val a: A) extends AnyVal {
    def convert[B](f: A => B): B = f(a)
  }

  /** Extension method to convert Scala iterables to mutable Java ArrayLists.
    *
    * This is useful when working with legacy Java APIs that expect mutable lists rather than the
    * immutable views returned by `.asJava`. As typically oEQ beans use ArrayLists for their
    * collection fields, this provides a convenient way to convert Scala collections to the expected
    * Java collection type.
    *
    * @param iterable
    *   The Scala iterable to convert
    */
  implicit class ArrayListConverter[A](private val iterable: Iterable[A]) extends AnyVal {

    /** Converts this Scala iterable to a mutable Java ArrayList.
      *
      * @return
      *   A new mutable ArrayList containing all elements
      */
    def asArrayList: util.ArrayList[A] = new util.ArrayList[A](iterable.toSeq.asJava)
  }

  /** Converts a Java `Date` to a Scala `LocalDateTime`. This is useful for converting Dates from
    * the Java world (e.g. from the database) to the Scala world; keeping in mind they may be null.
    */
  def toLocalDateTime(date: Date): Option[LocalDateTime] =
    Option(date).map(_.toInstant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime)
}
