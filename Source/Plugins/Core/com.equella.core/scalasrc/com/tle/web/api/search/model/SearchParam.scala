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

import io.swagger.annotations.ApiParam
import javax.ws.rs.{DefaultValue, QueryParam}

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
            allowableValues = "relevance,modified,name,rating,created")
  @QueryParam("order")
  var order: String = ""

  @ApiParam("Reverse the order of the search results")
  @QueryParam("reverseOrder")
  var reverseOrder: Boolean = _

  @ApiParam(
    "An advanced search UUID. If a value is supplied, the collections in the advanced search will be used and the collections parameter will be ignored.")
  @QueryParam("advancedSearch")
  var advancedSearch: String = _

  @ApiParam(
    "For details on structuring the where clause see https://github.com/openequella/openequella.github.io/blob/master/guides/RestAPIGuide.md#searching")
  @QueryParam("whereClause")
  var whereClause: String = _

  @ApiParam(value = "Filter by item status.",
            allowableValues =
              "DRAFT,LIVE,REJECTED,MODERATING,ARCHIVED,SUSPENDED,DELETED,REVIEW,PERSONAL")
  @QueryParam("status")
  var status: Array[String] = _

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
}
