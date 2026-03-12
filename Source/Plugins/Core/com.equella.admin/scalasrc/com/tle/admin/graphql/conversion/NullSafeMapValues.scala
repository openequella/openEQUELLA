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

package com.tle.admin.graphql.conversion

import scala.jdk.CollectionConverters._

/** Wraps a potentially null Java `Map` to allow null-safe conversion of its values with infix
  * notation.
  *
  * Similar to [[NullSafeList]], but for Java `Map` fields where only the values need to be
  * converted and the keys can be discarded. Usage:
  * {{{
  * NullSafeMapValues(bundle.getStrings) convert fromLanguageString
  * }}}
  *
  * Note: This is not a value class (`AnyVal`) because multi-type-parameter constructors are not
  * supported for value classes.
  *
  * @param underlying
  *   A potentially null Java Map
  */
class NullSafeMapValues[K, V](private val underlying: java.util.Map[K, V]) {

  /** Converts each value using `f`, returning `List.empty` if the underlying map is null.
    */
  def convert[B](f: V => B): List[B] =
    Option(underlying).map(_.asScala.values.map(f).toList).getOrElse(List.empty)
}

object NullSafeMapValues {
  def apply[K, V](map: java.util.Map[K, V]): NullSafeMapValues[K, V] =
    new NullSafeMapValues(map)
}
