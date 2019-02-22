/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.StyleableRenderer;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlLinkState;

public interface LinkTagRenderer
    extends JSDisableable, NestedRenderable, ElementId, StyleableRenderer {
  void setTarget(String target);

  void setRel(String rel);

  void setTitle(Label title);

  void setLabel(Label label);

  void setDisabled(boolean disabled);

  void setElementId(ElementId elemId);

  void ensureClickable();

  HtmlLinkState getLinkState();
}
