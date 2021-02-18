package com.tle.web.api.favourite

import com.tle.beans.item.ItemId
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam}

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{DELETE, POST, Path, PathParam, Produces, QueryParam}

/**
  * Basic information of a bookmark. The Java entity class 'com.tle.beans.item.Bookmark'
  * has too many fields that are useless in this API so use this simplified one.
  * @param bookmarkID The unique ID
  * @param keywords tags of this bookmark
  * @param alwaysLatest whether this bookmark uses latest Item version
  * @param itemID ID of the Item this bookmark is related to
  */
case class BookmarkInfo(bookmarkID: Long,
                        keywords: java.util.Collection[String],
                        alwaysLatest: Boolean,
                        itemID: String)

@Path("favourite")
@Produces(Array("application/json"))
@Api("Favourite")
class FavouriteResource {
  private val bookmarkService = LegacyGuice.bookmarkService
  private val itemService     = LegacyGuice.itemService

  @POST
  @ApiOperation(value = "Add one Item to user's favourites", response = classOf[BookmarkInfo])
  def addFavourite(@ApiParam(value = "The unique ID consisting of Item's UUID and version",
                             example = "77279582-ce3f-97ee-84c3-66de5af5a4c5/1") @QueryParam(
                     "itemID") itemID: String,
                   @ApiParam(value = "tags separated by a space, comma or semi colon") @QueryParam(
                     "tags") tags: String,
                   @ApiParam(value = "Whether to use the latest version of this Item") @QueryParam(
                     "latest") latest: Boolean): Response = {
    // ItemNotFoundException will be thrown by itemService so there is no need to
    // validate itemID here.
    val item        = itemService.get(new ItemId(itemID))
    val newBookmark = bookmarkService.add(item, tags, latest)
    Response
      .status(Status.CREATED)
      .entity(
        BookmarkInfo(newBookmark.getId, newBookmark.getKeywords, newBookmark.isAlwaysLatest, itemID)
      )
      .build()
  }

  @DELETE
  @Path("/{uuid}/{version}")
  @ApiOperation("Delete one Item from user's favourites")
  def deleteFavourite(@ApiParam("Item's UUID") @PathParam("uuid") uuid: String,
                      @ApiParam("Item's version") @PathParam("version") version: Int): Response = {
    Option(bookmarkService.getByItem(new ItemId(s"${uuid}/${version}"))) match {
      case Some(bookmark) =>
        bookmarkService.delete(bookmark.getId)
        Response.ok().build()
      case None =>
        ApiErrorResponse
          .resourceNotFound(s"No favourite matching Item UUID: ${uuid} and version: ${version}")
    }
  }
}
