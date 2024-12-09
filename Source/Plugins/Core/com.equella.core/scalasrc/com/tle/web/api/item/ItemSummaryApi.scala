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

import com.thoughtworks.xstream.XStream
import com.tle.beans.entity.itemdef.DisplayNode
import com.tle.beans.item.{Item, ItemId, ItemPack}
import com.tle.common.i18n.LangUtils
import com.tle.common.security.SecurityConstants
import com.tle.common.security.streaming.XStreamSecurityManager
import com.tle.core.item.helper.ItemHelper
import com.tle.exceptions.{AccessDeniedException, PrivilegeRequiredException}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.interfaces.beans._
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.events.StandardRenderContext
import com.tle.web.viewable.NewDefaultViewableItem
import com.tle.web.viewable.servlet.ItemServlet
import com.tle.web.viewitem.section.ParentViewItemSectionUtils
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters._

object ItemSummaryApi {

  val xstream = XStreamSecurityManager.newXStream()
  val Logger  = LoggerFactory.getLogger(getClass)

  def getItemSummary(uuid: String, version: Int): ItemSummary = {
    val vi   = LegacyGuice.viewableItemFactory.createNewViewableItem(new ItemId(uuid, version))
    val item = vi.getItem
    val info = LegacyGuice.sectionsController.createInfo(
      "/viewitem/viewitem.do",
      null,
      null,
      null,
      null,
      Map[AnyRef, AnyRef](ItemServlet.VIEWABLE_ITEM -> vi).asJava
    )
    val ii = ParentViewItemSectionUtils.getItemInfo(info)
    if (!ii.getPrivileges.contains(SecurityConstants.VIEW_ITEM)) {
      throw new PrivilegeRequiredException(SecurityConstants.VIEW_ITEM)
    }
    val converted =
      item.getItemDefinition.getItemSummaryDisplayTemplate.getConfigList.asScala.flatMap(c =>
        convert(info, vi, c.getValue, LangUtils.getString(c.getBundleTitle, ""), c.getConfiguration)
      )

    ItemSummary(
      LangUtils.getString(item.getName, item.getUuid),
      false,
      false,
      converted,
      CalSummaryDisplay.copyrightData(info, ii)
    )
  }

  def convert(
      info: SectionInfo,
      vitem: NewDefaultViewableItem,
      summaryType: String,
      sectionTitle: String,
      config: String
  ): Option[ItemSummarySection] = {
    val item    = vitem.getItem
    val ii      = ParentViewItemSectionUtils.getItemInfo(info)
    val itemXml = ii.getItemxml
    summaryType match {
      case "basicSection" =>
        Some(
          BasicDetails(
            sectionTitle,
            LangUtils.getString(item.getName, item.getUuid),
            Option(LangUtils.getString(item.getDescription, null: String))
          )
        )
      case "displayNodes" =>
        val dn = xstream.fromXML(config).asInstanceOf[java.util.List[DisplayNode]].asScala
        val fullItemXml = LegacyGuice.itemHelper.convertToXml(
          new ItemPack[Item](item, itemXml, ""),
          new ItemHelper.ItemHelperSettings(true)
        )
        Some(DisplayNodesSummarySection(sectionTitle, dn.flatMap(DisplayNodes.create(fullItemXml))))
      case "attachmentsSection" =>
        AttachmentsDisplay.create(info, vitem, itemXml, sectionTitle, config)
      case "xsltSection" =>
        val html = LegacyGuice.itemXsltService.renderSimpleXsltResult(
          new StandardRenderContext(info),
          ii,
          config
        )
        Some(HtmlSummarySection(sectionTitle, false, "xslt", html))
      case "freemarkerSection" =>
        Some(FreemarkerDisplay.create(info, ii, sectionTitle, config))
      case "commentsSection" =>
        CommentsDisplay.create(ii, sectionTitle, config)
      case "citationSummarySection" =>
        CalSummaryDisplay
          .citationForItem(item)
          .map(h => HtmlSummarySection(sectionTitle, true, "citation", h))
      case _ =>
        Logger.error(s"No summary for $summaryType:$config")
        None
    }
  }
}
