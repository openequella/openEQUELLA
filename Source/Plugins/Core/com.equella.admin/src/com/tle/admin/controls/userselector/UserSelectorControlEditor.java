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

package com.tle.admin.controls.userselector;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.dytech.gui.TableLayout;
import com.dytech.gui.filter.FilteredShuffleList;
import com.tle.admin.Driver;
import com.tle.admin.common.FilterGroupModel;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.JGroup;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.util.UserBeanUtils;
import com.tle.common.wizard.controls.userselector.UserSelectorControl;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class UserSelectorControlEditor extends AbstractControlEditor<CustomControl> {
  private static final long serialVersionUID = 1L;

  private MultiTargetChooser picker;
  private I18nTextField title;
  private I18nTextField description;
  private JCheckBox mandatory;
  private JCheckBox selectMultiple;

  private UserGroupPanel groupRestriction;

  public UserSelectorControlEditor(
      final Control control, final int wizardType, final SchemaModel schema) {
    super(control, wizardType, schema);
  }

  @Override
  public void init() {
    setShowScripting(true);

    addSection(createDetailsSection());

    groupRestriction =
        new UserGroupPanel(
            getString("usersel.restrict.groups"), // $NON-NLS-1$
            getString("groups.select"),
            getString("groups.selected")); // $NON-NLS-1$//$NON-NLS-2$

    addSection(groupRestriction);

    picker = WizardHelper.createMultiTargetChooser(this);
    addSection(WizardHelper.createMetaData(picker));
  }

  @Override
  protected void loadControl() {
    final UserSelectorControl control = (UserSelectorControl) getWizardControl();

    title.load(control.getTitle());
    description.load(control.getDescription());
    mandatory.setSelected(control.isMandatory());
    selectMultiple.setSelected(control.isSelectMultiple());
    groupRestriction.loadControl(control);

    WizardHelper.loadSchemaChooser(picker, control);
  }

  @Override
  protected void saveControl() {
    final UserSelectorControl control = (UserSelectorControl) getWizardControl();

    control.setTitle(title.save());
    control.setDescription(description.save());
    control.setMandatory(mandatory.isSelected());
    control.setSelectMultiple(selectMultiple.isSelected());
    groupRestriction.saveControl(control);

    WizardHelper.saveSchemaChooser(picker, control);
  }

  private JComponent createDetailsSection() {
    final JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); // $NON-NLS-1$
    final JLabel descriptionLabel =
        new JLabel(CurrentLocale.get("wizard.controls.description")); // $NON-NLS-1$

    final Set<Locale> langs = BundleCache.getLanguages();
    title = new I18nTextField(langs);
    description = new I18nTextField(langs);
    mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); // $NON-NLS-1$
    selectMultiple = new JCheckBox(getString("usersel.selectmultiple")); // $NON-NLS-1$

    final JPanel all = new JPanel(new MigLayout("wrap", "[][grow, fill]"));

    all.add(titleLabel);
    all.add(title);
    all.add(descriptionLabel);
    all.add(description);
    all.add(mandatory, "span 2");
    all.add(selectMultiple, "span 2");

    return all;
  }

  /** Very much copy and pasted from DRMAccessControlTab TODO: refactor into a common component */
  private final class UserGroupPanel extends JGroup {
    private static final long serialVersionUID = 1L;
    private final FilteredShuffleList<NameValue> groupList;
    private final JLabel groupLabel;

    public UserGroupPanel(String text, String groupText, String restrictGroupText) {
      super(text, false);

      groupLabel = new JLabel(groupText);
      groupList =
          new FilteredShuffleList<NameValue>(
              restrictGroupText,
              new FilterGroupModel(getClientService().getService(RemoteUserService.class)));

      final int height1 = groupLabel.getMinimumSize().height;
      final int height2 = groupList.getMinimumSize().height;
      final int[] rows = {height1, height2};
      final int[] cols = {TableLayout.FILL};

      setInnerLayout(new TableLayout(rows, cols, 5, 5));
      addInner(groupLabel, new Rectangle(0, 0, 1, 1));
      addInner(groupList, new Rectangle(0, 1, 1, 1));

      setSelected(false);
    }

    public void loadControl(UserSelectorControl control) {
      if (control.isRestricted(UserSelectorControl.KEY_RESTRICT_USER_GROUPS)) {
        setSelected(true);
        Set<String> groupUuids =
            control.getRestrictedTo(UserSelectorControl.KEY_RESTRICT_USER_GROUPS);
        if (groupUuids.size() > 0) {
          RemoteUserService userService =
              Driver.instance().getClientService().getService(RemoteUserService.class);
          Collection<NameValue> groups = new ArrayList<NameValue>();
          for (String groupUuid : groupUuids) {
            groups.add(UserBeanUtils.getGroup(userService, groupUuid));
          }
          addGroups(groups);
        }
      } else {
        setSelected(false);
      }
    }

    public void saveControl(UserSelectorControl control) {
      control.setRestricted(UserSelectorControl.KEY_RESTRICT_USER_GROUPS, isSelected());
      Set<String> restrictedTo =
          control.getRestrictedTo(UserSelectorControl.KEY_RESTRICT_USER_GROUPS);
      restrictedTo.clear();
      if (isSelected()) {
        restrictedTo.addAll(getSelectedGroupUuids());
      }
    }

    protected List<String> getSelectedGroupUuids() {
      List<NameValue> selected = groupList.getItems();
      List<String> selectedUuids = new ArrayList<String>(selected.size());
      for (NameValue nameVal : selected) {
        selectedUuids.add(nameVal.getValue());
      }
      return selectedUuids;
    }

    protected void addGroups(Collection<NameValue> groups) {
      groupList.removeAllItems();
      groupList.addItems(groups);
    }
  }
}
