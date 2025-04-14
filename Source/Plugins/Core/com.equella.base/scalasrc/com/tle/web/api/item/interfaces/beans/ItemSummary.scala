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

package com.tle.web.api.item.interfaces.beans

import java.util.Date

import com.tle.beans.item.ItemKey
import com.tle.web.api.users.UserDetails

case class ItemRef(uuid: String, version: Int)

object ItemRef {
  implicit def fromItemKey(key: ItemKey): ItemRef = {
    ItemRef(key.getUuid, key.getVersion)
  }
}

case class ItemSummary(
    title: String,
    hideOwner: Boolean,
    hideCollaborators: Boolean,
    sections: Iterable[ItemSummarySection],
    copyright: Option[CopyrightData]
)

sealed trait ItemSummarySection {
  def `type`: String
  def sectionTitle: String
}

case class BasicDetails(sectionTitle: String, title: String, description: Option[String])
    extends ItemSummarySection {
  override val `type` = "basic"
}

case class HtmlSummarySection(
    sectionTitle: String,
    showTitle: Boolean,
    sourceType: String,
    html: String
) extends ItemSummarySection {
  val `type` = "html"
}

case class AttachmentSummary(
    title: String,
    uuid: String,
    href: String,
    thumbnailHref: String,
    viewers: Map[String, String],
    details: Iterable[MetaDisplay],
    children: Option[Iterable[AttachmentSummary]]
)

case class AttachmentsSummarySection(
    sectionTitle: String,
    attachments: Iterable[AttachmentSummary],
    nodes: Iterable[String]
) extends ItemSummarySection {
  val `type` = "attachments"
}

case class MetaDisplay(title: String, value: String, fullWidth: Boolean, `type`: String)

case class DisplayNodesSummarySection(sectionTitle: String, meta: Iterable[MetaDisplay])
    extends ItemSummarySection {
  val `type` = "displayNodes"
}

case class CommentSummarySection(
    sectionTitle: String,
    canView: Boolean,
    canAdd: Boolean,
    canDelete: Boolean,
    userDisplay: String,
    anonymousOnly: Boolean,
    hideUsername: Boolean,
    allowAnonymous: Boolean
) extends ItemSummarySection {
  val `type` = "comments"
}

sealed trait HoldingSummary {
  def `type`: String
}

case class CopyrightAttachment(
    item: ItemRef,
    href: Option[String],
    title: String,
    uuid: String,
    status: String
)

case class BookSection(
    attachment: CopyrightAttachment,
    range: String,
    pageCount: Int,
    illustration: Boolean
)

case class BookChapter(
    title: String,
    chapterName: String,
    canActivate: Boolean,
    sections: Iterable[BookSection]
)

case class BookSummary(totalPages: Int, chapters: Iterable[BookChapter]) extends HoldingSummary {
  val `type` = "book"
}

case class JournalSection(attachment: CopyrightAttachment)

case class JournalPortion(title: String, sections: Iterable[JournalSection])

case class JournalSummary(
    volume: Option[String],
    issueNumber: Option[String],
    portions: Iterable[JournalPortion]
) extends HoldingSummary {
  val `type` = "journal"
}

case class CopyrightData(holdingItem: ItemRef, holding: HoldingSummary)
