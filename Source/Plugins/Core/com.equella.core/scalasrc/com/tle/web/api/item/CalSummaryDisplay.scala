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
import com.tle.web.viewurl.ItemSectionInfo
import org.jsoup.Jsoup

import scala.collection.JavaConverters._


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
      portion = portions.get(item.getId).flatMap(_.asScala.headOption)
      html <- Option(calService.citate(holding, portion.orNull))
    } yield html
  }

  def attachmentDisplayName(attachment: Attachment): String = {
    val item = attachment.getItem
    val attributes = item.getItemDefinition.getAttributes
    if (java.lang.Boolean.valueOf(attributes.get(CALConstants.KEY_USE_CITATION_AS_NAME))) {
      citationForItem(item).map(h => Jsoup.parse(h).text()).getOrElse(attachment.getDescription)
    } else attachment.getDescription
  }

  def bookData(info: SectionInfo, holding: CALHolding, activatable: util.Set[Item]): HoldingSummary = {
    val calWebService = LegacyGuice.calWebService
    val calService = LegacyGuice.calService
    val bookPortions = holding.getPortions.asScala.map { p =>
      val item = p.getItem

      val attachMap = calWebService.getAttachmentMap(info, item).asScala
      val vi = LegacyGuice.viewableItemFactory.createNewViewableItem(item.getItemId)
      val title = LangUtils.getString(item.getName, CoreStrings.text("summary.unnamedportion"))
      val sections = p.getSections.asScala.flatMap { s =>
        val range = s.getRange
        val pageCount = PageCounter.countTotalRange(range)
        val atUuid = s.getAttachment
        attachMap.get(atUuid).map { at =>
          val vr = LegacyGuice.attachmentResourceService.getViewableResource(info, vi, at)
          val ls = LegacyGuice.viewItemService.getViewableLink(info, vr, null).getLinkState
          val href = Option(ls.getBookmark).map(b => ItemUrlDisplay.addBaseUri(b.getHref))
          val attachment = CopyrightAttachment(href, attachmentDisplayName(at))
          val status = calWebService.getStatus(info, item, atUuid) match {
            case ActivateRequest.TYPE_ACTIVE => "active"
            case ActivateRequest.TYPE_INACTIVE => "inactive"
            case ActivateRequest.TYPE_PENDING => "pending"
          }
          BookSection(attachment, range, pageCount, status, s.isIllustration)
        }
      }
      BookChapter(item.getItemId, title, p.getChapter, activatable.contains(item), sections)
    }
    BookSummary(PageCounter.countTotalPages(holding.getLength), bookPortions.sortWith(chapterNameCompare))
  }

  def copyrightData(info: SectionInfo, ii: ItemSectionInfo): Option[CopyrightData] = {
    val calService = LegacyGuice.calService
    val activationService = LegacyGuice.activationService
    val item = ii.getItem
    if (!calService.isCopyrightedItem(item)) None else Some {
      val holding = calService.getHoldingForItem(item)
      val activatableItems = activationService.filterActivatableItems(new util.HashSet[Item](
        holding.getPortions.asScala.map(_.getItem).asJava))
      val holdingSummary = holding.getType match {
        case CALConstants.BOOK => bookData(info, holding, activatableItems)
      }
      CopyrightData(holding.getItem.getItemId, holdingSummary)
    }
  }
}
