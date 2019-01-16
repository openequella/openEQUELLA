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

package com.tle.web.template.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.template.RenderNewTemplate;
import com.tle.web.template.section.event.BlueBarConstants;
import com.tle.web.template.section.event.BlueBarRenderable;

@SuppressWarnings("nls")
public class HelpAndScreenOptionsSection
    extends AbstractPrototypeSection<HelpAndScreenOptionsSection.HelpAndScreenOptionsModel>
    implements HtmlRenderer {
  private static final String KEY_TABS = "BlueBarRenderables";

  private static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(HelpAndScreenOptionsSection.class);
  private static final ExternallyDefinedFunction BLIND =
      new ExternallyDefinedFunction(
          "blindDomResults",
          JQueryUIEffects.BLIND,
          new IncludeFile(resources.url("scripts/helpandoptions.js")));

  @AjaxFactory private AjaxGenerator ajax;
  @EventFactory private EventGenerator events;
  @ViewFactory private FreemarkerFactory viewFactory;

  private JSCallable showFunc;
  private JSCallable hideFunc;

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    if (RenderNewTemplate.isNewLayout(context)) {
      return null;
    }
    return new GenericNamedResult(
        "helpandoptions",
        new PreRenderable() {
          @Override
          public void preRender(PreRenderContext info) {
            final HelpAndScreenOptionsModel model = getModel(info);
            List<BlueBarRenderable> renderables =
                new ArrayList<BlueBarRenderable>(model.getTabMap().values());
            final List<HtmlComponentState> buttons = new ArrayList<HtmlComponentState>();
            final String shown = model.getShown();
            model.setButtons(buttons);

            Collections.sort(
                renderables,
                new Comparator<BlueBarRenderable>() {
                  @Override
                  public int compare(BlueBarRenderable o1, BlueBarRenderable o2) {
                    return o1.getPriority() - o2.getPriority();
                  }
                });
            for (BlueBarRenderable blueButton : renderables) {
              String key = BlueBarConstants.BLUEBAR_PREFIX + blueButton.getKey();
              SectionRenderable renderable = blueButton.getRenderable();

              final HtmlComponentState button = new HtmlComponentState();
              button.setLabel(blueButton.getLabel());
              button.setId(key + "_button");
              if (shown != null && shown.equals(key)) {
                final BlueBarContent content = new BlueBarContent();
                content.setId(key);
                content.setRenderable(renderable);
                model.setContent(content);
                button.addClass("active");
                button.setAttribute(Icon.class, Icon.UP);
                button.setClickHandler(new OverrideHandler(hideFunc));
              } else {
                button.setAttribute(Icon.class, Icon.DOWN);
                button.setClickHandler(new OverrideHandler(showFunc, key));
              }
              buttons.add(button);
            }
          }
        },
        viewFactory.createResult("helpandoptions.ftl", context));
  }

  @EventHandlerMethod
  public void show(SectionInfo info, String key) {
    final HelpAndScreenOptionsModel model = getModel(info);
    model.setShown(key);
  }

  @EventHandlerMethod
  public void hideEverything(SectionInfo info) {
    final HelpAndScreenOptionsModel model = getModel(info);
    model.setShown(null);
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    showFunc =
        ajax.getAjaxUpdateDomFunction(
            tree, null, events.getEventHandler("show"), BLIND, "helpAndOptions");

    hideFunc =
        ajax.getAjaxUpdateDomFunction(
            tree, null, events.getEventHandler("hideEverything"), BLIND, "helpAndOptions");
  }

  @Override
  public String getDefaultPropertyName() {
    return "hao";
  }

  @Override
  public HelpAndScreenOptionsModel getModel(SectionInfo context) {
    return super.getModel(context);
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new HelpAndScreenOptionsModel(info);
  }

  @Override
  public Class<HelpAndScreenOptionsModel> getModelClass() {
    return HelpAndScreenOptionsModel.class;
  }

  public static class BlueBarContent {
    private String id;
    private SectionRenderable renderable;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public SectionRenderable getRenderable() {
      return renderable;
    }

    public void setRenderable(SectionRenderable renderable) {
      this.renderable = renderable;
    }
  }

  public static class HelpAndScreenOptionsModel {
    private final SectionInfo info;

    public HelpAndScreenOptionsModel(SectionInfo info) {
      this.info = info;
    }

    @Bookmarked(stateful = false, name = "sh")
    private String shown;

    private List<HtmlComponentState> buttons;
    private BlueBarContent content;

    public Map<String, BlueBarRenderable> getTabMap() {
      return HelpAndScreenOptionsSection.getContent(info);
    }

    public String getShown() {
      return shown;
    }

    public void setShown(String shown) {
      this.shown = shown;
    }

    public List<HtmlComponentState> getButtons() {
      return buttons;
    }

    public void setButtons(List<HtmlComponentState> buttons) {
      this.buttons = buttons;
    }

    public BlueBarContent getContent() {
      return content;
    }

    public void setContent(BlueBarContent content) {
      this.content = content;
    }
  }

  public static void addTabs(SectionInfo info, List<BlueBarRenderable> results) {
    Map<String, BlueBarRenderable> renderables = getContent(info);
    for (BlueBarRenderable blueBarRenderable : results) {
      String key = blueBarRenderable.getKey();
      BlueBarRenderable existing = renderables.get(key);
      if (existing != null) {
        existing.combineWith(blueBarRenderable.getRenderable());
      } else {
        renderables.put(key, blueBarRenderable);
      }
    }
  }

  public static Map<String, BlueBarRenderable> getContent(SectionInfo info) {
    return info.getAttributeSafe(KEY_TABS, HashMap.class);
  }

  public static void addTab(SectionInfo info, BlueBarRenderable result) {
    addTabs(info, Collections.singletonList(result));
  }

  public static void addHelp(RenderContext context, SectionRenderable renderable) {
    if (renderable != null) {
      addTab(context, BlueBarConstants.Type.HELP.content(renderable));
    }
  }

  public static void addScreenOptions(RenderEventContext context, SectionRenderable renderable) {
    if (renderable != null) {
      addTab(context, BlueBarConstants.Type.SCREENOPTIONS.content(renderable));
    }
  }
}
