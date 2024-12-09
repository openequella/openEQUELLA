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

package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.AssignableFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CEditBox;
import com.tle.web.wizard.controls.SimpleValueControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.section.WizardBodySection;
import com.tle.web.wizard.standard.controls.EditBox.EditBoxModel;

@Bind
public class EditBox extends AbstractWebControl<EditBoxModel> implements SimpleValueControl {
  @ViewFactory(name = "wizardFreemarkerFactory")
  private FreemarkerFactory viewFactory;

  @Component(register = false, stateful = false)
  private TextField field;

  @Component
  @PlugKey("duplicatewarning.linktext")
  private Link duplicateWarningLink;

  private CEditBox box;

  @EventFactory private EventGenerator events;
  @AjaxFactory private AjaxGenerator ajax;

  @Override
  public void registered(String id, SectionTree tree) {
    field.setParameterId(getFormName());
    StatementHandler fieldValueChangedHandler =
        new StatementHandler(
            ajax.getAjaxUpdateDomFunction(
                tree,
                this,
                events.getEventHandler("onFieldValueChanged"),
                ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE),
                id + "_editbox_duplicate_warning"));
    if (box.isCheckDuplication() || box.isForceUnique()) {
      field.setEventHandler(JSHandler.EVENT_CHANGE, fieldValueChangedHandler);
    }
    tree.registerInnerSection(field, id);
    duplicateWarningLink.setClickHandler(events.getNamedHandler("openDuplicatePage"));
    super.registered(id, tree);
  }

  @EventHandlerMethod
  public void onFieldValueChanged(SectionInfo info) {
    box.checkDuplicate(field.getValue(info));
  }

  @EventHandlerMethod
  public void openDuplicatePage(SectionInfo info) {
    WizardBodySection bodySection = info.lookupSection(WizardBodySection.class);
    bodySection.goToDuplicateDataTab(info);
  }

  @Override
  public void setWrappedControl(HTMLControl control) {
    super.setWrappedControl(control);
    this.box = (CEditBox) control;
    if (control.getSize1() == 0) {
      control.setSize1(70);
    }
    this.box.setEditBoxSection(this);
  }

  @Override
  public void doEdits(SectionInfo info) {
    String value = field.getValue(info);
    HTMLControl ctrl = getWrappedControl();
    if (value == null) {
      ctrl.setValues();
    } else {
      ctrl.setValues(value);
    }
  }

  @Override
  @SuppressWarnings("nls")
  public SectionResult renderHtml(RenderEventContext context) throws Exception {
    field.setValue(context, box.getValue());
    if (getSize2() > 1) {
      field.setEventHandler(
          context,
          "keyup",
          new OverrideHandler(
              new ScriptStatement(
                  "if(this.value.length > 8192) this.value = this.value.slice(0, 8192);")));
    }
    if (box.isMandatory()) {
      field.getState(context).setAccessibilityAttr(TagRenderer.ARIA_REQUIRED, String.valueOf(true));
    }
    if (isInvalid() || getMessage() != null) {
      field
          .getState(context)
          .setAccessibilityAttr(
              TagRenderer.ARIA_LABELLEDBY,
              field.getElementId(context) + "_label " + field.getElementId(context) + "_invalid ");
    }
    addDisabler(context, field);
    return viewFactory.createResult("editbox.ftl", context);
  }

  public TextField getField() {
    return field;
  }

  public Link getDuplicateWarningLink() {
    return duplicateWarningLink;
  }

  @Override
  public JSAssignable createEditFunction() {
    return AssignableFunction.get(field.createSetFunction());
  }

  @Override
  public JSAssignable createResetFunction() {
    return AssignableFunction.get(field.createResetFunction());
  }

  @Override
  public JSAssignable createTextFunction() {
    return new AnonymousFunction(new ReturnStatement(field.createGetExpression()));
  }

  @Override
  public JSAssignable createValueFunction() {
    return new AnonymousFunction(new ReturnStatement(field.createGetExpression()));
  }

  @Override
  protected ElementId getIdForLabel() {
    return field;
  }

  @Override
  public EditBoxModel instantiateModel(SectionInfo info) {
    return new EditBoxModel();
  }

  @Override
  public Class<EditBoxModel> getModelClass() {
    return EditBoxModel.class;
  }

  public class EditBoxModel extends WebControlModel {

    public String getDuplicateWarningMessage() {
      return CoreStrings.text("duplicatewarning.message");
    }

    public boolean isDisplayDuplicateWarning() {
      return isDuplicateWarning();
    }
  }
}
