/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.{List, Set}

import com.dytech.devlib.PropBagEx
import com.tle.beans.item.Comment
import com.tle.common.Check
import com.tle.core.i18n.CoreStrings
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.interfaces.beans.{CommentDisplay, CommentSummarySection, ItemSummarySection}
import com.tle.web.api.users.UserDetails
import com.tle.web.viewitem.summary.section.CommentsSection.NameDisplayType
import com.tle.web.viewurl.ItemSectionInfo

import scala.collection.JavaConverters._

object CommentsDisplay {


  def create(ii: ItemSectionInfo, sectionTitle: String, config: String): Option[ItemSummarySection] = {

    val privileges = ii.getPrivileges.asScala

    val canCreate = privileges("COMMENT_CREATE_ITEM") // && !isForPreview(info))
    val canView = privileges("COMMENT_VIEW_ITEM")
    val canDelete = privileges("COMMENT_DELETE_ITEM")

    if (!canCreate && !canView) {
      None
    } else Some {
      val (displayIdentity, allowAnonymous, displayUsername, whichName) = if (!Check.isEmpty(config)) {
        val xml = new PropBagEx(config)

        def boolFlag(str: String): Boolean = {
          val v = xml.getNode(str)
          if (v == null) false else v.toLowerCase() == "true"
        }
        if (boolFlag("DISPLAY_IDENTITY_KEY")) {
          (true, boolFlag("ANONYMOUSLY_COMMENTS_KEY"),
            !boolFlag("SUPPRESS_USERNAME_KEY"), NameDisplayType.valueOf(xml.getNode("DISPLAY_NAME_KEY")))
        } else (false, false, false, NameDisplayType.BOTH)
      }
      else {
        (true, true, true, NameDisplayType.BOTH)
      }
      val comments = if (!canView) Iterable.empty else
        LegacyGuice.itemCommentService.getComments(ii.getItem, null, null, -1).asScala.map {
          c =>
            val commenter = if (!c.isAnonymous)
              Option(LegacyGuice.userService.getInformationForUser(c.getOwner)).map(UserDetails.apply)
            else None

            val userText = commenter.map { user =>
              val displayString = whichName match {
                case NameDisplayType.FIRST =>
                  user.firstName
                case NameDisplayType.LAST =>
                  user.lastName
                case _ =>
                  user.firstName + " " + user.lastName
              }
              if (displayUsername) displayString + " [" + user.username + "]" else displayString
            }.getOrElse(CoreStrings.text("comments.anonymous") )
            CommentDisplay(c.getId, c.getComment, c.getRating, c.getDateCreated, c.isAnonymous, userText, commenter)
        }

      CommentSummarySection(sectionTitle, !displayIdentity, canView, canCreate, canDelete, comments)
    }

  }
}
