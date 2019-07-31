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

package com.tle.web.sections.render;

import java.io.Serializable;

public class TextLabel implements Label, Serializable {
  private static final long serialVersionUID = 1L;

  public static final TextLabel SPACE = new TextLabel(" ", true); // $NON-NLS-1$

  private final String text;
  private final boolean html;

  public TextLabel(String text) {
    this(text, false);
  }

  public TextLabel(String text, boolean html) {
    this.text = text;
    this.html = html;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public boolean isHtml() {
    return html;
  }
}
