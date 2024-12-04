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

package com.tle.web.template.section.event;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.template.section.HelpAndScreenOptionsSection;
import java.util.EventListener;

public class BlueBarEvent extends AbstractSectionEvent<BlueBarEventListener> {
  private final RenderContext context;

  public BlueBarEvent(RenderContext context) {
    this.context = context;
  }

  @Override
  public void fire(SectionId sectionId, SectionInfo info, BlueBarEventListener listener)
      throws Exception {
    listener.addBlueBarResults(context, this);
  }

  public void addHelp(SectionRenderable renderable) {
    if (renderable != null) {
      HelpAndScreenOptionsSection.addTab(context, BlueBarConstants.Type.HELP.content(renderable));
    }
  }

  @Override
  public Class<? extends EventListener> getListenerClass() {
    return BlueBarEventListener.class;
  }

  public void addTab(BlueBarRenderable blueBarRenderable) {
    if (blueBarRenderable != null) {
      HelpAndScreenOptionsSection.addTab(context, blueBarRenderable);
    }
  }

  public void addScreenOptions(SectionRenderable renderable) {
    if (renderable != null) {
      HelpAndScreenOptionsSection.addTab(
          context, BlueBarConstants.Type.SCREENOPTIONS.content(renderable));
    }
  }
}
