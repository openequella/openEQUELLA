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
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.favourites.bean.{FavouriteSearch => FavouriteSearchBean}
import com.tle.core.favourites.service.{BookmarkService, FavouriteSearchService}
import com.tle.core.guice.Bind
import com.tle.core.item.service.ItemService
import com.tle.exceptions.AccessDeniedException

import java.util.Date
import com.tle.web.api.ApiErrorResponse
import com.tle.web.api.favourite.model.{
  FavouriteSearch,
  FavouriteSearchParam,
  FavouriteSearchPayload
}
import com.tle.web.api.search.model.SearchResult
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{BeanParam, DELETE, GET, POST, Path, PathParam, Produces}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

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
final case class FavouriteItemModel(
    itemID: String,
    keywords: Array[String],
    isAlwaysLatest: Boolean,
    bookmarkID: Long
)

/** Model class for search definitions to be saved to user's favourites.
  * @param name
  *   Name of a search definition.
  * @param url
  *   Path to new Search UI, including all query strings.
  */
final case class FavouriteSearchSaveParam(name: String, url: String)

@Bind
@Singleton
@NoCache
@Path("favourite")
@Produces(Array("application/json"))
@Api("Favourite")
class FavouriteResource @Inject() (
    favouritesSearchService: FavouriteSearchService,
    bookmarkService: BookmarkService,
    itemService: ItemService
) {
  val Logger: Logger = LoggerFactory.getLogger(getClass)

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

  @GET
  @Path("/search")
  @ApiOperation(
    value = "Search favourite searches",
    notes = "Retrieve a list of Favourite searches based on the specified criteria",
    response = classOf[SearchResult[FavouriteSearch]]
  )
  def getFavouriteSearches(@BeanParam params: FavouriteSearchParam): Response = {
    val payload           = FavouriteSearchPayload(params)
    val search            = FavouriteSearchHelper.createSearch(payload)
    val favouriteSearches = favouritesSearchService.search(search, payload.start, payload.length)
    val searchResult      = FavouriteSearchHelper.toSearchResults(payload.query, favouriteSearches)
    Response.ok(searchResult).build()
  }

  @POST
  @Path("/search")
  @ApiOperation(
    value = "Add a search definition to user's search favourites",
    response = classOf[FavouriteSearchSaveParam]
  )
  def addFavouriteSearch(searchInfo: FavouriteSearchSaveParam): Response = {
    val favSearchBean = new FavouriteSearchBean
    favSearchBean.setName(searchInfo.name)
    favSearchBean.setUrl(searchInfo.url)
    favSearchBean.setInstitution(CurrentInstitution.get())
    favSearchBean.setDateModified(new Date())
    favSearchBean.setOwner(CurrentUser.getUserID)
    val newFavouriteSearchBean = favouritesSearchService.save(favSearchBean)

    Response
      .status(Status.CREATED)
      .entity(
        FavouriteSearch(newFavouriteSearchBean)
      )
      .build()
  }

  @DELETE
  @Path("/search/{id}")
  @ApiOperation(
    value = "Delete a search from user's favourites"
  )
  def deleteFavouriteSearch(@ApiParam("Search ID") @PathParam("id") id: Long): Response = {
    Try(favouritesSearchService.deleteIfOwned(id)) match {
      case Success(_) =>
        Response.status(Status.NO_CONTENT).build()
      case Failure(e: NotFoundException) =>
        ApiErrorResponse.resourceNotFound(s"No favourite search matching ID: ${id}")
      case Failure(e: AccessDeniedException) =>
        ApiErrorResponse.forbiddenRequest(
          s"You are not the owner of the favourite search with ID: ${id}"
        )
      case Failure(otherException) =>
        ApiErrorResponse.serverError(s"An unexpected error occurred: ${otherException.getMessage}")
    }
  }
}
