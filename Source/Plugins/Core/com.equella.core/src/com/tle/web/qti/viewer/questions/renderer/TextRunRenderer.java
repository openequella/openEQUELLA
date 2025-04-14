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

package com.tle.web.qti.viewer.questions.renderer;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;

public class TextRunRenderer extends QtiNodeRenderer {
  private final TextRun model;

  @AssistedInject
  public TextRunRenderer(@Assisted TextRun model, @Assisted QtiViewerContext context) {
    super(model, context);
    this.model = model;
  }

  @Override
  protected SectionRenderable createTopRenderable() {
    return new LabelRenderer(new TextLabel(model.getTextContent()));
  }

  @Override
  protected boolean isNestedTop() {
    return false;
  }
}
