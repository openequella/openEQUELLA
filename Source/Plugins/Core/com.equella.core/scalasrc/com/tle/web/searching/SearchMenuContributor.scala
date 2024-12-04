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

package com.tle.web.searching

import java.util
import java.util.Collections
import com.dytech.edge.web.WebConstants
import com.tle.core.i18n.CoreStrings
import com.tle.core.security.ACLChecks.hasAcl
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.sections.standard.model.{HtmlLinkState, SimpleBookmark}
import com.tle.web.settings.UISettings
import com.tle.web.template.RenderNewTemplate
import com.tle.web.template.section.MenuContributor
import com.tle.web.template.section.MenuContributor.MenuContribution

object SearchMenuContributor extends MenuContributor {
  private val LABEL_KEY = new KeyLabel(CoreStrings.key("searching.menu"))
  private val ICON_PATH = CoreStrings.lookup.url("images/menu-icon-search.png")

  override def getMenuContributions(info: SectionInfo): util.List[MenuContribution] = {
    if (!hasAcl(WebConstants.SEARCH_PAGE_PRIVILEGE)) {
      Collections.emptyList()
    } else {
      val uis          = UISettings.getUISettings
      val useNewSearch = uis.newUI.newSearch && RenderNewTemplate.isNewLayout(info)
      val hls = new HtmlLinkState(
        new SimpleBookmark(if (useNewSearch) "page/search" else "searching.do")
      )
      hls.setLabel(SearchMenuContributor.LABEL_KEY)
      val mc = new MenuContributor.MenuContribution(
        hls,
        SearchMenuContributor.ICON_PATH,
        1,
        20,
        "search",
        if (useNewSearch) "/page/search" else null
      )
      Collections.singletonList(mc)
    }
  }

  override def clearCachedData(): Unit = {
    // Nothing is cached
  }
}
