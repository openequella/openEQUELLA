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

package com.dytech.edge.admin.wizard.editor;

import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.ReloadHandler;
import com.dytech.edge.admin.wizard.TripleShuffleList;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.ListBox;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class ListBoxEditor extends AbstractPowerSearchControlEditor<ListBox>
{
	private static final long serialVersionUID = 1L;

	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox reload;
	private JCheckBox mandatory;

	private MultiTargetChooser picker;
	private TripleShuffleList choices;

	public ListBoxEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@Override
	protected void loadControl()
	{
		ListBox control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		reload.setSelected(control.isReload());
		mandatory.setSelected(control.isMandatory());
		choices.setItems(control.getItems());

		WizardHelper.loadSchemaChooser(picker, control);

		super.loadControl();
	}

	@Override
	protected void saveControl()
	{
		ListBox control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setReload(reload.isSelected());
		control.setMandatory(mandatory.isSelected());

		control.getItems().clear();
		control.getItems().addAll(choices.getItems());

		WizardHelper.saveSchemaChooser(picker, control);

		super.saveControl();
	}

	@Override
	protected void setupGUI()
	{
		setShowScripting(true);

		picker = WizardHelper.createMultiTargetChooser(this);
		choices = new TripleShuffleList(
			CurrentLocale.get("com.dytech.edge.admin.wizard.editor.listboxeditor.optionname"), CurrentLocale //$NON-NLS-1$
				.get("com.dytech.edge.admin.wizard.editor.listboxeditor.optionvalue")); //$NON-NLS-1$
		choices.setSingleThirdColumnSelection(true);

		addSection(createDetails());
		addSection(WizardHelper.createMetaData(picker));
		addSection(WizardHelper.createItems(choices,
			CurrentLocale.get("com.dytech.edge.admin.wizard.editor.listboxeditor.add"))); //$NON-NLS-1$

		super.setupGUI();
	}

	private JComponent createDetails()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description")); //$NON-NLS-1$

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); //$NON-NLS-1$
		reload = new JCheckBox(CurrentLocale.get("wizard.controls.reload")); //$NON-NLS-1$

		reload.addActionListener(new ReloadHandler(reload));

		final int height1 = title.getPreferredSize().height;
		final int width1 = descriptionLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, height1};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 2, 1));

		all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		all.add(description, new Rectangle(1, 1, 2, 1));

		all.add(mandatory, new Rectangle(0, 2, 3, 1));

		Control parent = getControl().getParent();
		String classId = parent.getControlClass();
		if( !classId.equals("multi") ) //$NON-NLS-1$
		{
			all.add(reload, new Rectangle(0, 3, 3, 1));
		}

		return all;
	}
}
