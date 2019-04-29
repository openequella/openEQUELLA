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

package com.tle.web.sections.equella.utils;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.CoreStrings;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.CloseWindowResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import java.util.List;
import javax.inject.Inject;
import net.sf.json.JSONArray;

@SuppressWarnings("nls")
@Bind
public class SelectRoleDialog extends AbstractOkayableDialog<SelectRoleDialog.Model> {
  static {
    PluginResourceHandler.init(SelectRoleDialog.class);
  }

  private static final int WIDTH = 550;

  private CurrentRolesCallback currentRolesCallback;
  @Inject protected SelectRoleSection section;
  @ViewFactory private FreemarkerFactory viewFactory;

  @PlugKey("utils.selectroledialog.default.title")
  private static Label LABEL_DEFAULT_TITLE;

  private Label title = LABEL_DEFAULT_TITLE;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    setAjax(true);
    tree.registerSubInnerSection(section, id);
  }

  @Override
  protected Label getTitleLabel(RenderContext context) {
    return title;
  }

  public static Label getTitleLabel() {
    return LABEL_DEFAULT_TITLE;
  }

  @Override
  public void showDialog(SectionInfo info) {
    super.showDialog(info);
    if (currentRolesCallback != null) {
      section.setSelections(info, currentRolesCallback.getCurrentSelectedRoles(info));
    } else {
      section.setSelections(info, null);
    }
  }

  @Override
  protected SectionRenderable getRenderableContents(RenderContext context) {
    getModel(context).setInnerContents(renderSection(context, section));

    return viewFactory.createResult("utils/selectroledialog.ftl", this);
  }

  @Override
  public String getWidth() {
    return WIDTH + "px";
  }

  @Override
  protected JSHandler createOkHandler(SectionTree tree) {
    return events.getNamedHandler("returnResults");
  }

  @EventHandlerMethod
  public void returnResults(SectionInfo info) {
    section.addRoles(info);
    final List<SelectedRole> selections = section.getSelections(info);
    Object[] array;
    if (selections != null) {
      array = selections.toArray();
    } else {
      array = new SelectedRole[] {};
    }
    String roles = JSONArray.fromObject(array).toString();

    info.getRootRenderContext()
        .setRenderedBody(
            new CloseWindowResult(jscall(getCloseFunction()), jscall(getOkCallback(), roles)));
  }

  @Override
  public Model instantiateDialogModel(SectionInfo info) {
    return new Model();
  }

  public interface CurrentRolesCallback {
    List<SelectedRole> getCurrentSelectedRoles(SectionInfo info);
  }

  public void setRolesCallback(CurrentRolesCallback rolesCallback) {
    currentRolesCallback = rolesCallback;
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  public static List<SelectedRole> rolesFromJsonString(String rolesJson) {
    return JSONArray.toList(JSONArray.fromObject(rolesJson), SelectedRole.class);
  }

  public static SelectedRole roleFromJsonString(String rolesJson) {
    final List<SelectedRole> roles = rolesFromJsonString(rolesJson);
    if (Check.isEmpty(roles)) {
      return null;
    }
    return roles.get(0);
  }

  @Override
  protected Label getOkLabel() {
    final boolean multiple = section.isMultipleRoles();
    final String okeyDokey =
        (multiple
            ? CoreStrings.key("utils.selectroledialog.selecttheseroles")
            : CoreStrings.key("utils.selectroledialog.selectthisrole"));

    return new KeyLabel(okeyDokey);
  }

  public void setMultipleRoles(boolean b) {
    section.setMultipleRoles(b);
  }

  public static class Model extends DialogModel {
    private SectionRenderable innerContents;

    public SectionRenderable getInnerContents() {
      return innerContents;
    }

    public void setInnerContents(SectionRenderable innerContents) {
      this.innerContents = innerContents;
    }
  }

  public void setTitle(Label title) {
    this.title = title;
  }

  public void setPrompt(Label prompt) {
    section.setPrompt(prompt);
  }
}
