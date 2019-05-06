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

package com.tle.web.sections.standard.renderers.list;

import com.tle.web.freemarker.methods.SectionsTemplateModel;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractListDirective extends SectionsTemplateModel
    implements TemplateDirectiveModel {
  @Override
  @SuppressWarnings("unchecked")
  public void execute(
      Environment env,
      @SuppressWarnings("rawtypes") Map params,
      TemplateModel[] loopVars,
      TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object section = params.get("section"); // $NON-NLS-1$
    if (section == null) {
      throw new SectionsRuntimeException("Section is null"); // $NON-NLS-1$
    }
    if (section instanceof AdapterTemplateModel) {
      Object wrapped = ((AdapterTemplateModel) section).getAdaptedObject(Object.class);
      if (wrapped instanceof SectionId) {
        SectionId sectionId = (SectionId) wrapped;
        RenderContext context = getSectionWriter();
        AbstractRenderedComponent<HtmlComponentState> component =
            (AbstractRenderedComponent<HtmlComponentState>) context.getSectionForId(sectionId);
        component.setRendererType(context, "null"); // $NON-NLS-1$
        ResultListCollector results = new ResultListCollector(false);
        try {
          context.processEvent(new RenderEvent(context, sectionId.getSectionId(), results));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        HtmlComponentState state = component.getState(context);
        TagRenderer renderer =
            createTagRenderer((HtmlMutableListState) state, env, params, body, loopVars);
        state.setBeenRendered(true);
        state.fireRendererCallback(context, renderer);
        setupStyles(renderer, params);

        try (SectionWriter writer = new SectionWriter(env.getOut(), context)) {
          writer.render(renderer);
        }
      } else {
        throw new SectionsRuntimeException("Need a sectionId"); // $NON-NLS-1$
      }
    }
  }

  protected abstract TagRenderer createTagRenderer(
      HtmlMutableListState state,
      Environment env,
      Map<?, ?> params,
      TemplateDirectiveBody body,
      TemplateModel[] loopVars)
      throws TemplateModelException;

  @SuppressWarnings("nls")
  private void setupStyles(TagRenderer renderer, Map<String, TemplateModel> params) {
    String id = getParam(params, "id");
    String style = getParam(params, "style");
    String styleClass = getParam(params, "class");

    renderer.setStyles(style, styleClass, id);
  }

  private String getParam(Map<String, TemplateModel> params, String key) {
    TemplateModel templateModel = params.get(key);
    if (templateModel != null) {
      return templateModel.toString();
    }
    return null;
  }
}
