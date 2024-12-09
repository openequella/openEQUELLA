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

package com.tle.web.sections.equella.render;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.Check;
import com.tle.common.settings.standard.DateFormatSettings;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import java.io.IOException;
import java.util.Date;

public class DateRenderer implements SectionRenderable {
  private final TagRenderer timeAgo;

  @AssistedInject
  protected DateRenderer(
      @Assisted Date date,
      ConfigurationService configurationService,
      UserPreferenceService userPreferenceService) {
    this(date, false, configurationService, userPreferenceService);
  }

  @AssistedInject
  protected DateRenderer(
      @Assisted Date date,
      @Assisted boolean suppressSuffix,
      ConfigurationService configService,
      UserPreferenceService userPrefs) {
    String displayDateFormat =
        suppressSuffix
            ? JQueryTimeAgo.DATE_FORMAT_APPROX
            : getDisplayDateFormat(configService, userPrefs);
    timeAgo = JQueryTimeAgo.timeAgoTag(date, suppressSuffix, displayDateFormat);
  }

  private String getDisplayDateFormat(
      ConfigurationService configService, UserPreferenceService userPrefs) {
    final DateFormatSettings sysSettings = configService.getProperties(new DateFormatSettings());
    String systemDateFormat = sysSettings.getDateFormat();

    String displayDateFormat = null;
    String userSelectedDateFormat = userPrefs.getDateFormat();

    if (Check.isEmpty(userSelectedDateFormat)) {
      if (Check.isEmpty(systemDateFormat)) {
        displayDateFormat = JQueryTimeAgo.DATE_FORMAT_APPROX;
      } else {
        displayDateFormat = systemDateFormat;
      }
    } else {
      displayDateFormat = userSelectedDateFormat;
    }

    return displayDateFormat;
  }

  @Override
  public void preRender(PreRenderContext info) {
    timeAgo.preRender(info);
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    timeAgo.realRender(writer);
  }
}
