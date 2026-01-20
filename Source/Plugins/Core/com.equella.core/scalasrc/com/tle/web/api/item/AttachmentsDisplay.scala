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

import java.util.Collections

import com.dytech.devlib.PropBagEx
import com.tle.beans.item.attachments.Attachment
import com.tle.common.collection.AttachmentConfigConstants._
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.interfaces.beans.{
  AttachmentSummary,
  AttachmentsSummarySection,
  MetaDisplay
}
import com.tle.web.sections.events.{RenderContext, StandardRenderContext}
import com.tle.web.sections.render.TextLabel
import com.tle.web.sections.{SectionInfo, SectionUtils}
import com.tle.web.viewable.NewDefaultViewableItem
import scala.jdk.CollectionConverters._

object AttachmentsDisplay {

  def createAttachment(
      rc: RenderContext,
      vi: NewDefaultViewableItem,
      attachment: Attachment
  ): AttachmentSummary = {

    val vr   = LegacyGuice.attachmentResourceService.getViewableResource(rc, vi, attachment)
    val uuid = attachment.getUuid
    val deets = (vr.getCommonAttachmentDetails.asScala ++
      Option(vr.getExtraAttachmentDetails).getOrElse(Collections.emptyList()).asScala).map { ad =>
      val descHtml = SectionUtils.renderToString(rc, ad.getDescription)
      MetaDisplay(ad.getName.getText, descHtml, true, "html")
    }
    val attachmentUrl =
      LegacyGuice.institutionService.institutionalise(vr.createDefaultViewerUrl().getHref)
    AttachmentSummary(
      vr.getDescription,
      uuid,
      attachmentUrl,
      vr.createStandardThumbnailRenderer(new TextLabel("HELLO")).getSource,
      Map.empty,
      deets,
      None
    )
  }

  def create(
      info: SectionInfo,
      vi: NewDefaultViewableItem,
      itemXml: PropBagEx,
      stitle: String,
      config: String
  ): Option[AttachmentsSummarySection] = {
    val rc                 = new StandardRenderContext(info)
    val xml                = Option(config).map(new PropBagEx(_)).getOrElse(new PropBagEx())
    val showFull           = xml.isNodeTrue(SHOW_FULLSCREEN_LINK_KEY)
    val showFullNewWindow  = xml.isNodeTrue(SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY)
    val showStructuredView = !(xml.getNode(DISPLAY_MODE_KEY) == DISPLAY_MODE_THUMBNAIL)
    val metadataTargets    = xml.getNodeList(METADATA_TARGET).asScala

    val allUuids =
      metadataTargets.flatMap(n => itemXml.iterateAllValues(n).iterator().asScala.toBuffer).toSet
    val filterAttach = if (metadataTargets.nonEmpty) allUuids else (u: String) => true
    val attachDisplays = vi.getItem.getAttachmentsUnmodifiable
      .iterator()
      .asScala
      .filter(a => filterAttach(a.getUuid))
      .map(a => createAttachment(rc, vi, a))
      .toBuffer

    if (attachDisplays.nonEmpty)
      Some(AttachmentsSummarySection(stitle, attachDisplays, metadataTargets))
    else None
  }
}
