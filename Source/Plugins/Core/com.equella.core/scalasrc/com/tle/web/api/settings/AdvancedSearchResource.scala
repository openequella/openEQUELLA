package com.tle.web.api.settings

import com.tle.beans.entity.BaseEntityLabel
import com.tle.legacy.LegacyGuice
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache

/**
  * API for managing Advanced Searches (internally - and historically - known as Power Searches).
  */
@NoCache
@Path("settings/advancedsearch/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class AdvancedSearchResource {
  private val powerSearchService = LegacyGuice.powerSearchService

  @GET
  @ApiOperation(
    value = "List available Advanced Searches",
    notes =
      "This endpoint is used to retrieve available Advanced Searches and is secured by SEARCH_POWER_SEARCH",
    response = classOf[BaseEntityLabel],
    responseContainer = "List"
  )
  def getAll: Response =
    Response.ok().entity(powerSearchService.listSearchable()).build()
}
