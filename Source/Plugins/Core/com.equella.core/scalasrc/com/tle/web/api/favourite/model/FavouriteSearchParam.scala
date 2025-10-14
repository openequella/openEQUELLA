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

package com.tle.web.api.favourite.model

import io.swagger.annotations.ApiParam
import javax.ws.rs.{DefaultValue, QueryParam}

/** Provide a full list of query parameters used in the favourite search endpoint.
  */
class FavouriteSearchParam {
  @ApiParam("Query string")
  @QueryParam("query")
  var query: String = _

  @ApiParam("The first record of the search results to return")
  @QueryParam("start") @DefaultValue("0")
  var start: Int = _

  @ApiParam("The number of results to return")
  @QueryParam("length") @DefaultValue("10")
  var length: Int = _

  @ApiParam(value = "An ISO date format (yyyy-MM-dd)")
  @QueryParam("addedBefore")
  var addedBefore: String = _

  @ApiParam(value = "An ISO date format (yyyy-MM-dd)")
  @QueryParam("addedAfter")
  var addedAfter: String = _

  @ApiParam(
    value = "The order of the search results",
    allowableValues = "name,added_at"
  )
  @QueryParam("order")
  var order: String = _
}

/** Data structure with Option wrapping to represent all the supported search criteria to get
  * favourite search. It also supports the transformation from [[FavouriteSearchParam]].
  */
case class FavouriteSearchPayload(
    query: Option[String],
    start: Int = 0,
    length: Int = 10,
    addedBefore: Option[String],
    addedAfter: Option[String],
    order: Option[String]
)

object FavouriteSearchPayload {
  def apply(searchParam: FavouriteSearchParam): FavouriteSearchPayload =
    FavouriteSearchPayload(
      query = Option(searchParam.query),
      start = searchParam.start,
      length = searchParam.length,
      addedBefore = Option(searchParam.addedBefore),
      addedAfter = Option(searchParam.addedAfter),
      order = Option(searchParam.order)
    )
}
