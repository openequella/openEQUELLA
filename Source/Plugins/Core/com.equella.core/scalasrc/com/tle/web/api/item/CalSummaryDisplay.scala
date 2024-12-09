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

import java.util
import java.util.Collections

import com.dytech.common.text.NumberStringComparator
import com.tle.beans.activation.ActivateRequest
import com.tle.beans.cal.CALHolding
import com.tle.beans.item.Item
import com.tle.beans.item.attachments.Attachment
import com.tle.cal.CALConstants
import com.tle.common.i18n.LangUtils
import com.tle.core.activation.validation.PageCounter
import com.tle.core.i18n.CoreStrings
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.interfaces.beans._
import com.tle.web.sections.SectionInfo
import com.tle.web.viewable.NewDefaultViewableItem
import com.tle.web.viewurl.ItemSectionInfo
import org.jsoup.Jsoup
import scala.jdk.CollectionConverters._

object CalSummaryDisplay {

  val chapterNameCompare = {
    val comp = new NumberStringComparator[BookChapter] {
      override def convertToString(t: BookChapter): String = t.chapterName
    }
    (c1: BookChapter, c2: BookChapter) => comp.compare(c1, c2) < 0
  }

  def citationForItem(item: Item): Option[String] = {
    val calService = LegacyGuice.calService
    for {
      holding <- Option(calService.getHoldingForItem(item))
      portions = calService.getPortionsForItems(Collections.singletonList(item)).asScala
      portion  = portions.get(item.getId).flatMap(_.asScala.headOption)
      html <- Option(calService.citate(holding, portion.orNull))
    } yield html
  }

  def attachmentDisplayName(attachment: Attachment): String = {
    val item       = attachment.getItem
    val attributes = item.getItemDefinition.getAttributes
    if (java.lang.Boolean.valueOf(attributes.get(CALConstants.KEY_USE_CITATION_AS_NAME))) {
      citationForItem(item).map(h => Jsoup.parse(h).text()).getOrElse(attachment.getDescription)
    } else attachment.getDescription
  }

  def copyrightAttachment(
      info: SectionInfo,
      item: Item,
      vi: NewDefaultViewableItem,
      at: Attachment
  ) = {
    val vr     = LegacyGuice.attachmentResourceService.getViewableResource(info, vi, at)
    val ls     = LegacyGuice.viewItemService.getViewableLink(info, vr, null).getLinkState
    val href   = Option(ls.getBookmark).map(b => ItemUrlDisplay.addBaseUri(b.getHref))
    val atUuid = at.getUuid
    val status = LegacyGuice.calWebService.getStatus(info, item, atUuid) match {
      case ActivateRequest.TYPE_ACTIVE   => "active"
      case ActivateRequest.TYPE_INACTIVE => "inactive"
      case ActivateRequest.TYPE_PENDING  => "pending"
    }
    CopyrightAttachment(item.getItemId, href, attachmentDisplayName(at), atUuid, status)
  }

  def bookData(
      info: SectionInfo,
      holding: CALHolding,
      activatable: util.Set[Item]
  ): HoldingSummary = {
    val bookPortions = holding.getPortions.asScala.map { p =>
      val item = p.getItem

      val attachMap = LegacyGuice.calWebService.getAttachmentMap(info, item).asScala
      val vi        = LegacyGuice.viewableItemFactory.createNewViewableItem(item.getItemId)
      val title     = LangUtils.getString(item.getName, CoreStrings.text("summary.unnamedportion"))
      val sections = p.getSections.asScala.flatMap { s =>
        val range     = s.getRange
        val pageCount = PageCounter.countTotalRange(range)
        val atUuid    = s.getAttachment
        attachMap.get(atUuid).map { at =>
          val attachment = copyrightAttachment(info, item, vi, at)
          BookSection(attachment, range, pageCount, s.isIllustration)
        }
      }
      BookChapter(title, p.getChapter, activatable.contains(item), sections)
    }
    BookSummary(
      PageCounter.countTotalPages(holding.getLength),
      bookPortions.sortWith(chapterNameCompare)
    )
  }

  def journalData(info: SectionInfo, holding: CALHolding): HoldingSummary = {
    val calWebService = LegacyGuice.calWebService
    val journalPortions = holding.getPortions.asScala.flatMap { p =>
      val item      = p.getItem
      val vi        = LegacyGuice.viewableItemFactory.createNewViewableItem(item.getItemId)
      val attachMap = calWebService.getAttachmentMap(info, item).asScala
      val sections = p.getSections.asScala.flatMap { s =>
        val atUuid = s.getAttachment
        attachMap.get(atUuid).map { at =>
          p.getTitle -> JournalSection(copyrightAttachment(info, item, vi, at))
        }
      }
      sections.groupBy(_._1).map { case (title, s) =>
        JournalPortion(title, s.map(_._2))
      }
    }

    JournalSummary(Option(holding.getVolume), Option(holding.getIssueNumber), journalPortions)
  }

  def copyrightData(info: SectionInfo, ii: ItemSectionInfo): Option[CopyrightData] = {
    val calService        = LegacyGuice.calService
    val activationService = LegacyGuice.activationService
    val item              = ii.getItem
    if (!calService.isCopyrightedItem(item)) None
    else
      Option(calService.getHoldingForItem(item)).map { holding =>
        val activatableItems = activationService.filterActivatableItems(
          new util.HashSet[Item](holding.getPortions.asScala.map(_.getItem).asJava)
        )
        val holdingSummary = holding.getType match {
          case CALConstants.BOOK    => bookData(info, holding, activatableItems)
          case CALConstants.JOURNAL => journalData(info, holding)
        }
        CopyrightData(holding.getItem.getItemId, holdingSummary)
      }
  }
}
