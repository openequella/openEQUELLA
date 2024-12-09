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

package com.tle.web.api.search.service

import com.tle.common.search.DefaultSearch
import com.tle.core.guice.Bind
import com.tle.legacy.LegacyGuice
import com.tle.web.api.search.CSVHeader
import com.tle.web.api.search.ExportCSVHelper.{buildCSVRow, convertSearchResultToXML}
import com.tle.web.api.search.SearchHelper.search
import org.springframework.transaction.annotation.Transactional
import javax.inject.Singleton
import scala.jdk.CollectionConverters._

@Bind
@Singleton
class ExportService {

  /** Export search results as CSV contents. The full result is chunked by paging and streaming.
    * @param defaultSearch
    *   A set of search criteria
    * @param searchAttachments
    *   Whether to search attachments.
    * @param headers
    *   A list of CSV headers
    * @param writeRow
    *   Function used to output CSV contents
    */
  @Transactional
  def export(
      defaultSearch: DefaultSearch,
      searchAttachments: Boolean,
      headers: List[CSVHeader],
      writeRow: (String) => Unit
  ): Unit = {
    // Get the total count first. As we only do one search, the result of 'countsFromFilters'
    // must be an array that has only one element.
    val count     = LegacyGuice.freeTextService.countsFromFilters(List(defaultSearch).asJava)(0)
    val chunkSize = 100
    val starts: Seq[Int] =
      for (i <- 0 until count / chunkSize + 1) yield i * chunkSize

    starts.foreach(start => {
      convertSearchResultToXML(
        search(defaultSearch, start, chunkSize, searchAttachments).getSearchResults.asScala.toList
      )
        .foreach(xml => {
          writeRow(s"${buildCSVRow(xml, headers)}")
        })
    })
  }
}
