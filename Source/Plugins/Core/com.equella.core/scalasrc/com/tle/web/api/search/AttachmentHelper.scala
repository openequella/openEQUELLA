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

package com.tle.web.api.search

import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.api.search.SearchHelper.getLinksFromBean
import com.tle.web.api.search.service.AttachmentResourceService.getLatestVersionForCustomAttachment
import com.tle.web.controls.resource.ResourceAttachmentBean
import com.tle.web.controls.youtube.YoutubeAttachmentBean
import java.util.Optional
import scala.collection.JavaConverters._

/**
  * Object to provide helper functions for building a SearchResultAttachment.
  */
object AttachmentHelper {

  /**
    * Build the links maps for an attachment, which can also include external links (or IDs) for
    * custom attachments.
    *
    * @param attachment to build the links map for
    * @return a map containing the standard links (view, thumbnail), plus optionally external links
    *         if suitable for the attachment type.
    */
  def buildAttachmentLinks(attachment: AttachmentBean): java.util.Map[String, String] = {
    val addExternalId = (links: Map[String, String], externalId: Optional[String]) =>
      if (externalId.isPresent) links ++ Map("externalId" -> externalId.get) else links

    val links = getLinksFromBean(attachment).asScala.toMap
    (attachment match {
      case youtube: YoutubeAttachmentBean => addExternalId(links, youtube.getExternalId)
      case kaltura if attachment.getRawAttachmentType == "custom/kaltura" =>
        addExternalId(links, kaltura.getExternalId)
      case _ => links
    }).asJava
  }

  /**
    * When an AttachmentBean is converted to SearchResultAttachment, it may require some extra sanitising
    * to complete the conversion. The sanitising work includes tasks listed below.
    *
    * 1. Help ResourceAttachmentBean check the version of its linked resource.
    *
    * @param att An AttachmentBean to be sanitised.
    */
  def sanitiseAttachmentBean(att: AttachmentBean): AttachmentBean = {
    att match {
      case bean: ResourceAttachmentBean =>
        val latestVersion =
          getLatestVersionForCustomAttachment(bean.getItemVersion, bean.getItemUuid)
        bean.setItemVersion(latestVersion)
      case _ =>
    }
    att
  }
}
