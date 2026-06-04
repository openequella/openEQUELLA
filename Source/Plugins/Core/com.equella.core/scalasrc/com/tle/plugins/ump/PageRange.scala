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

package com.tle.plugins.ump

/** A value object representing a page window with a limit and an offset.
  *
  * @param limit
  *   maximum number of results to return; must be non-negative
  * @param offset
  *   zero-based index of the first result to return; must be non-negative
  * @throws IllegalArgumentException
  *   if `limit` or `offset` is negative
  */
case class PageRange(limit: Int, offset: Int) {
  require(limit >= 0, s"limit must be non-negative, got $limit")
  require(offset >= 0, s"offset must be non-negative, got $offset")
}
