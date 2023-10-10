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

import com.tle.beans.item.ItemStatus
import io.swagger.annotations.ApiParam

import javax.ws.rs.{DefaultValue, QueryParam}

/**
  * This class is typically used with @BeanParam in order to aggregate lots of Search parameters instead of
  * having a long list of parameters.
  */
class SearchParam {
  @ApiParam("Query string")
  @QueryParam("query")
  var query: String = _

  @ApiParam("The first record of the search results to return")
  @QueryParam("start") @DefaultValue("0")
  var start: Int = _

  @ApiParam("The number of results to return")
  @QueryParam("length") @DefaultValue("10")
  var length: Int = _

  @ApiParam("List of collections")
  @QueryParam("collections")
  var collections: Array[String] = _

  @ApiParam(value = "The order of the search results",
            allowableValues =
              "relevance,modified,name,rating,created,task_submitted,task_lastaction")
  @QueryParam("order")
  var order: String = _

  @ApiParam("Reverse the order of the search results")
  @QueryParam("reverseOrder")
  var reverseOrder: Boolean = _

  @ApiParam("Whether to search attachments or not")
  @DefaultValue("true")
  @QueryParam("searchAttachments")
  var searchAttachments: Boolean = _

  @ApiParam(
    "Whether to include full attachment details in results. Including attachments incurs extra processing and can slow down response times.")
  @DefaultValue("true")
  @QueryParam("includeAttachments")
  var includeAttachments: Boolean = _

  @ApiParam(
    "An advanced search UUID. If a value is supplied, the collections in the advanced search will be used and the collections parameter will be ignored.")
  @QueryParam("advancedSearch")
  var advancedSearch: String = _

  @ApiParam(
    "For details on structuring the where clause see https://github.com/openequella/openequella.github.io/blob/master/guides/RestAPIGuide.md#searching")
  @QueryParam("whereClause")
  var whereClause: String = _

  @ApiParam(value = "Filter by item status.")
  @QueryParam("status")
  var status: Array[ItemStatus] = _

  @ApiParam("An ISO date format (yyyy-MM-dd)")
  @QueryParam("modifiedBefore")
  var modifiedBefore: String = _

  @ApiParam("An ISO date format (yyyy-MM-dd)")
  @QueryParam("modifiedAfter")
  var modifiedAfter: String = _

  @ApiParam("An ID of a user")
  @QueryParam("owner")
  var owner: String = _

  @ApiParam("Single dynamic collection uuid (:virtualized value)")
  @QueryParam("dynaCollection")
  var dynaCollection: String = _

  @ApiParam("List of MIME types to filter by")
  @QueryParam("mimeTypes")
  var mimeTypes: Array[String] = _

  @ApiParam(
    "List of search index key/value pairs to filter by. e.g. videothumb:true or realthumb:true.")
  @QueryParam("musts")
  var musts: Array[String] = _
}

/**
  * Data structure to represent all the supported search criteria, which is typically used as the type of POST request payload.
  * It also supports the transformation from [[SearchParam]].
  */
case class SearchCriteria(
    query: Option[String],
    start: Int = 0,
    length: Int = 10,
    collections: Array[String] = Array(),
    order: Option[String],
    reverseOrder: Boolean = false,
    searchAttachments: Boolean = true,
    includeAttachments: Boolean = true,
    advancedSearch: Option[String],
    whereClause: Option[String],
    status: Array[ItemStatus] = Array(),
    modifiedBefore: Option[String],
    modifiedAfter: Option[String],
    owner: Option[String],
    dynaCollection: Option[String],
    mimeTypes: Array[String] = Array(),
    musts: Array[String] = Array()
)

object SearchCriteria {

  def apply(searchParam: SearchParam): SearchCriteria =
    SearchCriteria(
      query = Option(searchParam.query),
      start = searchParam.start,
      length = searchParam.length,
      collections = searchParam.collections,
      order = Option(searchParam.order),
      reverseOrder = searchParam.reverseOrder,
      searchAttachments = searchParam.searchAttachments,
      includeAttachments = searchParam.includeAttachments,
      advancedSearch = Option(searchParam.advancedSearch),
      whereClause = Option(searchParam.whereClause),
      status = searchParam.status,
      modifiedBefore = Option(searchParam.modifiedBefore),
      modifiedAfter = Option(searchParam.modifiedAfter),
      owner = Option(searchParam.owner),
      dynaCollection = Option(searchParam.dynaCollection),
      mimeTypes = searchParam.mimeTypes,
      musts = searchParam.musts
    )
}
