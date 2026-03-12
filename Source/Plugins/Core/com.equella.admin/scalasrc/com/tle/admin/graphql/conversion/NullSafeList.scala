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

/** Wraps a potentially null Java `List` to allow null-safe conversion of its elements with infix
  * notation.
  *
  * Legacy Java beans often have `List` fields that may be null rather than empty. This wrapper
  * encapsulates the `Option`/`getOrElse` boilerplate, allowing concise conversion such as:
  * {{{
  * NullSafeList(schema.getExportTransforms) convert fromSchemaTransform
  * }}}
  *
  * @param underlying
  *   A potentially null Java List
  */
class NullSafeList[A](val underlying: java.util.List[A]) {

  /** Converts each element using `f`, returning `List.empty` if the underlying list is null.
    */
  def convert[B](f: A => B): List[B] =
    Option(underlying).map(_.asScala.map(f).toList).getOrElse(List.empty)
}

object NullSafeList {
  def apply[A](list: java.util.List[A]): NullSafeList[A] = new NullSafeList(list)
}
