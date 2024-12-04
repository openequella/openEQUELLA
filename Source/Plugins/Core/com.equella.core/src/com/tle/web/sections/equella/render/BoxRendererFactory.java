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

package com.tle.web.sections.equella.render;

import com.google.inject.Inject;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.component.model.BoxState;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.FreemarkerComponentRendererFactory;
import javax.inject.Singleton;

@Bind
@Singleton
public class BoxRendererFactory extends FreemarkerComponentRendererFactory {
  @Inject private AccessibilityModeService accessibilityService;

  @Override
  public SectionRenderable getRenderer(
      RendererFactory rendererFactory,
      SectionInfo info,
      String renderer,
      HtmlComponentState state) {
    return new BoxRenderer(
        factory, (BoxState) state, accessibilityService.isAccessibilityMode()); // NOSONAR
  }
}
