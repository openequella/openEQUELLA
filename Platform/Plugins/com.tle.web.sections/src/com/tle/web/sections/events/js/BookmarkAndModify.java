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

package com.tle.web.sections.events.js;

import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.generic.InfoBookmark;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookmarkAndModify implements Bookmark {
  private final Collection<BookmarkModifier> modifiers;
  private final InfoBookmark infoBookmark;
  private String query;
  private String path;

  public BookmarkAndModify(SectionInfo info, BookmarkModifier... modifiers) {
    this(new InfoBookmark(info), modifiers);
  }

  public BookmarkAndModify(InfoBookmark infoBookmark, BookmarkModifier... modifiers) {
    this.infoBookmark = infoBookmark;
    this.modifiers = Arrays.asList(modifiers);
  }

  public BookmarkAndModify(InfoBookmark infoBookmark, Collection<BookmarkModifier> modifiers) {
    this.infoBookmark = infoBookmark;
    this.modifiers = modifiers;
  }

  @Override
  public String getHref() {
    String query = getQuery();
    if (path == null) {
      path = infoBookmark.getPath();
    }
    if (Check.isEmpty(query)) {
      return path;
    }
    return path + '?' + query;
  }

  public String getQuery() {
    if (query == null) {
      SectionInfo info = infoBookmark.getInfo();
      Map<String, String[]> params =
          new LinkedHashMap<String, String[]>(infoBookmark.getBookmarkParams());
      if (modifiers != null) {
        for (BookmarkModifier modifier : modifiers) {
          modifier.addToBookmark(info, params);
        }
      }
      String[] pathar = params.remove(SectionInfo.KEY_PATH);
      if (pathar != null) {
        path = pathar[0];
      }
      query = SectionUtils.getParameterString(SectionUtils.getParameterNameValues(params, false));
    }
    return query;
  }
}
