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
import scala.jdk.OptionConverters._

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
    def asArrayList: util.ArrayList[A] = {
      val list = new util.ArrayList[A](iterable.size)
      iterable.foreach(list.add)
      list
    }
  }

  /** Extension method to convert Scala maps to mutable Java HashMaps.
    *
    * This is useful when working with legacy Java APIs that expect mutable maps rather than the
    * immutable views returned by `.asJava`. As typically oEQ beans use HashMaps for their map
    * fields, this provides a convenient way to convert Scala maps to the expected Java collection
    * type.
    *
    * @param map
    *   The Scala map to convert
    */
  implicit class HashMapConverter[K, V](private val map: Map[K, V]) extends AnyVal {

    /** Converts this Scala Map to a mutable Java HashMap.
      *
      * @return
      *   A new mutable HashMap containing all key-value pairs
      */
    def asHashMap: util.HashMap[K, V] = {
      val hashMap = new util.HashMap[K, V](map.size)
      map.foreach { case (k, v) => hashMap.put(k, v) }
      hashMap
    }
  }

  /** Converts an Option to a Java Optional by applying a mapping function.
    *
    * @param f
    *   function mapping each element to the target type
    * @param option
    *   the source Option
    * @tparam A
    *   the element type of the source Option
    * @tparam B
    *   the element type of the resulting Optional
    * @return
    *   a [[java.util.Optional]] containing the mapped value, or empty if the Option was [[None]]
    */
  def asOptional[A, B](f: A => B)(option: Option[A]): util.Optional[B] =
    option.map(f).toJava

  /** Converts an iterable to a mutable Java ArrayList by applying a mapping function.
    *
    * @param f
    *   function mapping each element to the target type
    * @param iterable
    *   the source iterable
    * @tparam A
    *   the element type of the source iterable
    * @tparam B
    *   the element type of the resulting ArrayList
    * @return
    *   a new mutable ArrayList containing all mapped elements
    */
  def asArrayList[A, B](f: A => B)(iterable: Iterable[A]): util.ArrayList[B] =
    iterable.map(f).asArrayList

  /** Converts an iterable to a mutable Java HashMap by applying a key-value mapping function.
    *
    * @param f
    *   function mapping each element to a key-value pair
    * @param iterable
    *   the source iterable
    * @tparam A
    *   the element type of the source iterable
    * @tparam K
    *   the key type of the resulting HashMap
    * @tparam V
    *   the value type of the resulting HashMap
    * @return
    *   a new mutable HashMap containing all mapped key-value pairs
    */
  def asHashMap[A, K, V](f: A => (K, V))(iterable: Iterable[A]): util.Map[K, V] =
    iterable.map(f).toMap.asHashMap

  /** Converts a Java `Date` to a Scala `LocalDateTime`. This is useful for converting Dates from
    * the Java world (e.g. from the database) to the Scala world; keeping in mind they may be null.
    */
  def toLocalDateTime(date: Date): Option[LocalDateTime] =
    Option(date).map(_.toInstant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime)

  /** Converts a Scala `LocalDateTime` to a Java `Date`. This is the inverse of [[toLocalDateTime]].
    */
  def toDate(ldt: LocalDateTime): Date =
    Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant)
}
