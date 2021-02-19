package com.tle.web.api.favourite

import com.tle.beans.item.ItemId
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
  */
case class FavouriteItem(itemID: String, keywords: Array[String], isAlwaysLatest: Boolean)

@Path("favourite")
@Produces(Array("application/json"))
@Api("Favourite")
class FavouriteResource {
  private val bookmarkService = LegacyGuice.bookmarkService
  private val itemService     = LegacyGuice.itemService

  @POST
  @ApiOperation(value = "Add one Item to user's favourites", response = classOf[FavouriteItem])
  def addFavouriteItem(favouriteItem: FavouriteItem): Response = {
    // ItemNotFoundException will be thrown by itemService if there is no Item matching this
    // item ID so we don't validate item ID here again.
    val item = itemService.get(new ItemId(favouriteItem.itemID))
    // Adding an item to user's favourites is essentially creating a new bookmark.
    val newBookmark =
      bookmarkService.add(item, favouriteItem.keywords.toSet.asJava, favouriteItem.isAlwaysLatest)
    Response
      .status(Status.CREATED)
      .entity(
        FavouriteItem(
          newBookmark.getItem.getItemId.toString,
          newBookmark.getKeywords.asScala.toArray,
          newBookmark.isAlwaysLatest
        )
      )
      .build()
  }

  @DELETE
  @Path("/{uuid}/{version}")
  @ApiOperation("Delete one Item from user's favourites")
  def deleteFavouriteItem(
      @ApiParam("Item's UUID") @PathParam("uuid") uuid: String,
      @ApiParam("Item's version") @PathParam("version") version: Int): Response = {
    Option(bookmarkService.getByItem(new ItemId(s"${uuid}/${version}"))) match {
      case Some(bookmark) =>
        bookmarkService.delete(bookmark.getId)
        Response.status(Status.NO_CONTENT).build()
      case None =>
        ApiErrorResponse
          .resourceNotFound(s"No favourite Item matching UUID: ${uuid} and version: ${version}")
    }
  }
}
