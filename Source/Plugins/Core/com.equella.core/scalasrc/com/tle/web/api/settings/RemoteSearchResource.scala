package com.tle.web.api.settings

import com.tle.legacy.LegacyGuice
import com.tle.web.api.entity.BaseEntitySummary
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@Path("settings/remotesearch/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class RemoteSearchResource {
  private val federatedSearchService = LegacyGuice.federatedSearchService

  @GET
  @ApiOperation(
    value = "List available Remote (Federated) Searches",
    notes =
      "This endpoint is used to retrieve available Remote Searches and is secured by SEARCH_FEDERATED_SEARCH",
    response = classOf[BaseEntitySummary],
    responseContainer = "List"
  )
  def getAll: Response =
    Response
      .ok()
      .entity(
        federatedSearchService.enumerateSearchable().asScala.map(be => BaseEntitySummary(be))
      )
      .build()
}
