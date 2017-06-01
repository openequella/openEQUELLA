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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.RemoveChildTargets;
import com.dytech.edge.wizard.beans.control.Multi;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.admin.schema.TargetListener;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class MultiEditor extends AbstractControlEditor<Multi> implements TargetListener
{
	private static final long serialVersionUID = 1L;
	private SingleTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;

	/**
	 * Creates a new MultiEditor.
	 */
	public MultiEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		Multi control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());

		WizardHelper.loadSchemaChooser(picker, control);
	}

	@Override
	protected void saveControl()
	{
		Multi control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());

		// Get the targets
		WizardHelper.saveSchemaChooser(picker, control);
	}

	private void setupGUI()
	{
		setShowScripting(true);

		picker = WizardHelper.createSingleTargetChooser(this);
		picker.setNonLeafSelection(true);
		picker.addTargetListener(this);

		addSection(createDetails());
		addSection(WizardHelper.createMetaData(picker));
	}

	private JComponent createDetails()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description")); //$NON-NLS-1$

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); //$NON-NLS-1$

		final int height1 = title.getPreferredSize().height;
		final int width1 = descriptionLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1,};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 2, 1));

		all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		all.add(description, new Rectangle(1, 1, 2, 1));

		all.add(mandatory, new Rectangle(0, 2, 2, 1));

		return all;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.schema.TargetListener#targetAdded(
	 * com.dytech.edge.admin.schema.SchemaNode)
	 */
	@Override
	public void targetAdded(String target)
	{
		setChildTargetBase();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.schema.TargetListener#targetRemoved(
	 * com.dytech.edge.admin.schema.SchemaNode)
	 */
	@Override
	public void targetRemoved(String target)
	{
		// We don't care about this event.
	}

	/**
	 * Removes all the targets from child controls.
	 */
	private void setChildTargetBase()
	{
		RemoveChildTargets walker = new RemoveChildTargets();
		walker.execute(getControl());

		if( walker.hasRemovedTargets() )
		{
			JOptionPane.showMessageDialog(this, CurrentLocale.get("wizard.prompt.removedchildtargets")); //$NON-NLS-1$
		}
	}
}
