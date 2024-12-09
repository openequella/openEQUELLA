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

import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import java.util.Collections;
import java.util.List;

public class BlueBarRenderable {
  private final String key;
  private final Label label;
  private SectionRenderable renderable;
  private final int priority;

  public BlueBarRenderable(
      String unprefixedKey, Label label, SectionRenderable renderable, int priority) {
    this.key = unprefixedKey;
    this.label = label;
    this.renderable = renderable;
    this.priority = priority;
  }

  public Label getLabel() {
    return label;
  }

  public String getKey() {
    return key;
  }

  public void combineWith(SectionRenderable renderable) {
    this.renderable = CombinedRenderer.combineResults(this.renderable, renderable);
  }

  public SectionRenderable getRenderable() {
    return renderable;
  }

  public int getPriority() {
    return priority;
  }

  public static List<BlueBarRenderable> help(SectionRenderable renderable) {
    return Collections.singletonList(BlueBarConstants.Type.HELP.content(renderable));
  }
}
