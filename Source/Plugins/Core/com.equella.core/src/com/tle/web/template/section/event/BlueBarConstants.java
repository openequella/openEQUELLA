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

import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
public final class BlueBarConstants {
  static {
    PluginResourceHandler.init(BlueBarConstants.class);
  }

  public static final String BLUEBAR_PREFIX = "bluebar_";

  @PlugKey("buttonbar.helpbutton")
  private static Label LABEL_HELPBUTTON;

  @PlugKey("buttonbar.screenoptionsbutton")
  private static Label LABEL_SCREENOPTIONS;

  public enum Type {
    SCREENOPTIONS(LABEL_SCREENOPTIONS, 200),
    HELP(LABEL_HELPBUTTON, 100);

    private final Label label;
    private final int priority;

    Type(Label label, int priority) {
      this.label = label;
      this.priority = priority;
    }

    private String getKey() {
      return name().toLowerCase();
    }

    public BlueBarRenderable content(SectionRenderable renderable) {
      return new BlueBarRenderable(getKey(), label, renderable, priority);
    }
  }

  private BlueBarConstants() {}
}
