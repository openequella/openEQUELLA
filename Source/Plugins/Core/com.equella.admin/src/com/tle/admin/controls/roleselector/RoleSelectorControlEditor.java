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

package com.tle.admin.controls.roleselector;

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
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.roleselector.RoleSelectorControl;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class RoleSelectorControlEditor extends AbstractControlEditor<CustomControl>
{
	private static final long serialVersionUID = 1L;

	private MultiTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;
	private JCheckBox selectMultiple;

	public RoleSelectorControlEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@Override
	public void init()
	{
		setShowScripting(true);

		addSection(createDetailsSection());

		picker = WizardHelper.createMultiTargetChooser(this);
		addSection(WizardHelper.createMetaData(picker));
	}

	@Override
	protected void loadControl()
	{
		final RoleSelectorControl control = (RoleSelectorControl) getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		selectMultiple.setSelected(control.isSelectMultiple());

		WizardHelper.loadSchemaChooser(picker, control);
	}

	@Override
	protected void saveControl()
	{
		final RoleSelectorControl control = (RoleSelectorControl) getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setSelectMultiple(selectMultiple.isSelected());

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
		selectMultiple = new JCheckBox(getString("rolesel.selectmultiple")); //$NON-NLS-1$

		final JPanel all = new JPanel(new MigLayout("wrap", "[][grow, fill]"));

		all.add(titleLabel);
		all.add(title);

		all.add(descriptionLabel);
		all.add(description);

		all.add(mandatory, "span 2");

		all.add(selectMultiple, "span 2");

		return all;
	}
}
