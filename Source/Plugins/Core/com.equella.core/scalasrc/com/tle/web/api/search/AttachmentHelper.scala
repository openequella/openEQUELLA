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

import com.tle.beans.item.ItemIdKey
import com.tle.beans.item.attachments.Attachment
import com.tle.core.item.service.AttachmentService._
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.api.search.SearchHelper.getLinksFromBean
import com.tle.web.api.search.model.SearchResultAttachment
import com.tle.web.controls.resource.ResourceAttachmentBean
import com.tle.web.controls.youtube.YoutubeAttachmentBean

import java.util.Optional
import scala.jdk.CollectionConverters._
import scala.jdk.javaapi.OptionConverters.toScala

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
          LegacyGuice.itemService.getRealVersion(bean.getItemVersion, bean.getItemUuid)
        bean.setItemVersion(latestVersion)
      case _ =>
    }
    att
  }

  /**
    * Produces a function to support checking whether a user has permission to view an attachment.
    * Currently only checking around the concept of 'restricted attachments', but could be expanded
    * if other checks required.
    *
    * @param hasRestrictedAttachmentPrivileges typically a `lazy val` from hasAcl(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)
    * @param att the attachment to check if the user is allows to view
    * @return True if viewable, otherwise false
    */
  def isViewable(hasRestrictedAttachmentPrivileges: => Boolean)(att: AttachmentBean) =
    !att.isRestricted || hasRestrictedAttachmentPrivileges

  /**
    * Given an `AttachmentBean` and details of the owning Item, builds a `SearchResultAttachment` for
    * use in search results.
    *
    * @param itemKey Key for the owning item of `att`.
    * @param att The attachment to convert.
    * @return The resultant `SearchResultAttachment`.
    */
  def toSearchResultAttachment(itemKey: ItemIdKey, att: AttachmentBean): SearchResultAttachment = {
    val attachment = recurseBrokenAttachmentCheck(itemKey, att.getUuid)
    def ifNotBroken[T](f: Attachment => Option[T], default: Option[T] = None) =
      if (attachment.isDefined) f(attachment.get) else default

    SearchResultAttachment(
      attachmentType = att.getRawAttachmentType,
      id = att.getUuid,
      preview = att.isPreview,
      hasGeneratedThumb = thumbExists(itemKey, att),
      links = buildAttachmentLinks(att),
      filePath = getFilePathForAttachment(att),
      brokenAttachment = attachment.isEmpty,
      // Use the `description` from the `Attachment` behind the `AttachmentBean` as this provides
      // the value more commonly seen in the LegacyUI. And specifically uses any tweaks done for
      // Custom Attachments - such as with Kaltura where the Kaltura Media `title` is pushed into
      // the `description` rather than using the optional (and multi-line) Kaltura Media `description`.
      // But if not available due to broken attachments, well something is better than
      // nothing so use the on in `AttachmentBean`.
      description =
        ifNotBroken((a: Attachment) => Option(a.getDescription), Option(att.getDescription)),
      mimeType = ifNotBroken(_ => getMimetypeForAttachment(att))
    )
  }
}
