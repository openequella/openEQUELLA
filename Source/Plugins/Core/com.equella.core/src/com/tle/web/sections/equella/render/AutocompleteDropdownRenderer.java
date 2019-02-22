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

package com.tle.web.sections.equella.render;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("nls")
public class AutocompleteDropdownRenderer extends DropDownRenderer {
  static {
    PluginResourceHandler.init(AutocompleteDropdownRenderer.class);
  }

  private static final PluginResourceHelper resHelper =
      ResourcesService.getResourceHelper(AutocompleteDropdownRenderer.class);

  public static final IncludeFile SELECT2_HELPER_LIB =
      new IncludeFile(
          resHelper.url("scripts/component/select2helper.js"),
          new IncludeFile(resHelper.url("js/select2.js")).hasMin(),
          CssInclude.include(resHelper.url("css/select2.css")).hasMin().make());

  private static final JSCallable SETTER =
      new ExternallyDefinedFunction("select2helper.setValue", 2, SELECT2_HELPER_LIB);
  private static final JSCallable GETTER =
      new ExternallyDefinedFunction("select2helper.getValue", 1, SELECT2_HELPER_LIB);
  private static final JSCallable RESET =
      new ExternallyDefinedFunction("select2helper.reset", 1, SELECT2_HELPER_LIB);

  private static final JSCallable SETUP =
      new ExternallyDefinedFunction("select2helper.setup", SELECT2_HELPER_LIB);

  @PlugKey("renderer.autocompletedropdown.placeholder")
  private static String PLACEHOLDER_KEY;

  @PlugKey("renderer.autocompletedropdown.searching")
  private static String SEARCHING_KEY;

  public AutocompleteDropdownRenderer(HtmlListState state) {
    super(state);
  }

  @Override
  public void preRender(PreRenderContext info) {
    final ObjectExpression params = new ObjectExpression();
    final HtmlComponentState selectState = getHtmlState();

    // inherit any classes put on the SELECT element
    final Set<String> classes = selectState.getStyleClasses();
    final StringBuilder classString = new StringBuilder();
    boolean first = true;
    if (!Check.isEmpty(classes)) {
      for (String clas : classes) {
        if (!first) {
          classString.append(' ');
        }
        classString.append(clas);
        first = false;
      }
    }
    params.put("class", classString.toString());
    params.put("placeholderText", CurrentLocale.get(PLACEHOLDER_KEY));
    params.put("searchingText", CurrentLocale.get(SEARCHING_KEY));

    JSCallAndReference extension = null;
    final AutocompleteDropdownRenderOptions options =
        selectState.getAttribute(AutocompleteDropdownRenderOptions.class);
    if (options != null) {
      extension = options.getExtension(info);
      for (Map.Entry<String, Object> kv : options.getParameters(info).entrySet()) {
        params.put(kv.getKey(), kv.getValue());
      }
    }

    info.addReadyStatements(
        new FunctionCallStatement(SETUP, new JQuerySelector(this), params, extension));
    super.preRender(info);
  }

  @Override
  public JSExpression createGetExpression() {
    return Js.call(GETTER, this);
  }

  @Override
  public JSCallable createSetFunction() {
    return new PrependedParameterFunction(SETTER, this);
  }

  @Override
  public JSCallable createResetFunction() {
    return new PrependedParameterFunction(RESET, this);
  }

  public interface AutocompleteDropdownRenderOptions {
    JSCallAndReference getExtension(PreRenderContext info);

    Map<String, Object> getParameters(PreRenderContext info);
  }
}
