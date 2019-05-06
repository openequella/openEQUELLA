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

package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;

public class SimpleLink extends HtmlLinkState {

  public SimpleLink(String href, Label label, String target) {
    setBookmark(new SimpleBookmark(href));
    setLabel(label);
    setTarget(target);
  }

  public SimpleLink(String href, String label, String target) {
    this(href, new TextLabel(label), target);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends HtmlComponentState> Class<T> getClassForRendering() {
    return (Class<T>) HtmlLinkState.class;
  }
}
