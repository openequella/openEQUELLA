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

package com.tle.web.api.search.model

/** Represents the results for a search query.
  *
  * @param start
  *   The starting offset into the total search results
  * @param length
  *   How many results can be found in `results`
  * @param available
  *   The maximum number of results available for paging
  * @param results
  *   The individual items which match the search
  * @param highlight
  *   List of words to use to highlight when displaying the results
  */
case class SearchResult[T](
    start: Int,
    length: Int,
    available: Int,
    results: List[T],
    highlight: List[String]
)
