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

package com.tle.web.sections.equella.layout;

import com.tle.web.sections.SectionInfo;

public class ContentLayout {
  private static final String CONTENT_LAYOUT_KEY = "$CONTENT_LAYOUT$"; // $NON-NLS-1$

  public static final ContentLayout ONE_COLUMN =
      new ContentLayout("layouts/content/onecolumn.ftl"); // $NON-NLS-1$
  public static final ContentLayout TWO_COLUMN =
      new ContentLayout("layouts/content/twocolumn.ftl"); // $NON-NLS-1$
  public static final ContentLayout COMBINED_COLUMN =
      new ContentLayout("layouts/content/combinedcolumn.ftl"); // $NON-NLS-1$

  private final String ftl;

  public ContentLayout(String ftl) {
    this.ftl = ftl;
  }

  public String getFtl() {
    return ftl;
  }

  public static void setLayout(SectionInfo info, ContentLayout layout) {
    if (layout == null) {
      throw new IllegalArgumentException("layout cannot be null"); // $NON-NLS-1$
    }
    info.setAttribute(CONTENT_LAYOUT_KEY, layout);
  }

  public static ContentLayout getLayout(SectionInfo info) {
    return (ContentLayout) info.getAttribute(CONTENT_LAYOUT_KEY);
  }
}
