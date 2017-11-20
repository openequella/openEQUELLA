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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.EditBox;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.JAdminSpinner;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class EditBoxEditor extends AbstractPowerSearchControlEditor<EditBox>
{
	private static final long serialVersionUID = 1L;
	private MultiTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private JTextField defaultValue;

	private JCheckBox mandatory;

	private JAdminSpinner rowModel;
	private JCheckBox checkDuplicates;
	private JCheckBox enforceUniqueness;

	public EditBoxEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@Override
	protected void loadControl()
	{
		EditBox control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		checkDuplicates.setSelected(control.isCheckDuplication());
		enforceUniqueness.setSelected(control.isForceUnique());

		rowModel.set(control.getSize2(), 1);

		// Get the default value
		WizardControlItem firstItem = null;
		if( !control.getItems().isEmpty() )
		{
			firstItem = control.getItems().get(0);
			defaultValue.setText(firstItem.getValue());
		}

		WizardHelper.loadSchemaChooser(picker, control);

		super.loadControl();

		updateGui();
	}

	@Override
	protected void saveControl()
	{
		EditBox control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setSize2(rowModel.getIntValue());
		control.setCheckDuplication(checkDuplicates.isSelected());
		control.setForceUnique(enforceUniqueness.isSelected());

		List<WizardControlItem> items = control.getItems();
		items.clear();
		// Get the possible default value
		String dv = defaultValue.getText().trim();
		if( dv.length() > 0 )
		{
			WizardControlItem firstItem = new WizardControlItem();
			firstItem.setValue(dv);
			items.add(firstItem);
		}

		WizardHelper.saveSchemaChooser(picker, control);

		super.saveControl();
	}

	@Override
	protected void setupGUI()
	{
		setShowScripting(true);

		picker = WizardHelper.createMultiTargetChooser(this);

		addSection(createDetails());
		addSection(WizardHelper.createMetaData(picker));

		super.setupGUI();

		updateGui();
	}

	private JComponent createDetails()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description")); //$NON-NLS-1$
		JLabel defaultValueLabel = new JLabel(CurrentLocale.get("wizard.controls.initialvalue")); //$NON-NLS-1$
		JLabel rowsLabel = new JLabel(CurrentLocale.get("wizard.controls.rowcount")); //$NON-NLS-1$

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		defaultValue = new JTextField();
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); //$NON-NLS-1$
		checkDuplicates = new JCheckBox(CurrentLocale.get("wizard.controls.checkDuplicates")); //$NON-NLS-1$
		enforceUniqueness = new JCheckBox(CurrentLocale.get("wizard.controls.enforceUniqueness")); //$NON-NLS-1$

		checkDuplicates.addActionListener(new ActionListener()
		{
			private boolean showWarning = true;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateGui();

				if( checkDuplicates.isSelected() && showWarning )
				{
					Object[] options = new String[]{CurrentLocale.get("wizard.controls.uniqueness.ok"), //$NON-NLS-1$
							CurrentLocale.get("wizard.controls.uniqueness.dontshowagain")}; //$NON-NLS-1$

					if( JOptionPane.showOptionDialog(checkDuplicates,
						CurrentLocale.get("wizard.controls.uniqueness.message"), CurrentLocale //$NON-NLS-1$
							.get("wizard.controls.uniqueness.title"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE, null, options, options[0]) == JOptionPane.NO_OPTION )
					{
						showWarning = false;
					}
				}
			}
		});

		// Setup the number spinners
		rowModel = new JAdminSpinner(1, 1, 10, 1);

		final int height1 = title.getPreferredSize().height;
		final int width1 = rowsLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, height1, height1, height1, height1, TableLayout.PREFERRED,};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 2, 1));

		all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		all.add(description, new Rectangle(1, 1, 2, 1));

		all.add(defaultValueLabel, new Rectangle(0, 2, 1, 1));
		all.add(defaultValue, new Rectangle(1, 2, 2, 1));

		all.add(rowsLabel, new Rectangle(0, 3, 1, 1));
		all.add(rowModel, new Rectangle(1, 3, 1, 1));

		all.add(mandatory, new Rectangle(0, 4, 3, 1));
		all.add(checkDuplicates, new Rectangle(0, 5, 3, 1));
		all.add(enforceUniqueness, new Rectangle(0, 6, 3, 1));

		return all;
	}

	private void updateGui()
	{
		enforceUniqueness.setEnabled(checkDuplicates.isSelected());
		if( !enforceUniqueness.isEnabled() )
		{
			enforceUniqueness.setSelected(false);
		}
	}
}
