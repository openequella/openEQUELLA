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

/** Provide shared query parameters for both of the search and facet search endpoints.
  */
class BaseSearchParam {
  @ApiParam("Query string")
  @QueryParam("query")
  var query: String = _

  @ApiParam(
    "For details on structuring the where clause see https://github.com/openequella/openequella.github.io/blob/master/guides/RestAPIGuide.md#searching"
  )
  @QueryParam("whereClause")
  var whereClause: String = _

  @ApiParam(value = "List of collections. (ignored if hierarchy is set)")
  @QueryParam("collections")
  var collections: Array[String] = _

  @ApiParam(value = "An ISO date format (yyyy-MM-dd)")
  @QueryParam("modifiedBefore")
  var modifiedBefore: String = _

  @ApiParam(value = "An ISO date format (yyyy-MM-dd)")
  @QueryParam("modifiedAfter")
  var modifiedAfter: String = _

  @ApiParam(value = "An ID (not a username) of a user")
  @QueryParam("owner")
  var owner: String = _

  @ApiParam("List of MIME types to filter by")
  @QueryParam("mimeTypes")
  var mimeTypes: Array[String] = _

  @ApiParam(
    "List of search index key/value pairs to filter by. e.g. videothumb:true or realthumb:true."
  )
  @QueryParam("musts")
  var musts: Array[String] = _

  @ApiParam(value = "Filter by item status. (ignored if hierarchy is set)")
  @QueryParam("status")
  var status: Array[ItemStatus] = _

  @ApiParam(value = "Hierarchy topic compound UUID. Only used for hierarchy search.")
  @QueryParam("hierarchy")
  var hierarchy: String = _
}

/** Provide a full list of query parameters used in the search endpoint. Typically used with
  * \@BeanParam in order to aggregate lots of Search parameters instead of having a long list of
  * parameters.
  */
class SearchParam extends BaseSearchParam {

  @ApiParam("The first record of the search results to return")
  @QueryParam("start") @DefaultValue("0")
  var start: Int = _

  @ApiParam("The number of results to return")
  @QueryParam("length") @DefaultValue("10")
  var length: Int = _

  @ApiParam(
    value = "The order of the search results",
    allowableValues = "relevance,modified,name,rating,created,task_submitted,task_lastaction"
  )
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
    "Whether to include full attachment details in results. Including attachments incurs extra processing and can slow down response times."
  )
  @DefaultValue("true")
  @QueryParam("includeAttachments")
  var includeAttachments: Boolean = _

  @ApiParam(
    "An advanced search UUID. If a value is supplied, the collections in the advanced search will be used and the collections parameter will be ignored."
  )
  @QueryParam("advancedSearch")
  var advancedSearch: String = _

  @ApiParam("Single dynamic collection uuid (:virtualized value).")
  @QueryParam("dynaCollection")
  var dynaCollection: String = _
}

/** Similar to {{SearchParam}} but used in the faceted search endpoint.
  */
class FacetedSearchParam extends BaseSearchParam {
  @ApiParam(value = "List of schema nodes to facet over", required = true)
  @QueryParam("nodes")
  var nodes: Array[String] = _
}

/** Data structure to represent all the supported search criteria, which is typically used as the
  * type of POST request payload. It also supports the transformation from both [[SearchParam]] and
  * [[FacetedSearchParam]].
  */
case class SearchPayload(
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
    musts: Array[String] = Array(),
    hierarchy: Option[String]
)

object SearchPayload {
  def apply(searchParam: SearchParam): SearchPayload =
    SearchPayload(
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
      musts = searchParam.musts,
      hierarchy = Option(searchParam.hierarchy)
    )

  def apply(facetedSearchParam: FacetedSearchParam): SearchPayload =
    SearchPayload(
      query = Option(facetedSearchParam.query),
      collections = facetedSearchParam.collections,
      order = None,
      advancedSearch = None,
      whereClause = Option(facetedSearchParam.whereClause),
      status = facetedSearchParam.status,
      modifiedBefore = Option(facetedSearchParam.modifiedBefore),
      modifiedAfter = Option(facetedSearchParam.modifiedAfter),
      owner = Option(facetedSearchParam.owner),
      dynaCollection = None,
      mimeTypes = facetedSearchParam.mimeTypes,
      musts = facetedSearchParam.musts,
      hierarchy = Option(facetedSearchParam.hierarchy)
    )
}
