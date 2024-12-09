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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
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
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.servlet.ItemServlet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.sf.json.JSONArray;

/**
 * Note: the okCallback is event handler of signature: handler(SectionInfo info, String usersJson)
 * where usersJson is an array of SelectUserDialog.SelectedUser objects in JSON format. Use
 * SelectUserDialog.userFromJsonString(String) or SelectUserDialog.usersFromJsonString(String) to
 * deserialise the string.
 *
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class SelectUserDialog extends AbstractOkayableDialog<SelectUserDialog.Model> {
  static {
    PluginResourceHandler.init(SelectUserDialog.class);
  }

  private static final int WIDTH = 550;

  private CurrentUsersCallback currentUsersCallback;
  private String permission = SecurityConstants.LIST_USERS;
  private boolean checkOnItem = false;

  @Inject protected SelectUserSection section;
  @Inject protected TLEAclManager securityManager;

  @ViewFactory private FreemarkerFactory viewFactory;

  @PlugKey("utils.selectuserdialog.default.title")
  private static Label LABEL_DEFAULT_TITLE;

  @PlugKey("utils.selectuserdialog.selecttheseusers")
  private static String KEY_MULTIPLE_USERS;

  @PlugKey("utils.selectuserdialog.selectthisuser")
  private static String KEY_SINGLE_USER;

  @PlugKey("editor.error.accessdenied")
  private static String NO_PERMISSIONS;

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
    if (currentUsersCallback != null) {
      section.setSelections(info, currentUsersCallback.getCurrentSelectedUsers(info));
    } else {
      section.setSelections(info, null);
    }
  }

  @Override
  protected SectionRenderable getRenderableContents(RenderContext context) {
    if (canView(context)) {
      getModel(context).setInnerContents(renderSection(context, section));
      return viewFactory.createResult("utils/selectuserdialog.ftl", this);
    } else {
      throw new AccessDeniedException(CurrentLocale.get(NO_PERMISSIONS, permission));
    }
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
    section.addUsers(info);
    final List<SelectedUser> selections = section.getSelections(info);
    Object[] array;
    if (selections != null) {
      array = selections.toArray();
    } else {
      array = new SelectedUser[] {};
    }
    String users = JSONArray.fromObject(array).toString();

    info.getRootRenderContext()
        .setRenderedBody(
            new CloseWindowResult(jscall(getCloseFunction()), jscall(getOkCallback(), users)));
  }

  @Override
  public Model instantiateDialogModel(@Nullable SectionInfo info) {
    return new Model();
  }

  public interface CurrentUsersCallback {
    List<SelectedUser> getCurrentSelectedUsers(SectionInfo info);
  }

  public void setUsersCallback(CurrentUsersCallback usersCallback) {
    currentUsersCallback = usersCallback;
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  public static List<SelectedUser> usersFromJsonString(String usersJson) {
    return JSONArray.toList(JSONArray.fromObject(usersJson), SelectedUser.class);
  }

  @Nullable
  public static SelectedUser userFromJsonString(String usersJson) {
    final List<SelectedUser> users = usersFromJsonString(usersJson);
    if (Check.isEmpty(users)) {
      return null;
    }
    return users.get(0);
  }

  @Override
  protected Label getOkLabel() {
    final boolean multiple = section.isMultipleUsers();
    final String okeyDokey = (multiple ? KEY_MULTIPLE_USERS : KEY_SINGLE_USER);

    return new KeyLabel(okeyDokey);
  }

  public void setMultipleUsers(boolean b) {
    section.setMultipleUsers(b);
  }

  public void setGroupFilter(Set<String> filter) {
    section.setGroupFilter(filter);
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

  /**
   * Allows checking permissions before rendering the dialog.
   *
   * @param permission The ACL string to check against. Defaults to LIST_USERS.
   * @param checkOnItem If true, the ACL will be checked against an item, if false it will be
   *     checked against the user. If checkOnItem is true, the request for this dialog MUST be an
   *     item summary URL. If not, it will trigger an IllegalArgumentException when checking the
   *     current viewable item.
   */
  public void setCheckPermissionBeforeOpen(String permission, boolean checkOnItem) {
    this.permission = permission;
    this.checkOnItem = checkOnItem;
  }

  private boolean canView(RenderContext context) {
    if (permission == null) {
      throw new IllegalStateException("Dialog permission should not be null");
    }
    if (checkOnItem) {
      // Check the ACL against the current item
      ViewableItem<Item> item = context.getAttribute(ItemServlet.VIEWABLE_ITEM);
      if (item == null) {
        throw new IllegalStateException(
            "Access control has been set to check against item, however 'item' is currently"
                + " 'null'.");
      }
      return !(securityManager.filterNonGrantedPrivileges(item.getItem(), permission).isEmpty());
    }
    // if a permission is set but we don't need to check it against an item
    return !(securityManager.filterNonGrantedPrivileges(permission).isEmpty());
  }
}
