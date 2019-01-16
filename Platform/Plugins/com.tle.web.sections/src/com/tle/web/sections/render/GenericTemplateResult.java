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

package com.tle.web.sections.render;

import java.util.HashMap;
import java.util.Map;

import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.events.RenderContext;

public class GenericTemplateResult implements TemplateResult {
  private final Map<String, SectionRenderable> results = new HashMap<String, SectionRenderable>();

  public GenericTemplateResult() {
    // nothing
  }

  public GenericTemplateResult(NamedSectionResult... results) {
    for (NamedSectionResult result : results) {
      addNamedResult(result);
    }
  }

  public void addNamedResult(NamedSectionResult result) {
    addNamedResult(result.getName(), result);
  }

  public GenericTemplateResult addNamedResult(String name, SectionRenderable result) {
    SectionRenderable current = results.get(name);
    if (current != null) {
      result = CombinedRenderer.combineResults(current, result);
    }
    results.put(name, result);
    return this;
  }

  @Override
  public TemplateRenderable getNamedResult(RenderContext info, String name) {
    SectionRenderable renderable = results.get(name);
    if (renderable != null) {
      return new WrappedTemplateRenderable(renderable);
    }
    return null;
  }

  @Override
  public String toString() {
    return results.toString();
  }
}
