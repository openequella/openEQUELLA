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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderResultListener;

public class TemplateResultCollector implements RenderResultListener {
  private final CombinedTemplateResult templateResult = new CombinedTemplateResult();
  private final Map<String, List<SectionRenderable>> renderablesMap =
      new LinkedHashMap<String, List<SectionRenderable>>();
  private final String prefix;

  public TemplateResultCollector() {
    this(""); // $NON-NLS-1$
  }

  public TemplateResultCollector(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void returnResult(SectionResult result, String fromId) {
    if (result instanceof TemplateResult) {
      templateResult.addOtherTemplate((TemplateResult) result);
      return;
    }
    if (!(result instanceof SectionRenderable)) {
      result = new PreRenderOnly((PreRenderable) result);
    }
    if (result instanceof SectionRenderable) {
      List<SectionRenderable> list = renderablesMap.get(fromId);
      if (list == null) {
        list = new ArrayList<SectionRenderable>();
        renderablesMap.put(fromId, list);
      }
      list.add((SectionRenderable) result);
    } else {
      throw new SectionsRuntimeException("Never heard of that!"); // $NON-NLS-1$
    }
  }

  /**
   * Collect all results into a TemplateResult.
   *
   * <ul>
   *   <li>If a result is named, it is added to the Template.
   *   <li>If a result is unnamed, and another result from the same section has a name, it gets
   *       added to the first named result.
   *   <li>If no results have a name for the particular section, they get added as "body".
   * </ul>
   *
   * @return The TemplateResult with all SectionRenderables added.
   */
  public CombinedTemplateResult getTemplateResult() {
    for (String sectionId : renderablesMap.keySet()) {
      String firstName = null;
      List<SectionRenderable> list = renderablesMap.get(sectionId);
      Iterator<SectionRenderable> iter = list.iterator();
      while (iter.hasNext()) {
        SectionRenderable renderable = iter.next();
        if (renderable instanceof NamedSectionResult) {
          NamedSectionResult namedResult = (NamedSectionResult) renderable;
          String newName = prefix + namedResult.getName();
          templateResult.addNamedResult(newName, renderable);
          if (firstName == null) {
            firstName = newName;
          }
          iter.remove();
        }
      }
      if (firstName == null) {
        firstName = prefix + "body"; // $NON-NLS-1$
      }
      for (SectionRenderable renderable : list) {
        templateResult.addNamedResult(firstName, renderable);
      }
    }
    renderablesMap.clear();
    return templateResult;
  }
}
