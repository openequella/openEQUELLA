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

import com.dytech.devlib.PropBagEx
import com.tle.common.Check
import com.tle.web.api.item.interfaces.beans.{CommentSummarySection, ItemSummarySection}
import com.tle.web.viewitem.summary.section.CommentsSection.NameDisplayType
import com.tle.web.viewurl.ItemSectionInfo
import scala.jdk.CollectionConverters._

object CommentsDisplay {

  def create(
      ii: ItemSectionInfo,
      sectionTitle: String,
      config: String
  ): Option[ItemSummarySection] = {

    val privileges = ii.getPrivileges.asScala

    val canCreate = privileges("COMMENT_CREATE_ITEM") // && !isForPreview(info))
    val canView   = privileges("COMMENT_VIEW_ITEM")
    val canDelete = privileges("COMMENT_DELETE_ITEM")

    if (!canCreate && !canView) {
      None
    } else
      Some {
        val (displayIdentity, allowAnonymous, hideUsername, whichName) =
          if (!Check.isEmpty(config)) {
            val xml = new PropBagEx(config)

            def boolFlag(str: String): Boolean = {
              val v = xml.getNode(str)
              if (v == null) false else v.toLowerCase() == "true"
            }
            if (boolFlag("DISPLAY_IDENTITY_KEY")) {
              (
                true,
                boolFlag("ANONYMOUSLY_COMMENTS_KEY"),
                boolFlag("SUPPRESS_USERNAME_KEY"),
                NameDisplayType.valueOf(xml.getNode("DISPLAY_NAME_KEY"))
              )
            } else (false, true, true, NameDisplayType.BOTH)
          } else {
            (true, true, false, NameDisplayType.BOTH)
          }
        CommentSummarySection(
          sectionTitle,
          canView,
          canCreate,
          canDelete,
          whichName.toString.toLowerCase(),
          !displayIdentity,
          hideUsername,
          allowAnonymous
        )
      }

  }
}
