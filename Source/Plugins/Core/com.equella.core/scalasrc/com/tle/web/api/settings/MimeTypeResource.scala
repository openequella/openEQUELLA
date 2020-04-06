package com.tle.web.api.settings

import com.dytech.edge.common.Constants
import com.tle.legacy.LegacyGuice
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache
import scala.collection.JavaConverters._

case class MimeTypeDetail(mimeType: String, desc: String)

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class MimeTypeResource {

  @GET
  @Path("mimetype")
  @ApiOperation(
    value = "List available MIME types",
    notes = "This endpoint is used to retrieve MIME types.",
    response = classOf[MimeTypeDetail],
    responseContainer = "List"
  )
  def listMimeTypes: Response = {
    LegacyGuice.mimePrivProvider.checkAuthorised()
    val mimeEntries =
      LegacyGuice.mimeTypeService.searchByMimeType(Constants.BLANK, 0, -1).getResults.asScala
    val mimeTypes = mimeEntries.map(entry => {
      MimeTypeDetail(entry.getType, entry.getDescription)
    })
    Response.ok().entity(mimeTypes).build()
  }
}
