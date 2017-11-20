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
import javax.swing.JTextField;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.GroupItem;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class GroupItemEditor extends AbstractControlEditor<GroupItem>
{
	private static final long serialVersionUID = 1L;
	private I18nTextField name;
	private JTextField value;

	public GroupItemEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		GroupItem control = getWizardControl();

		name.load(control.getName());
		value.setText(control.getValue());
	}

	@Override
	protected void saveControl()
	{
		GroupItem control = getWizardControl();

		control.setName(name.save());
		control.setValue(value.getText());
	}

	protected void setupGUI()
	{
		setShowScripting(true);

		JLabel titleLabel = new JLabel(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.groupitemeditor.title")); //$NON-NLS-1$
		JLabel valueLabel = new JLabel(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.groupitemeditor.value")); //$NON-NLS-1$

		name = new I18nTextField(BundleCache.getLanguages());
		value = new JTextField();

		final int width1 = valueLabel.getPreferredSize().width;
		final int height1 = name.getPreferredSize().height;

		final int[] rows = {height1, height1,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(name, new Rectangle(1, 0, 1, 1));

		all.add(valueLabel, new Rectangle(0, 1, 1, 1));
		all.add(value, new Rectangle(1, 1, 1, 1));

		addSection(all);
	}
}
