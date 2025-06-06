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

package com.tle.web.sections.standard;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;

public class Link extends AbstractEventOnlyComponent<HtmlLinkState> {
  private boolean disablable;

  public Link() {
    super(RendererConstants.LINK);
  }

  @Override
  public Class<HtmlLinkState> getModelClass() {
    return HtmlLinkState.class;
  }

  public void setBookmark(SectionInfo info, Bookmark bookmark) {
    HtmlLinkState state = getState(info);
    state.setBookmark(bookmark);
  }

  public void setDisablable(boolean isDisablable) {
    this.disablable = isDisablable;
  }

  @Override
  protected HtmlLinkState setupState(SectionInfo info, HtmlLinkState state) {
    super.setupState(info, state);
    state.setDisablable(disablable);
    return state;
  }
}
