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

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

/**
 * @author Aaron
 */
public abstract class AbstractPowerSearchControlEditor<T extends WizardControl> extends AbstractControlEditor<T>
{
	private static final long serialVersionUID = 1L;

	private PowerSection powerSection;

	public AbstractPowerSearchControlEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	protected void setupGUI()
	{
		if( getWizardType() == WizardHelper.WIZARD_TYPE_POWERSEARCH )
		{
			powerSection = new PowerSection();
			addSection(powerSection);
		}
	}

	@Override
	protected void loadControl()
	{
		if( powerSection != null )
		{
			powerSection.loadControl();
		}
	}

	@Override
	protected void saveControl()
	{
		if( powerSection != null )
		{
			powerSection.saveControl();
		}
	}

	protected class PowerSection extends JPanel
	{
		private static final long serialVersionUID = 1L;

		private I18nTextField friendlyName;

		public PowerSection()
		{
			setupGUI();
		}

		protected void setupGUI()
		{
			friendlyName = new I18nTextField(BundleCache.getLanguages());

			JLabel titleLabel = new JLabel(CurrentLocale.get("com.tle.admin.controls.powercontrol.label.sectiontitle")); //$NON-NLS-1$
			JLabel friendlyNameLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.controls.powercontrol.label.friendly")); //$NON-NLS-1$

			int rowHeight = friendlyName.getPreferredSize().height;
			int width1 = friendlyNameLabel.getPreferredSize().width;

			int[] rows = new int[]{titleLabel.getPreferredSize().height, rowHeight};
			int[] columns = new int[]{width1, TableLayout.FILL};
			setLayout(new TableLayout(rows, columns, 5, 5));

			add(titleLabel, new Rectangle(0, 0, 2, 1));
			add(friendlyNameLabel, new Rectangle(0, 1, 1, 1));
			add(friendlyName, new Rectangle(1, 1, 1, 1));
		}

		protected void loadControl()
		{
			T control = getWizardControl();
			friendlyName.load(control.getPowerSearchFriendlyName());
		}

		protected void saveControl()
		{
			T control = getWizardControl();
			control.setPowerSearchFriendlyName(friendlyName.save());
		}
	}
}
