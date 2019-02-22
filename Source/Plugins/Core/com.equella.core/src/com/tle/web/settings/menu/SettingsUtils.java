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

package com.tle.web.settings.menu;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.RenderNewTemplate;

@SuppressWarnings("nls")
public final class SettingsUtils {
  private static final KeyLabel BREADCRUMB_LABEL =
      new KeyLabel(ResourcesService.getResourceHelper(SettingsUtils.class).key("breadcrumb"));

  private static final SimpleBookmark SETTINGS_BOOKMARK = new SimpleBookmark("access/settings.do");
  private static final SimpleBookmark NEW_SETTINGS_BOOKMARK = new SimpleBookmark("page/settings");

  private static final KeyLabel BREADCRUMB_TITLE =
      new KeyLabel(
          ResourcesService.getResourceHelper(SettingsUtils.class).key("settings.breadcrumb.title"));

  public static final Bookmark getBookmark(SectionInfo info) {
    if (RenderNewTemplate.isNewLayout(info)) {
      return NEW_SETTINGS_BOOKMARK;
    }
    return SETTINGS_BOOKMARK;
  }

  public static HtmlLinkState getBreadcrumb(SectionInfo info) {
    HtmlLinkState link = new HtmlLinkState(getBookmark(info));
    link.setLabel(BREADCRUMB_LABEL);
    link.setTitle(BREADCRUMB_TITLE);
    return link;
  }

  private SettingsUtils() {
    throw new Error();
  }
}
