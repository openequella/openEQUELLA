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

package com.tle.web.sections.equella.render;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.core.javascript.JavascriptModule;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.LabelExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.ExtraAttributes;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TagProcessor;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.KeyLabel;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@SuppressWarnings("nls")
public class JQueryTimeAgo implements JavascriptModule, PreRenderable {
  private static final long serialVersionUID = 1L;
  public static final String DATE_FORMAT_APPROX = "format.approx";
  private static String KEY_PFX = AbstractPluginService.getMyPluginId(JQueryTimeAgo.class) + ".";

  private static final PreRenderable PRERENDER = new JQueryTimeAgo();
  private static final ExternallyDefinedFunction TIMEAGO_METHOD =
      new ExternallyDefinedFunction("timeago", 2, PRERENDER);

  private static final TagProcessor TIMEAGO_READY = new TimeAgoProcess(true, "timeago", true);
  private static final TagProcessor TIMEAGO_NOSUF_READY =
      new TimeAgoProcess(false, "timeago_nosuf", true);

  private static final TagProcessor TIMEAGO_READY_NO_FUZZY =
      new TimeAgoProcess(true, "timeago", false);
  private static final TagProcessor TIMEAGO_NOSUF_READY_NO_FUZZY =
      new TimeAgoProcess(false, "timeago_nosuf", false);

  public static class TimeAgoProcess implements TagProcessor {
    private final JQueryStatement statement;
    private final String clazz;

    public TimeAgoProcess(boolean showSuffix, String clazz, boolean fuzzyTime) {
      statement =
          new JQueryStatement(
              new JQuerySelector(Type.CLASS, clazz),
              new FunctionCallExpression(TIMEAGO_METHOD, showSuffix, fuzzyTime));
      this.clazz = clazz;
    }

    @Override
    public void processAttributes(SectionWriter writer, Map<String, String> attrs) {
      TagRenderer.addClass(attrs, clazz);
    }

    @Override
    public void preRender(PreRenderContext info) {
      info.addReadyStatements(statement);
    }
  }
  ;

  private static final JQueryLibraryInclude TIMEAGO_JS =
      new JQueryLibraryInclude("jquery.timeago.js", JQueryCore.PRERENDER);

  private static final AssignStatement ALLOW_FUTURE_OPTION =
      new AssignStatement(new ScriptExpression("jQuery.timeago.settings.allowFuture"), true);

  private static final AssignStatement STRINGS_OPTION;

  static {
    ObjectExpression strs = new ObjectExpression();
    strs.put("prefixAgo", s("prefixAgo"));
    strs.put("suffixAgo", s("suffixAgo"));
    strs.put("seconds", s("seconds"));
    strs.put("minute", s("minute"));
    strs.put("minutes", s("minutes"));
    strs.put("hour", s("hour"));
    strs.put("hours", s("hours"));
    strs.put("day", s("day"));
    strs.put("days", s("days"));
    strs.put("month", s("month"));
    strs.put("months", s("months"));
    strs.put("year", s("year"));
    strs.put("singularYear", s("singularYear"));
    strs.put("years", s("years"));
    strs.put("prefixFromNow", s("prefixFromNow"));
    strs.put("suffixFromNow", s("suffixFromNow"));
    strs.put("andExtraUnits", s("andExtraUnits"));

    STRINGS_OPTION =
        new AssignStatement(new ScriptExpression("jQuery.timeago.settings.strings"), strs);
  }

  private static JSExpression s(String keyPart) {
    return new LabelExpression(new KeyLabel(KEY_PFX + "timeago." + keyPart), true) {
      @Override
      public String getExpression(RenderContext info) {
        String text = label.getText();
        if (text.startsWith("function(")) {
          return text;
        }
        return super.getExpression(info);
      }
    };
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(TIMEAGO_JS);
    info.addStatements(STRINGS_OPTION);

    if (info.getAttribute(ALLOW_FUTURE_OPTION) != null) {
      info.addStatements(ALLOW_FUTURE_OPTION);
    }
  }

  public static TagRenderer timeAgoTag(Date date) {
    return timeAgoTag(date, false, DATE_FORMAT_APPROX);
  }

  public static TagRenderer timeAgoTag(Date date, boolean noSuffix, String displayDateFormat) {
    TagState state = new TagState();
    TimeZone zone = CurrentTimeZone.get();

    if (displayDateFormat.equals(DATE_FORMAT_APPROX)) {
      state.addTagProcessor(new ExtraAttributes("title", Long.toString(date.getTime())));
      state.addTagProcessor(noSuffix ? TIMEAGO_NOSUF_READY : TIMEAGO_READY);
    } else {
      state.addTagProcessor(
          new ExtraAttributes("title", new LocalDate(date, zone).format(Dates.DATE_AND_TIME)));
      state.addTagProcessor(noSuffix ? TIMEAGO_NOSUF_READY_NO_FUZZY : TIMEAGO_READY_NO_FUZZY);
    }

    TagRenderer renderer = new TagRenderer("abbr", state);
    SimpleSectionResult nested =
        new SimpleSectionResult(new LocalDate(date, zone).format(Dates.DATE_AND_TIME));
    renderer.setNestedRenderable(nested);
    return renderer;
  }

  public static void enableFutureTimes(SectionInfo info) {
    info.setAttribute(ALLOW_FUTURE_OPTION, ALLOW_FUTURE_OPTION);
  }

  @Override
  public String getDisplayName() {
    return CurrentLocale.get("com.tle.web.sections.jquery.modules.timeago.name");
  }

  @Override
  public String getId() {
    return "timeago";
  }

  @Override
  public PreRenderable getPreRenderer() {
    return PRERENDER;
  }
}
