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

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.MultiEditBox;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.JAdminSpinner;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class I18nEditBoxEditor extends AbstractControlEditor<MultiEditBox>
{
	private static final long serialVersionUID = 1L;
	private MultiTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;

	private JCheckBox mandatory;

	private JAdminSpinner rowModel;

	public I18nEditBoxEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		MultiEditBox control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());

		rowModel.set(control.getSize2(), 1);

		WizardHelper.loadSchemaChooser(picker, control);
	}

	@Override
	protected void saveControl()
	{
		MultiEditBox control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setSize2(rowModel.getIntValue());

		WizardHelper.saveSchemaChooser(picker, control);
	}

	private void setupGUI()
	{
		setShowScripting(true);

		picker = WizardHelper.createMultiTargetChooser(this);

		addSection(createDetails());
		addSection(WizardHelper.createMetaData(picker));
	}

	private JComponent createDetails()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description")); //$NON-NLS-1$
		JLabel rowsLabel = new JLabel(CurrentLocale.get("wizard.controls.rowcount")); //$NON-NLS-1$

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); //$NON-NLS-1$
		// Setup the number spinners
		rowModel = new JAdminSpinner(1, 1, 10, 1);

		final int height1 = title.getPreferredSize().height;
		final int width1 = Math.max(descriptionLabel.getPreferredSize().width, rowsLabel.getPreferredSize().width);

		final int[] rows = {height1, height1, height1, height1};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 2, 1));

		all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		all.add(description, new Rectangle(1, 1, 2, 1));

		all.add(rowsLabel, new Rectangle(0, 2, 1, 1));
		all.add(rowModel, new Rectangle(1, 2, 1, 1));

		all.add(mandatory, new Rectangle(0, 3, 3, 1));

		return all;
	}
}
