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
}
