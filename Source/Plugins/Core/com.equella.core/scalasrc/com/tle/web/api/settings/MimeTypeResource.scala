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

package com.tle.web.api.settings

import com.dytech.edge.common.Constants
import com.tle.core.mimetypes.MimeTypeConstants
import com.tle.legacy.LegacyGuice
import com.tle.web.viewurl.ResourceViewerConfig
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, PathParam, Produces}
import org.apache.http.HttpStatus
import org.jboss.resteasy.annotations.cache.NoCache

import scala.jdk.CollectionConverters._
import scala.collection.mutable

/** Summary information for a MIME type in oEQ.
  *
  * @param mimeType
  *   the MIME type specification - e.g. application/pdf
  * @param desc
  *   a human readable description (optional)
  */
case class MimeTypeDetail(mimeType: String, desc: String)

/** Details around the viewer configuration for a MIME type.
  *
  * @param viewerId
  *   One of the standard internal viewer identification strings - e.g. file, fancybox, etc.
  * @param config
  *   The optional configuration for the viewer, only present if non-default has been configured
  */
case class MimeTypeViewerDetail(viewerId: String, config: ResourceViewerConfig)

/** Full MIME type viewer configuration, including those details from MimeTypeViewerDetail as well
  * as the viewerID of the viewer to use by default.
  *
  * @param defaultViewer
  *   the 'viewerId' of one of the 'viewers' which should be used by default
  * @param viewers
  *   the full list of viewers enabled, plus their configuration - if any
  */
case class MimeTypeViewerConfiguration(defaultViewer: String, viewers: Seq[MimeTypeViewerDetail])

@NoCache
@Path("mimetype/")
@Produces(value = Array("application/json"))
@Api(value = "Mimetype")
class MimeTypeResource {

  @GET
  @ApiOperation(
    value = "List available MIME types",
    notes = "This endpoint is used to retrieve MIME types.",
    response = classOf[MimeTypeDetail],
    responseContainer = "List"
  )
  def listMimeTypes: Response = {
    val mimeEntries =
      LegacyGuice.mimeTypeService.searchByMimeType(Constants.BLANK, 0, -1).getResults.asScala
    val mimeTypes = mimeEntries.map(entry =>
      MimeTypeDetail(
        entry.getType,
        entry.getDescription
      )
    )
    Response.ok().entity(mimeTypes).build()
  }

  @GET
  @Path("viewerconfig/{type}/{sub}")
  @ApiOperation(
    value = "Retrieve the viewer configuration for a MIME type",
    response = classOf[MimeTypeViewerConfiguration]
  )
  def getMimeTypeViewer(
      @PathParam("type") mimeType: String,
      @PathParam("sub") mimeTypeSub: String
  ): Response = {
    val mts = LegacyGuice.mimeTypeService

    Option(mts.getEntryForMimeType(s"$mimeType/$mimeTypeSub")) match {
      case Some(entry) =>
        val enabledViewers = mutable.Buffer[String]() ++= mts
          .getListFromAttribute(entry, MimeTypeConstants.KEY_ENABLED_VIEWERS, classOf[String])
          .asScala
        if (entry.getAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER) == null)
          enabledViewers += MimeTypeConstants.VAL_DEFAULT_VIEWERID

        val config = MimeTypeViewerConfiguration(
          defaultViewer = Option(entry.getAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID)) match {
            case Some(value) => value
            case None        => MimeTypeConstants.VAL_DEFAULT_VIEWERID
          },
          viewers = enabledViewers.toSeq
            .map(v =>
              MimeTypeViewerDetail(
                viewerId = v,
                config = mts.getBeanFromAttribute(
                  entry,
                  MimeTypeConstants.KEY_VIEWER_CONFIG_PREFIX + v,
                  classOf[ResourceViewerConfig]
                )
              )
            )
        )
        Response.ok().entity(config).build()
      case None => Response.status(HttpStatus.SC_NOT_FOUND).build()
    }
  }
}
