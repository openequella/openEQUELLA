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

package com.tle.admin.controls.groupselector;

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
import com.tle.common.wizard.controls.groupselector.GroupSelectorControl;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class GroupSelectorControlEditor extends AbstractControlEditor<CustomControl>
{
	private static final long serialVersionUID = 1L;

	private MultiTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;
	private JCheckBox selectMultiple;

	private GroupPanel groupRestriction;

	public GroupSelectorControlEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@Override
	public void init()
	{
		setShowScripting(true);

		addSection(createDetailsSection());

		groupRestriction = new GroupPanel(getString("restrict.groups"), getString("groups.select"),
			getString("groups.selected"));

		addSection(groupRestriction);

		picker = WizardHelper.createMultiTargetChooser(this);
		addSection(WizardHelper.createMetaData(picker));
	}

	@Override
	protected void loadControl()
	{
		final GroupSelectorControl control = (GroupSelectorControl) getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		selectMultiple.setSelected(control.isSelectMultiple());
		groupRestriction.loadControl(control);

		WizardHelper.loadSchemaChooser(picker, control);
	}

	@Override
	protected void saveControl()
	{
		final GroupSelectorControl control = (GroupSelectorControl) getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setSelectMultiple(selectMultiple.isSelected());
		groupRestriction.saveControl(control);

		WizardHelper.saveSchemaChooser(picker, control);
	}

	private JComponent createDetailsSection()
	{
		final JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$
		final JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description")); //$NON-NLS-1$

		final Set<Locale> langs = BundleCache.getLanguages();
		title = new I18nTextField(langs);
		description = new I18nTextField(langs);
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); //$NON-NLS-1$
		selectMultiple = new JCheckBox(getString("selectmultiple")); //$NON-NLS-1$

		final JPanel all = new JPanel(new MigLayout("wrap", "[][grow, fill]"));

		all.add(titleLabel);
		all.add(title);

		all.add(descriptionLabel);
		all.add(description);

		all.add(mandatory, "span 2");

		all.add(selectMultiple, "span 2");

		return all;
	}

	protected String getString(String partKey)
	{
		return CurrentLocale.get("com.tle.admin.controls.groupselector." + partKey); //$NON-NLS-1$
	}

	private final class GroupPanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final FilteredShuffleList<NameValue> groupList;
		private final JLabel groupLabel;

		public GroupPanel(String text, String groupText, String restrictGroupText)
		{
			super(text, false);

			groupLabel = new JLabel(groupText);
			groupList = new FilteredShuffleList<NameValue>(restrictGroupText, new FilterGroupModel(getClientService()
				.getService(RemoteUserService.class)));

			setInnerLayout(new MigLayout("wrap, insets 0", "[][grow, fill]"));
			addInner(groupLabel, "span 2");
			addInner(groupList, "span 2, growx, pushx");

			setSelected(false);
		}

		public void loadControl(GroupSelectorControl control)
		{
			if( control.isRestricted(GroupSelectorControl.KEY_RESTRICT_GROUPS) )
			{
				setSelected(true);
				Set<String> groupUuids = control.getRestrictedTo(GroupSelectorControl.KEY_RESTRICT_GROUPS);
				if( groupUuids.size() > 0 )
				{
					RemoteUserService userService = Driver.instance().getClientService()
						.getService(RemoteUserService.class);
					Collection<NameValue> groups = new ArrayList<NameValue>();
					for( String groupUuid : groupUuids )
					{
						groups.add(UserBeanUtils.getGroup(userService, groupUuid));
					}
					addGroups(groups);
				}
			}
			else
			{
				setSelected(false);
			}
		}

		public void saveControl(GroupSelectorControl control)
		{
			control.setRestricted(GroupSelectorControl.KEY_RESTRICT_GROUPS, isSelected());
			Set<String> restrictedTo = control.getRestrictedTo(GroupSelectorControl.KEY_RESTRICT_GROUPS);
			restrictedTo.clear();
			if( isSelected() )
			{
				restrictedTo.addAll(getSelectedGroupUuids());
			}
		}

		protected List<String> getSelectedGroupUuids()
		{
			List<NameValue> selected = groupList.getItems();
			List<String> selectedUuids = new ArrayList<String>(selected.size());
			for( NameValue nameVal : selected )
			{
				selectedUuids.add(nameVal.getValue());
			}
			return selectedUuids;
		}

		protected void addGroups(Collection<NameValue> groups)
		{
			groupList.removeAllItems();
			groupList.addItems(groups);
		}
	}
}