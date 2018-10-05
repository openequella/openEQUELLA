package com.tle.web.api.searches

import java.util.UUID

import cats.data.OptionT
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.settings.SettingsDB
import com.tle.web.api.ApiHelper
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs._
import javax.ws.rs.core.Response.Status

@Api("Search page configuration")
@Path("searches")
@Produces(Array("application/json"))
class SearchConfigApi {

  @GET
  @Path("config/{uuid}")
  @ApiOperation(value = "Get search config", response = classOf[SearchConfig])
  def getConfig(@PathParam("uuid") configId: UUID): Response = ApiHelper.runAndBuild {
    ApiHelper.entityOrNotFoundDB(SearchConfigDB.readConfig(configId))
  }

  @PUT
  @Path("config/{uuid}")
  @ApiOperation(value = "Edit search config")
  def editConfig(@PathParam("uuid") configId: UUID, config: SearchConfig): Response = {
    ApiHelper.runAndBuild {
      SearchConfigDB.writeConfig(configId, config).map(_ => Response.ok())
    }
  }

  @POST
  @Path("config")
  @ApiOperation(value = "Create new search config")
  def newConfig(config: SearchConfig): Response = {
    val newID = UUID.randomUUID()
    ApiHelper.runAndBuild {
      SearchConfigDB.writeConfig(newID, config).map(_ => Response.ok())
    }
  }

  @GET
  @Path("page/{pagename}/resolve")
  @ApiOperation("Resolve config for a page")
  def resolveConfig(@PathParam("pagename") pagename: String): Response = ApiHelper.runAndBuild {
    for {
      config <- SearchConfigDB.readPageConfig(pagename).flatMap { sc =>
        SearchConfigDB.readConfig(sc.configId)
      }.value
    } yield {
      (config, SearchDefaults.defaultMap.get(pagename)) match {
        case (Some(c), Some(d)) => Response.ok(SearchDefaults.mergeDefaults(d, c))
        case (a, b) => ApiHelper.entityOrNotFound(a.orElse(b))
      }
    }
  }
}
