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

package com.tle.web.favourites.portal;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.RecentSelectionsSegment;

@Bind
public class FavouritesSegment extends FavouritesPortletRenderer
    implements RecentSelectionsSegment {
  @PlugKey("favourites.portal.name")
  private static Label TITLE;

  @Override
  public String getTitle(SectionInfo info, SelectionSession session) {
    return TITLE.getText(); // $NON-NLS-1$
  }
}
