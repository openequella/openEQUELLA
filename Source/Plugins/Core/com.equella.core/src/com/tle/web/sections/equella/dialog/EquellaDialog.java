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

package com.tle.web.sections.equella.dialog;

import com.google.common.collect.ObjectArrays;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonSize;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.DummySectionInfo;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.dialog.AbstractDialog;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.template.Decorations;
import com.tle.web.template.DialogTemplate;
import java.util.Collection;
import javax.inject.Inject;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class EquellaDialog<S extends DialogModel> extends AbstractDialog<S> {
  @Inject protected DialogTemplate template;

  private boolean alwaysShowFooter;
  private ElementId footerId;
  private ElementId titleId;

  @Override
  protected SectionRenderable getDialogContents(RenderContext context) {
    return template.getLayout(
        getTitleLabel(context),
        context,
        getDialogTemplate(context),
        this,
        getTemplateCloseFunction(),
        getContentBodyClass(context),
        footerId,
        titleId);
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    footerId = new AppendedElementId(this, "footer");
    titleId = new AppendedElementId(this, "title");
  }

  @Override
  protected void setupModal(RenderEventContext context) {
    Decorations.getDecorations(context).clearAllDecorations();
  }

  @Nullable
  protected String getContentBodyClass(RenderContext context) {
    return null;
  }

  public JSCallable getFooterUpdate(SectionTree tree, ParameterizedEvent event) {
    return getFooterUpdate(tree, event, footerId.getElementId(new DummySectionInfo()));
  }

  public JSCallable getFooterUpdate(SectionTree tree, ParameterizedEvent event, String... ids) {
    return ajaxEvents.getAjaxUpdateDomFunction(
        tree,
        this,
        event,
        ajaxEvents.getEffectFunction(EffectType.REPLACE_IN_PLACE),
        ObjectArrays.concat(footerId.getElementId(new DummySectionInfo()), ids));
  }

  @Nullable
  protected abstract Label getTitleLabel(RenderContext context);

  protected JSHandler getTemplateCloseFunction() {
    return new OverrideHandler(getCloseFunction());
  }

  protected TemplateResult getDialogTemplate(RenderContext context) {
    GenericTemplateResult tr = new GenericTemplateResult();

    tr.addNamedResult(DialogTemplate.BODY, getRenderableContents(context));

    Collection<Button> actions = collectFooterActions(context);
    if (alwaysShowFooter || !Check.isEmpty(actions)) {
      SectionRenderable sr = null;
      for (Button b : actions) {
        b.getState(context).setOverrideRendererType(EquellaButtonExtension.BOOTSTRAP_BUTTON);
        ButtonRenderer br = (ButtonRenderer) SectionUtils.renderSection(context, b);
        if (br != null) {
          ButtonType type = b.getComponentAttribute(ButtonType.class);
          if (type != null) {
            br.showAs(type);
          }
          br.setSize(ButtonSize.MEDIUM);

          sr = CombinedRenderer.combineResults(sr, br);
        }
      }

      if (sr == null && alwaysShowFooter) {
        sr = SectionUtils.convertToRenderer("");
      }

      tr.addNamedResult(DialogTemplate.FOOTER, sr);
    }

    return tr;
  }

  @Nullable
  protected Collection<Button> collectFooterActions(RenderContext context) {
    return null;
  }

  @Nullable
  @Override
  protected SectionRenderable getRenderableContents(RenderContext context) {
    throw new RuntimeException("Must implement getRenderableContents() or getDialogTemplate()");
  }

  public ElementId getFooterId() {
    return footerId;
  }

  public ElementId getTitleId() {
    return titleId;
  }

  protected void setAlwaysShowFooter(boolean show) {
    this.alwaysShowFooter = show;
  }
}
