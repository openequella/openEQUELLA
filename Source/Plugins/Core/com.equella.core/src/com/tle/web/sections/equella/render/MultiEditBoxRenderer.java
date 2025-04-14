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

import com.dytech.common.text.NumberStringComparator;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LocaleUtils;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.component.model.MultiEditBoxState;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.RendererCallback;
import com.tle.web.sections.standard.js.DelayedRenderer;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSListComponent;
import com.tle.web.sections.standard.js.impl.DelayedFunction;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("nls")
@NonNullByDefault
public class MultiEditBoxRenderer extends TagRenderer
    implements JSDisableable, DelayedRenderer<JSListComponent> {
  static {
    PluginResourceHandler.init(MultiEditBoxRenderer.class);
  }

  @PlugURL("scripts/component/multieditbox.js")
  private static String MULTI_SCRIPT;

  @PlugURL("css/component/multieditbox.css")
  private static String MULTI_STYLE;

  @PlugKey("component.multiedit.showall")
  private static String SHOW_ALL;

  @PlugKey("component.multiedit.collapse")
  private static Label COLLAPSE;

  private static JSCallable SETUP_FUNC =
      new ExternallyDefinedFunction("setupMultiEdit", new IncludeFile(MULTI_SCRIPT));
  private static JSCallable DISABLE_FUNC =
      new ExternallyDefinedFunction("disableMultiEdit", new IncludeFile(MULTI_SCRIPT));

  private static final CssInclude CSS = CssInclude.include(MULTI_STYLE).hasRtl().make();

  private final MultiEditBoxState state;

  private final HtmlValueState universal;
  private final HtmlListState localeSelector;
  private final HtmlLinkState collapse;
  private final List<Pair<Label, HtmlValueState>> localeRows;

  private int size;

  public MultiEditBoxRenderer(FreemarkerFactory view, MultiEditBoxState state) {
    super("div", state);
    this.state = state;
    this.size = state.getSize();
    addClass("multieditbox");
    nestedRenderable = view.createResultWithModel("component/multieditbox.ftl", this);

    universal = new HtmlValueState();
    localeRows = new ArrayList<Pair<Label, HtmlValueState>>();
    localeSelector = new HtmlListState();
    localeSelector.setId(state.getId() + "_loc");
    localeSelector.addRendererCallback(
        new RendererCallback() {
          @Override
          public void rendererSelected(RenderContext info, SectionRenderable renderer) {
            info.setAttribute(MultiEditBoxRenderer.this, renderer);
          }
        });
    collapse = new HtmlLinkState(COLLAPSE);
  }

  @Override
  public JSCallable createDisableFunction() {
    return new PrependedParameterFunction(DISABLE_FUNC, this);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(CSS);
    if (hasMultipleLangs()) {
      JSCallable delayed =
          new DelayedFunction<JSListComponent>(this, "setloc", this, 1) {
            @Override
            protected JSCallable createRealFunction(RenderContext info2, JSListComponent renderer) {
              return renderer.createSetFunction();
            }
          };
      AppendedElementId id = new AppendedElementId(this, "_" + getDefaultLocale());
      info.addReadyStatements(Js.call_s(SETUP_FUNC, this, id.getElementId(info), delayed));
    }
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    List<Option<?>> opts = new ArrayList<Option<?>>();

    for (String locale : state.getLocaleMap().keySet()) {
      AppendedElementId id = new AppendedElementId(this, "_" + locale);

      Pair<Label, HtmlValueState> localeRow = new Pair<Label, HtmlValueState>();
      HtmlValueState valState = state.getLocaleMap().get(locale);
      Label label = valState.getLabel();
      localeRow.setFirst(label);

      valState.setLabel(null); // Remove label to avoid duplicate.
      valState.addClass(id.getElementId(writer));
      valState.setElementId(id);
      valState.setLabel(null);
      localeRow.setSecond(valState);

      localeRows.add(localeRow);

      opts.add(new SimpleOption<String>(label.getText(), id.getElementId(writer)));
    }

    opts.add(new KeyOption<String>(SHOW_ALL, "all", null));

    universal.addClass("universalinput");

    localeSelector.setOptions(opts);
    localeSelector.addClass("localeselector");
    Set<String> emptyList = Collections.emptySet();
    localeSelector.setSelectedValues(emptyList);

    collapse.addClass("collapse");

    super.writeMiddle(writer);
  }

  private String getDefaultLocale() {
    Map<String, HtmlValueState> localeMap = state.getLocaleMap();
    TreeSet<Locale> editableLocales = new TreeSet<Locale>(localeComparator);

    Locale firstValidLoc = null;
    for (Map.Entry<String, HtmlValueState> entry : localeMap.entrySet()) {
      Locale locale = LocaleUtils.parseLocale(entry.getKey());
      if (!Check.isEmpty(entry.getValue().getValue()) && firstValidLoc == null) {
        firstValidLoc = locale;
      }
      editableLocales.add(locale);
    }

    Locale currentLocale = CurrentLocale.getLocale();

    if (!editableLocales.contains(currentLocale)) {
      Locale closestLocale = LocaleUtils.getClosestLocale(editableLocales, currentLocale);

      if (closestLocale != null) {
        currentLocale = closestLocale;

        if (!isEmpty(localeMap.values())
            && Check.isEmpty(localeMap.get(closestLocale.toString()).getValue())) {
          currentLocale = firstValidLoc;
        }
      } else {
        currentLocale = editableLocales.first();
      }
    }

    return currentLocale.toString();
  }

  private boolean isEmpty(Collection<HtmlValueState> values) {
    for (HtmlValueState valueState : values) {
      if (!Check.isEmpty(valueState.getValue())) {
        return false;
      }
    }
    return true;
  }

  private boolean hasMultipleLangs() {
    return state.getLocaleMap().keySet().size() > 1;
  }

  public HtmlValueState getUniversal() {
    return universal;
  }

  public HtmlListState getLocaleSelector() {
    return localeSelector;
  }

  public List<Pair<Label, HtmlValueState>> getLocaleRows() {
    return localeRows;
  }

  public HtmlLinkState getCollapse() {
    return collapse;
  }

  private final Comparator<Locale> localeComparator =
      new NumberStringComparator<Locale>() {
        private static final long serialVersionUID = 1L;

        @Override
        public String convertToString(Locale locale) {
          return locale.getDisplayName();
        }
      };

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Nullable
  @Override
  public JSListComponent getSelectedRenderer(RenderContext info) {
    return (JSListComponent) info.getAttribute(this);
  }
}
