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

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.FormValuesLibrary;
import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ListRendererDirective extends AbstractListDirective {
  @Override
  protected TagRenderer createTagRenderer(
      HtmlMutableListState state,
      Environment env,
      Map params,
      TemplateDirectiveBody body,
      TemplateModel[] loopVars) {
    return new FreemarkerListRenderer(state, env, body, loopVars);
  }

  public static class FreemarkerListRenderer extends TagRenderer implements JSValueComponent {
    private final HtmlMutableListState listState;
    private final TemplateDirectiveBody body;
    private final TemplateModel[] loopVars;
    private final BeansWrapper wrapper;

    public FreemarkerListRenderer(
        HtmlMutableListState state,
        Environment env,
        TemplateDirectiveBody body,
        TemplateModel[] loopVars) {
      super("ul", state); // $NON-NLS-1$
      this.wrapper = (BeansWrapper) env.getObjectWrapper();
      this.listState = state;
      this.body = body;
      this.loopVars = loopVars;
    }

    @Override
    public String getElementId(SectionInfo info) {
      return super.getElementId(info) + "_ul"; // $NON-NLS-1$
    }

    @Override
    public void realRender(SectionWriter writer) throws IOException {
      if (!listState.getOptions().isEmpty()) {
        super.realRender(writer);
      }
    }

    @SuppressWarnings({"nls"})
    @Override
    protected void writeMiddle(SectionWriter writer) throws IOException {
      List<Option<?>> options = listState.getOptions();
      for (Option<?> option : options) {
        writer.writeTag("li");
        writer.writeTag(
            "input", "type", "hidden", "name", listState.getName(), "value", option.getValue());
        loopVars[0] = new BeanModel(option, wrapper);
        try {
          body.render(writer);
        } catch (TemplateException e) {
          SectionUtils.throwRuntime(e);
        }
        writer.endTag("li");
      }
    }

    @Override
    public JSExpression createGetExpression() {
      return new FunctionCallExpression(FormValuesLibrary.GET_ALL_VALUES, listState.getName());
    }

    @Override
    public JSCallable createResetFunction() {
      throw new UnsupportedOperationException();
    }

    @Override
    public JSCallable createSetFunction() {
      throw new UnsupportedOperationException();
    }
  }
}
