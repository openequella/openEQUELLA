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

package com.tle.web.freemarker.methods;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.StyleableRenderer;
import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.Map;

/**
 * Base FreeMarker directive bridge for rendering Section-backed UI components.
 *
 * <p>Implementations (for example {@code RenderDirective}, exposed as {@code _render} in FreeMarker
 * configuration) adapt a template {@code section} parameter into a {@link SectionRenderable} via
 * {@link #getRenderable(Object, Map)}.
 *
 * <p>This base class centralises common rendering responsibilities:
 *
 * <ul>
 *   <li>validating and unwrapping the FreeMarker {@code section} model,
 *   <li>applying optional style/class/id attributes for {@link StyleableRenderer},
 *   <li>bridging nested FreeMarker body content into Sections via {@link BodyDirectiveRenderable},
 *       and
 *   <li>executing the standard Sections lifecycle ({@code preRender} then {@code realRender}).
 * </ul>
 */
@NonNullByDefault
public abstract class AbstractRenderDirective extends SectionsTemplateModel
    implements TemplateDirectiveModel {
  @NonNullByDefault(false)
  @SuppressWarnings({"unchecked"})
  @Override
  public void execute(Environment env, Map params, TemplateModel[] arg2, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    RenderContext renderContext = getSectionWriter();
    try {
      Object model = params.get("section");
      if (model instanceof AdapterTemplateModel) {
        Object wrapped = ((AdapterTemplateModel) model).getAdaptedObject(Object.class);
        if (wrapped != null) {
          SectionRenderable renderable = getRenderable(wrapped, params);
          if (renderable == null) {
            return;
          }
          if (renderable instanceof StyleableRenderer) {
            ((StyleableRenderer) renderable)
                .setStyles(
                    getParam("style", params), getParam("class", params), getParam("id", params));
          }
          if (renderable instanceof NestedRenderable nestedRenderable && body != null) {
            nestedRenderable.setNestedRenderable(
                new BodyDirectiveRenderable(body, nestedRenderable.getNestedRenderable()));
          }
          SectionWriter writer = new SectionWriter(env.getOut(), renderContext);
          writer.preRender(renderable);
          renderable.realRender(writer);
        }
      } else {
        throw new RuntimeException(
            "'section' parameter to render macro was not of the appropriate type (was "
                + model.getClass()
                + ").  Perhaps you named your section property the same as a method name?");
      }
    } catch (Exception e) {
      throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
    }
  }

  @Nullable
  protected String getParam(String param, Map<?, ?> params) {
    Object val = params.get(param);
    if (val != null) {
      return val.toString();
    }
    return null;
  }

  @Nullable
  protected abstract SectionRenderable getRenderable(
      Object section, Map<String, TemplateModel> params);
}
