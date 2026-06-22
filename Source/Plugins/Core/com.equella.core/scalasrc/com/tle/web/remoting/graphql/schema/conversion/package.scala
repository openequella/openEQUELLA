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

/** Provides converters for transforming GraphQL schema types to their corresponding
  * Hibernate/domain entity representations.
  *
  * This package follows a consistent pattern where each converter object provides methods to
  * transform immutable GraphQL input types into mutable Java entity objects suitable for
  * persistence.
  */
package object conversion {

  /** Extension method to allow functional conversion with infix notation.
    *
    * Usage:
    * {{{
    *   val result = inputValue convert toOutputType
    *   // Equivalent to: toOutputType(inputValue)
    * }}}
    *
    * @param a
    *   the value to convert
    * @tparam A
    *   the type of the value to convert
    */
  implicit class Converter[A](val a: A) extends AnyVal {

    /** Applies the conversion function to the wrapped value.
      *
      * @param f
      *   the conversion function
      * @tparam B
      *   the target type
      * @return
      *   the converted value
      */
    def convert[B](f: A => B): B = f(a)
  }

  /** Extension method to convert Scala iterables to mutable Java [[java.util.ArrayList]]s.
    *
    * oEQ Java beans use [[java.util.ArrayList]] for their collection fields, including those stored
    * via `xstream_immutable` Hibernate types. Using `.asJava` from
    * [[scala.jdk.CollectionConverters]] returns a Scala-backed view that XStream cannot safely
    * serialise. This extension produces a proper mutable [[java.util.ArrayList]] instead.
    *
    * @param iterable
    *   the Scala iterable to convert
    * @tparam A
    *   the element type
    */
  implicit class ArrayListConverter[A](private val iterable: Iterable[A]) extends AnyVal {

    /** Converts this Scala iterable to a new mutable [[java.util.ArrayList]].
      *
      * @return
      *   a new mutable [[java.util.ArrayList]] containing all elements
      */
    def asArrayList: java.util.ArrayList[A] = {
      val list = new java.util.ArrayList[A](iterable.size)
      iterable.foreach(list.add)
      list
    }
  }

  /** Extension method to convert Scala maps to mutable Java [[java.util.HashMap]]s.
    *
    * oEQ Java beans use [[java.util.HashMap]] for their map fields, including those stored via
    * `xstream_immutable` Hibernate types. Using `.asJava` from [[scala.jdk.CollectionConverters]]
    * returns a Scala-backed view that XStream cannot safely serialise. This extension produces a
    * proper mutable [[java.util.HashMap]] instead.
    *
    * @param map
    *   the Scala map to convert
    * @tparam K
    *   the key type
    * @tparam V
    *   the value type
    */
  implicit class HashMapConverter[K, V](private val map: Map[K, V]) extends AnyVal {

    /** Converts this Scala [[Map]] to a new mutable [[java.util.HashMap]].
      *
      * @return
      *   a new mutable [[java.util.HashMap]] containing all key-value pairs
      */
    def asHashMap: java.util.HashMap[K, V] = {
      val hashMap = new java.util.HashMap[K, V](map.size)
      map.foreach { case (k, v) => hashMap.put(k, v) }
      hashMap
    }
  }
}
