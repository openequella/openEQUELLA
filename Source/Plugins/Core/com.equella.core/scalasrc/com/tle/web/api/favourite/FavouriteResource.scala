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
import scala.collection.JavaConverters._

/**
  * Provide basic information of a favourite Item.
  * @param keywords Tags of this Favourite Item
  * @param isAlwaysLatest Whether this Favourite Item uses latest Item version
  * @param itemID ID of the Item
  * @param bookmarkID ID of the related Bookmark
  */
case class FavouriteItem(itemID: String,
                         keywords: Array[String],
                         isAlwaysLatest: Boolean,
                         bookmarkID: Long)

@Path("favourite")
@Produces(Array("application/json"))
@Api("Favourite")
class FavouriteResource {
  private val bookmarkService = LegacyGuice.bookmarkService
  private val itemService     = LegacyGuice.itemService

  @POST
  @Path("/item")
  @ApiOperation(value = "Add one Item to user's favourites",
                notes = "This operation is essentially adding a new bookmark.",
                response = classOf[FavouriteItem])
  def addFavouriteItem(favouriteItem: FavouriteItem): Response = {
    // ItemNotFoundException will be thrown by itemService if there is no Item matching this
    // item ID so we don't validate item ID here again.
    val item = itemService.get(new ItemId(favouriteItem.itemID))
    val newBookmark =
      bookmarkService.add(item, favouriteItem.keywords.toSet.asJava, favouriteItem.isAlwaysLatest)
    Response
      .status(Status.CREATED)
      .entity(
        FavouriteItem(
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
    notes = "This operation is essentially deleting a bookmark.",
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
  @ApiOperation(value = "Add one search to user's favourites", response = classOf[FavouriteSearch])
  def addFavouriteSearch(favouriteSearch: FavouriteSearch): Response = {
    favouriteSearch.setInstitution(CurrentInstitution.get())
    favouriteSearch.setDateModified(new Date())
    favouriteSearch.setOwner(CurrentUser.getUserID)
    val newFavouriteSearch = LegacyGuice.favouriteSearchService.save(favouriteSearch)
    Response.status(Status.CREATED).entity(newFavouriteSearch).build()
  }
}
