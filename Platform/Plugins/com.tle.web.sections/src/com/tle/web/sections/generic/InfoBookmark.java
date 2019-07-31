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

package com.tle.web.sections.generic;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.QueryParams;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import hurl.build.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * A {@link Bookmark} implementation which generates a URI based on a {@link BookmarkEvent}.
 *
 * @author jmaginnis
 */
public class InfoBookmark implements Bookmark {
  protected SectionInfo info;
  private String href;
  private String query;
  private String path;
  private URI baseURI;
  private Map<String, String[]> bookmarkParams;
  private BookmarkEvent bookmarkEvent;

  public InfoBookmark(SectionInfo info) {
    this(info, new BookmarkEvent());
  }

  public InfoBookmark(SectionInfo info, URI baseURI) {
    this(info, new BookmarkEvent());
    this.baseURI = baseURI;
  }

  public InfoBookmark(SectionInfo info, BookmarkEvent bookmarkEvent) {
    this.info = info;
    this.bookmarkEvent = bookmarkEvent;
  }

  @Override
  public String getHref() {
    if (href == null) {
      String path = getPath();
      String query = getQuery();

      int queryLength = query.length();
      if (queryLength == 0) {
        return path;
      }
      href = path + "?" + query; // $NON-NLS-1$
    }
    return href;
  }

  public static URI getBaseHref(SectionInfo info) {
    URI baseHref = info.getAttribute(SectionInfo.KEY_BASE_HREF);
    if (baseHref == null) {
      baseHref = createFromRequest(info.getRequest());
    }
    return baseHref;
  }

  private static URI createFromRequest(HttpServletRequest request) {
    UriBuilder uriBuilder = UriBuilder.create(request.getRequestURI());
    uriBuilder.setScheme(request.getScheme());
    uriBuilder.setHost(request.getServerName());
    uriBuilder.setPort(request.getServerPort());
    return uriBuilder.build();
  }

  public URI getRelativeURI() {
    String path = info.getAttribute(SectionInfo.KEY_PATH);
    try {
      return new URI(null, null, path.substring(1), null);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException();
    }
  }

  public URI getBaseURI() {
    if (baseURI == null) {
      baseURI = getBaseHref(info);
    }
    return baseURI;
  }

  public URI getFullURI() {
    return getBaseURI().resolve(getRelativeURI());
  }

  public String getPath() {
    if (path == null) {
      path = getFullURI().toString();
    }
    return path;
  }

  public String getQuery() {
    if (query == null) {
      query = QueryParams.paramString(getBookmarkParams());
    }
    return query;
  }

  public Map<String, String[]> getBookmarkParams() {
    if (bookmarkParams == null) {
      info.processEvent(bookmarkEvent);
      bookmarkParams = bookmarkEvent.getBookmarkState();
    }
    return bookmarkParams;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public SectionInfo getInfo() {
    return info;
  }
}
