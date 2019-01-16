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
import java.util.List;

import com.tle.web.sections.events.RenderContext;

public abstract class AbstractCombinedTemplateRenderable implements TemplateRenderable {
  private List<TemplateRenderable> templateRenderers;
  private Boolean exists;

  protected List<TemplateRenderable> getRenderers(RenderContext context) {
    if (templateRenderers == null) {
      templateRenderers = new ArrayList<TemplateRenderable>();
      setupTemplateRenderables(context);
    }
    return templateRenderers;
  }

  protected void addTemplateRenderable(TemplateRenderable renderable) {
    templateRenderers.add(renderable);
  }

  protected abstract void setupTemplateRenderables(RenderContext context);

  @Override
  public boolean exists(RenderContext context) {
    if (exists == null) {
      List<TemplateRenderable> renderers = getRenderers(context);
      for (TemplateRenderable templateRenderable : renderers) {
        if (templateRenderable != null && templateRenderable.exists(context)) {
          exists = true;
          return true;
        }
      }
      exists = false;
    }
    return exists;
  }
}
