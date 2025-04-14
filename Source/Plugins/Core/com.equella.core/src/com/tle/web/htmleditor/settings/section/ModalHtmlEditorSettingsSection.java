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

package com.tle.web.htmleditor.settings.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@NonNullByDefault
public interface ModalHtmlEditorSettingsSection extends Section {
  void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);

  void startSession(SectionInfo info);

  /**
   * @param info
   * @return Return null if it doesn't apply (e.g. no privs)
   */
  @Nullable
  SettingInfo getSettingInfo(SectionInfo info);

  public class SettingInfo {
    private final String id;
    private final Label linkTitle;
    private final Label blurb;

    public SettingInfo(String id, Label linkTitle, Label blurb) {
      this.id = id;
      this.linkTitle = linkTitle;
      this.blurb = blurb;
    }

    public String getId() {
      return id;
    }

    public Label getLinkTitle() {
      return linkTitle;
    }

    public Label getBlurb() {
      return blurb;
    }
  }
}
