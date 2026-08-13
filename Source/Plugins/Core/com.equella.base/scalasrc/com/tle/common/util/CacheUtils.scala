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

package com.tle.common.util

import scala.annotation.varargs

object CacheUtils {

  /** Builds an unambiguous composite cache key from arbitrary parts.
    *
    * Each part is encoded with a length prefix so values containing separators cannot collide. A
    * null part is encoded as `-1:`.
    *
    * For example:
    * {{{
    * buildCacheKey("ab", "c") == "|2:ab|1:c|"
    * buildCacheKey(null)       == "|-1:|"
    * }}}
    */
  @varargs
  def buildCacheKey(parts: AnyRef*): String =
    if (parts.isEmpty) ""
    else parts.iterator.map(encodePart).mkString("|", "|", "|")

  private def encodePart(part: AnyRef): String =
    Option(part) match {
      case None        => "-1:"
      case Some(value) =>
        val stringValue = String.valueOf(value)
        s"${stringValue.length}:$stringValue"
    }
}
