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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.RemoveChildTargets;
import com.dytech.edge.wizard.beans.control.Repeater;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.admin.schema.TargetListener;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class RepeaterEditor extends AbstractControlEditor<Repeater> implements ChangeListener, TargetListener
{
	private static final long serialVersionUID = 1L;
	private SingleTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private I18nTextField noun;
	private SpinnerNumberModel minModel;
	private SpinnerNumberModel maxModel;

	public RepeaterEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		Repeater control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		noun.load(control.getNoun());

		WizardHelper.loadSchemaChooser(picker, control);

		minModel.setValue(control.getMin());
		maxModel.setValue(control.getMax());
	}

	@Override
	protected void saveControl()
	{
		Repeater control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setNoun(noun.save());

		control.setMin(minModel.getNumber().intValue());
		control.setMax(maxModel.getNumber().intValue());

		// Get the targets
		WizardHelper.saveSchemaChooser(picker, control);
	}

	private void setupGUI()
	{
		if( getWizardType() == WizardHelper.WIZARD_TYPE_CONTRIBUTION )
		{
			setShowScripting(true);
		}

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
		JLabel nounLabel = new JLabel(CurrentLocale.get("wizard.controls.repeatnoun")); //$NON-NLS-1$
		JLabel minLabel = new JLabel(CurrentLocale.get("wizard.controls.minoccurrences")); //$NON-NLS-1$
		JLabel maxLabel = new JLabel(CurrentLocale.get("wizard.controls.maxoccurrences")); //$NON-NLS-1$

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		noun = new I18nTextField(BundleCache.getLanguages());

		// Setup the number spinners
		minModel = new SpinnerNumberModel(1, 0, 50, 1);
		maxModel = new SpinnerNumberModel(10, 0, 500, 1);

		minModel.addChangeListener(this);
		maxModel.addChangeListener(this);

		final int height1 = title.getPreferredSize().height;
		final int width1 = maxLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, height1, height1,};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 2, 1));

		all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		all.add(description, new Rectangle(1, 1, 2, 1));

		all.add(nounLabel, new Rectangle(0, 2, 1, 1));
		all.add(noun, new Rectangle(1, 2, 2, 1));

		all.add(minLabel, new Rectangle(0, 3, 1, 1));
		all.add(new JSpinner(minModel), new Rectangle(1, 3, 1, 1));

		all.add(maxLabel, new Rectangle(0, 4, 1, 1));
		all.add(new JSpinner(maxModel), new Rectangle(1, 4, 1, 1));

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

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(ChangeEvent e)
	{
		Integer min = (Integer) minModel.getValue();
		Integer max = (Integer) maxModel.getNumber();

		if( e.getSource() == minModel )
		{
			if( min.compareTo(max) > 0 )
			{
				if( min.compareTo((Integer) maxModel.getMaximum()) <= 0 )
				{
					maxModel.setValue(min);
				}
				else
				{
					minModel.setValue(max);
				}
			}
		}
		else if( e.getSource() == maxModel && max.compareTo(min) < 0 )
		{
			if( max.compareTo((Integer) minModel.getMinimum()) >= 0 )
			{
				minModel.setValue(max);
			}
			else
			{
				maxModel.setValue(min);
			}
		}
	}
}
