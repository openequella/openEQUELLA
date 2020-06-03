package com.tle.web.api.search

import com.tle.beans.item.ItemIdKey
import com.tle.common.searching.SearchResults
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.api.search.model.{SearchParam, SearchResult}
import com.tle.web.api.search.SearchHelper._
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.{BeanParam, GET, Path, Produces}
import javax.ws.rs.core.Response
import org.jboss.resteasy.annotations.cache.NoCache

import scala.collection.JavaConverters._

@NoCache
@Path("search2")
@Produces(Array("application/json"))
@Api("Search V2")
class SearchResource {

  @GET
  @ApiOperation(
    value = "List items",
    notes = "This endpoint is used to retrieve items.",
    response = classOf[SearchResult],
  )
  def searchItems(@BeanParam params: SearchParam): Response = {
    val searchResults: SearchResults[ItemIdKey] =
      LegacyGuice.freeTextService.searchIds(createSearch(params), params.start, params.length)
    val itemIds: List[ItemIdKey] = searchResults.getResults.asScala.toList
    val serializer               = createSerializer(itemIds)
    val items: List[(ItemIdKey, EquellaItemBean)] = for { itemId <- itemIds } yield {
      val itemBean = new EquellaItemBean
      itemBean.setUuid(itemId.getUuid)
      itemBean.setVersion(itemId.getVersion)
      serializer.writeItemBeanResult(itemBean, itemId.getKey)
      LegacyGuice.itemLinkService.addLinks(itemBean)
      itemId -> itemBean
    }
    val result = SearchResult(searchResults.getOffset,
                              searchResults.getCount,
                              searchResults.getAvailable,
                              items.map(convertToItem))

    Response.ok.entity(result).build()
  }
}
