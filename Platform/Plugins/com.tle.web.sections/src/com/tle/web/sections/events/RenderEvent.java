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

package com.tle.web.sections.events;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SimpleSectionId;

public class RenderEvent extends AbstractSectionEvent<RenderEventListener> {
  private final String listenerId;
  private final SectionId sectionId;
  private final RenderContext renderContext;
  private RenderResultListener listener;

  public RenderEvent(RenderContext renderContext, String id, RenderResultListener listener) {
    this(renderContext, new SimpleSectionId(id), listener);
  }

  public RenderEvent(RenderContext renderContext, SectionId id, RenderResultListener listener) {
    this.renderContext = renderContext;
    this.sectionId = id;
    this.listenerId = id != null ? id.getSectionId() : null;
    this.listener = listener;
  }

  public RenderResultListener getListener() {
    return listener;
  }

  public void setListener(RenderResultListener listener) {
    this.listener = listener;
  }

  public RenderContext getRenderContext() {
    return renderContext;
  }

  public void returnResult(SectionResult result) {
    if (listener != null) {
      listener.returnResult(result, sectionId.getSectionId());
    }
  }

  @Override
  public SectionId getForSectionId() {
    return sectionId;
  }

  @Override
  public String getListenerId() {
    return listenerId;
  }

  @Override
  public void fire(SectionId sectionId, SectionInfo info, RenderEventListener eventListener)
      throws Exception {
    eventListener.render(new StandardRenderEventContext(sectionId, renderContext, this));
  }

  @Override
  public Class<RenderEventListener> getListenerClass() {
    return RenderEventListener.class;
  }
}
