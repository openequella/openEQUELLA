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

package com.tle.web.api.item

import com.tle.beans.item.ItemId
import com.tle.legacy.LegacyGuice
import com.tle.web.sections.{SectionId, SectionInfo, SectionNode, SectionUtils}
import com.tle.web.sections.equella.AbstractScalaSection
import com.tle.web.sections.generic.DefaultSectionTree
import com.tle.web.selection.{SelectedResource, SelectedResourceKey}
import com.tle.web.template.RenderNewTemplate
import io.swagger.annotations.Api
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.{POST, Path, PathParam, QueryParam}
import scala.jdk.CollectionConverters._

case class SelectionKey(
    uuid: String,
    version: Int,
    `type`: String,
    attachmentUuid: Option[String],
    folderId: Option[String],
    url: Option[String]
)

case class ResourceSelection(key: SelectionKey, title: String)

@Path("selection/")
@Api("Selection session")
class SelectionApi {

  import SelectionApi._

  @POST
  @Path("{sessid}/return")
  def returnSelections(
      @QueryParam("integid") integid: String,
      @PathParam("sessid") sessid: String,
      @Context request: HttpServletRequest,
      @Context response: HttpServletResponse
  ): Response = {
    LegacyGuice.userSessionService.reenableSessionUse()
    val info        = setupSession(sessid, Option(integid), request, response)
    val sessionData = selectionService.getCurrentSession(info)
    val integ       = integrationService.getIntegrationInterface(info)
    val ok          = integ.select(info, sessionData)
    val rc          = info.getRootRenderContext
    info.setAttribute(RenderNewTemplate.DisableNewUI, true)
    val output = SectionUtils.renderToString(rc, SectionUtils.renderSection(rc, "temp"))
    Response.ok(output).build()
  }

  @POST
  @Path("{sessid}/add")
  def addResource(
      @PathParam("sessid") sessid: String,
      @Context request: HttpServletRequest,
      @Context response: HttpServletResponse,
      resource: ResourceSelection
  ): Response = {
    LegacyGuice.userSessionService.reenableSessionUse()
    val info = setupSession(sessid, None, request, response)
    val res  = new SelectedResource(toSRK(resource.key))
    res.setTitle(resource.title)
    selectionService.addSelectedResource(info, res, false)
    Response.ok().build()
  }

  def toSRK(resKey: SelectionKey): SelectedResourceKey = {
    val key = new SelectedResourceKey(new ItemId(resKey.uuid, resKey.version), null)
    key.setType(resKey.`type`.charAt(0))
    resKey.folderId.foreach(key.setFolderId)
    resKey.attachmentUuid.foreach(key.setAttachmentUuid)
    resKey.url.foreach(key.setUrl)
    key
  }

  @POST
  @Path("{sessid}/remove")
  def removeResource(
      @PathParam("sessid") sessid: String,
      @Context request: HttpServletRequest,
      @Context response: HttpServletResponse,
      resKey: SelectionKey
  ): Response = {
    LegacyGuice.userSessionService.reenableSessionUse()
    val info = setupSession(sessid, None, request, response)
    selectionService.removeSelectedResource(info, toSRK(resKey))
    Response.ok().build()
  }

}

object SelectionApi {
  lazy val integrationService = LegacyGuice.integrationService.get()
  lazy val selectionService   = LegacyGuice.selectionService.get()

  val blankTree =
    new DefaultSectionTree(
      LegacyGuice.treeRegistry,
      new SectionNode(
        "",
        new AbstractScalaSection {
          override type M = Int

          override def newModel: SectionInfo => Int = _ => 1
        }
      )
    )

  def setupSession(
      sessid: String,
      integid: Option[String],
      request: HttpServletRequest,
      response: HttpServletResponse
  ) = {
    val paramMap = Iterable(
      Some("_sl.stateId" -> Array(sessid)),
      integid.map("_int.id" -> Array(_))
    ).flatten.toMap
    val info = LegacyGuice.sectionsController.createInfo(
      blankTree,
      "/",
      request,
      response,
      null,
      paramMap.asJava,
      null
    )

    info.fireBeforeEvents()
    info
  }
}
