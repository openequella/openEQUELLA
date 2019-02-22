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

import com.tle.common.Check;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.statement.AssignAsFunction;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlIpAddressInputState;
import com.tle.web.sections.standard.model.HtmlNumberFieldState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("nls")
public class IpAddressInputRenderer extends AbstractInputRenderer
    implements JSDisableable, JSValueComponent {
  private static final PluginResourceHelper helper =
      ResourcesService.getResourceHelper(IpAddressInputRenderer.class);

  private static final CssInclude CSS = CssInclude.include(helper.url("css/ipaddress.css")).make();

  private static final IncludeFile JS =
      new IncludeFile(helper.url("js/ipaddress.js"), JQueryCore.PRERENDER);
  private static JSCallable SET_F = new ExternallyDefinedFunction("setIp", JS);
  private static JSCallable DISABLE_F = new ExternallyDefinedFunction("disableFields", JS);

  private HtmlIpAddressInputState ipstate;

  public IpAddressInputRenderer(HtmlIpAddressInputState state) {
    super(state, "hidden");
    this.ipstate = state;
  }

  @Override
  public void preRender(com.tle.web.sections.events.PreRenderContext info) {
    super.preRender(info);
    info.preRender(CSS);

    if (isDisabled()) {
      info.addReadyStatements(Js.call_s(createDisableFunction(), true));
    }
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    writer.write("<div class='ip-input'>");
    super.writeStart(writer, attrs);
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    super.writeEnd(writer);

    String ip = ipstate.getValue();
    List<String> fields = splitIp(ip);

    int fieldCount = 0;
    while (fieldCount < 4) {
      HtmlNumberFieldState ipLimit = new HtmlNumberFieldState();
      ipLimit.setMax(255);
      ipLimit.setMin(0);
      ipLimit.addClass("ip-field");
      // this is the most ass-backwards way to set an id, but setElementId
      // isn't playing nice
      ipLimit.setId(new AppendedElementId(this, "_field_" + fieldCount).getElementId(writer));
      ipLimit.addEventStatements(
          "input", Js.call_s(SET_F, fieldCount, this, ipLimit.getElementId(writer)));
      ipLimit.setValue(fields == null ? "0" : fields.get(fieldCount));
      writer.render(new NumberFieldRenderer(ipLimit));
      if (fieldCount < 3) {
        writer.append(".");
      }
      fieldCount++;
    }
    writer.append("/");
    HtmlNumberFieldState maskLimit = new HtmlNumberFieldState();
    maskLimit.setMin(0);
    maskLimit.setMax(32);
    maskLimit.addClass("ip-field");
    maskLimit.setId(new AppendedElementId(this, "_field_4").getElementId(writer));
    maskLimit.addEventStatements(
        "input", Js.call_s(SET_F, 4, this, maskLimit.getElementId(writer)));
    maskLimit.setValue(fields == null ? "32" : fields.get(fieldCount));
    writer.render(new NumberFieldRenderer(maskLimit));

    writer.write("</div>");
  }

  private List<String> splitIp(String ip) {
    if (!Check.isEmpty(ip)) {
      ArrayList<String> ipList = new ArrayList<String>();
      for (String field : ip.split("\\.")) {
        if (field.contains("/")) {
          ipList.addAll(Arrays.asList(field.split("/")));
        } else {
          ipList.add(field);
        }
      }
      return ipList;
    }
    return null;
  }

  @Override
  public JSExpression createGetExpression() {
    return PropertyExpression.create(new ElementByIdExpression(this), "value");
  }

  @Override
  public JSCallable createSetFunction() {
    return new AssignAsFunction(createGetExpression());
  }

  @Override
  public JSCallable createResetFunction() {
    return null;
  }

  @Override
  public JSCallable createDisableFunction() {
    return new PrependedParameterFunction(DISABLE_F, this);
  }
}
