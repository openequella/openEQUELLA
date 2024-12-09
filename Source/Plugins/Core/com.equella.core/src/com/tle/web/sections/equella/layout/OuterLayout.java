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

public class OuterLayout {
  private static final String OUTER_LAYOUT_KEY = "$OUTER_LAYOUT$"; // $NON-NLS-1$

  public static final OuterLayout STANDARD =
      new OuterLayout("layouts/outer/standard.ftl"); // $NON-NLS-1$

  private final String ftl;

  public OuterLayout(String ftl) {
    this.ftl = ftl;
  }

  public String getFtl() {
    return ftl;
  }

  public static void setLayout(SectionInfo info, OuterLayout layout) {
    if (layout == null) {
      throw new IllegalArgumentException("layout cannot be null"); // $NON-NLS-1$
    }
    info.setAttribute(OUTER_LAYOUT_KEY, layout);
  }

  /**
   * The current outer layout (frameset, standard).
   *
   * @param info
   * @return Will never return null, default layout is OuterLayout.STANDARD
   */
  public static OuterLayout getLayout(SectionInfo info) {
    OuterLayout layout = info.getAttribute(OUTER_LAYOUT_KEY);
    if (layout == null) {
      layout = OuterLayout.STANDARD;
      setLayout(info, layout);
    }
    return layout;
  }
}
