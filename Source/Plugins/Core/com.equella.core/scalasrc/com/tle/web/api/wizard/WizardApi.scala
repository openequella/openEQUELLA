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

package com.tle.web.api.wizard

import com.tle.common.filesystem.FileEntry
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean
import com.tle.web.api.item.{ItemEditResponses, ItemEdits}
import com.tle.web.wizard.impl.WizardServiceImpl.WizardSessionState
import com.tle.web.wizard.{WizardState, WizardStateInterface}
import io.swagger.annotations.Api
import org.jboss.resteasy.annotations.cache.NoCache

import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import javax.ws.rs.core.Context
import scala.jdk.CollectionConverters._

case class FileInfo(size: Long, files: Option[Map[String, FileInfo]])
case class ItemState(
    xml: String,
    attachments: Iterable[EquellaAttachmentBean],
    files: Map[String, FileInfo],
    stateVersion: Int
)

@Api("Wizard editing")
@Path("wizard/{wizid}")
class WizardApi {

  def withWizardState[A](wizid: String, req: HttpServletRequest, edit: Boolean)(
      f: WizardStateInterface => A
  ): A = {
    val sessionService = LegacyGuice.userSessionService
    sessionService.reenableSessionUse()
    Option(sessionService.getAttribute(wizid).asInstanceOf[WizardSessionState])
      .map { wss =>
        val wsi = wss.getWizardState
        sessionService.getSessionLock.synchronized {
          if (edit) {
            wsi match {
              case wizstate: WizardState => wizstate.incrementVersion()
              case _                     => ()
            }
          }
          val res = f(wsi)
          if (edit) sessionService.setAttribute(wizid, new WizardSessionState(wsi))
          res
        }
      }
      .getOrElse(throw new WebApplicationException(404))
  }

  @GET
  @NoCache
  @Path("state")
  def getState(@PathParam("wizid") wizid: String, @Context req: HttpServletRequest): ItemState = {
    withWizardState(wizid, req, false) { wsi =>
      val attachments =
        wsi.getItem.getAttachments.asScala.map(a =>
          ItemEdits.attachmentSerializers.serializeAttachment(a)
        )
      val itemPack = wsi.getItemPack

      def writeFile(fileInfo: FileEntry): (String, FileInfo) = {
        val childFiles = if (fileInfo.isFolder) {
          Some(writeFiles(fileInfo.getFiles.asScala))
        } else {
          None
        }
        (fileInfo.getName, FileInfo(fileInfo.getLength, childFiles))
      }

      def writeFiles(entries: Iterable[FileEntry]): Map[String, FileInfo] = {
        entries.map(writeFile).toMap
      }
      val files = LegacyGuice.fileSystemService.enumerateTree(wsi.getFileHandle, "", null)
      ItemState(
        itemPack.getXml.toString,
        attachments,
        writeFiles(files.getFiles.asScala),
        wsi.getStateVersion
      )
    }
  }

  @PUT
  @Path("edit")
  def editAttachments(
      @PathParam("wizid") wizid: String,
      itemEdit: ItemEdits,
      @Context req: HttpServletRequest
  ): ItemEditResponses = {
    withWizardState(wizid, req, true) { wsi =>
      val editor   = new WizardItemEditor(wsi)
      val response = ItemEdits.performEdits(itemEdit, editor)
      editor.finishedEditing(false)
      response
    }
  }
}
