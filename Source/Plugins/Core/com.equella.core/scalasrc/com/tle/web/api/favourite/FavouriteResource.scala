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

package com.tle.web.api.favourite

import com.tle.beans.item.ItemId
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.favourites.bean.FavouriteSearch
import java.util.Date
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam}

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{DELETE, POST, Path, PathParam, Produces, QueryParam}
import scala.jdk.CollectionConverters._

/** Model class for Items to be saved to user's favourites.
  * @param keywords
  *   Tags of this Favourite Item
  * @param isAlwaysLatest
  *   Whether this Favourite Item uses latest Item version
  * @param itemID
  *   ID of the Item
  * @param bookmarkID
  *   ID of the related Bookmark
  */
case class FavouriteItemModel(
    itemID: String,
    keywords: Array[String],
    isAlwaysLatest: Boolean,
    bookmarkID: Long
)

/** Model class for search definitions to be saved to user's favourites.
  * @param id
  *   ID of a search definition. The value is None before the search definition persists to DB.
  * @param name
  *   Name of a search definition.
  * @param url
  *   Path to new Search UI, including all query strings.
  */
case class FavouriteSearchModel(id: Option[Long], name: String, url: String)

@Path("favourite")
@Produces(Array("application/json"))
@Api("Favourite")
class FavouriteResource {
  private val bookmarkService = LegacyGuice.bookmarkService
  private val itemService     = LegacyGuice.itemService

  @POST
  @Path("/item")
  @ApiOperation(
    value = "Add one Item to user's favourites",
    notes = "This operation is essentially adding a new bookmark.",
    response = classOf[FavouriteItemModel]
  )
  def addFavouriteItem(favouriteItem: FavouriteItemModel): Response = {
    // ItemNotFoundException will be thrown by itemService if there is no Item matching this
    // item ID so we don't validate item ID here again.
    val item = itemService.get(new ItemId(favouriteItem.itemID))
    val newBookmark =
      bookmarkService.add(item, favouriteItem.keywords.toSet.asJava, favouriteItem.isAlwaysLatest)
    Response
      .status(Status.CREATED)
      .entity(
        FavouriteItemModel(
          newBookmark.getItem.getItemId.toString,
          newBookmark.getKeywords.asScala.toArray,
          newBookmark.isAlwaysLatest,
          newBookmark.getId
        )
      )
      .build()
  }

  @DELETE
  @Path("/item/{id}")
  @ApiOperation(
    value = "Delete one Item from user's favourites",
    notes = "This operation is essentially deleting a bookmark."
  )
  def deleteFavouriteItem(@ApiParam("Bookmark ID") @PathParam("id") id: Long): Response = {
    Option(bookmarkService.getById(id)) match {
      case Some(_) =>
        bookmarkService.delete(id)
        Response.status(Status.NO_CONTENT).build()
      case None =>
        ApiErrorResponse
          .resourceNotFound(s"No Bookmark matching ID: ${id}")
    }
  }

  @POST
  @Path("/search")
  @ApiOperation(
    value = "Add a search definition to user's search favourites",
    response = classOf[FavouriteSearchModel]
  )
  def addFavouriteSearch(searchInfo: FavouriteSearchModel): Response = {
    val favouriteSearch = new FavouriteSearch
    favouriteSearch.setName(searchInfo.name)
    favouriteSearch.setUrl(searchInfo.url)
    favouriteSearch.setInstitution(CurrentInstitution.get())
    favouriteSearch.setDateModified(new Date())
    favouriteSearch.setOwner(CurrentUser.getUserID)
    val newFavouriteSearch = LegacyGuice.favouriteSearchService.save(favouriteSearch)

    Response
      .status(Status.CREATED)
      .entity(
        FavouriteSearchModel(
          Option(newFavouriteSearch.getId),
          newFavouriteSearch.getName,
          newFavouriteSearch.getUrl
        )
      )
      .build()
  }
}
