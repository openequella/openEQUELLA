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

package com.tle.admin.controls.filemanager;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class FileManagerEditor extends AbstractControlEditor<FileManagerControl>
{
	private static final long serialVersionUID = 6872094930875310377L;

	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;
	private JCheckBox autoMarkAsResources;
	private JCheckBox webdav;

	public FileManagerEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		FileManagerControl control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		autoMarkAsResources.setSelected(control.isAutoMarkAsResource());
		webdav.setSelected(control.isAllowWebDav());
	}

	@Override
	protected void saveControl()
	{
		FileManagerControl control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setAutoMarkAsResource(autoMarkAsResources.isSelected());
		control.setAllowWebDav(webdav.isSelected());
	}

	@SuppressWarnings("nls")
	private void setupGUI()
	{
		setShowScripting(true);

		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title"));
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description"));

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory"));
		autoMarkAsResources = new JCheckBox(getString("autoMarkAsResource"));
		webdav = new JCheckBox(getString("webdav"), true);

		final int height1 = title.getPreferredSize().height;
		final int width1 = descriptionLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, height1, height1,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 1, 1));

		all.add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		all.add(description, new Rectangle(1, 1, 1, 1));

		all.add(mandatory, new Rectangle(0, 2, 2, 1));
		all.add(autoMarkAsResources, new Rectangle(0, 3, 2, 1));
		all.add(webdav, new Rectangle(0, 4, 2, 1));

		addSection(all);

		autoMarkAsResources.addActionListener(autoMarkAndWebDav);
		webdav.addActionListener(autoMarkAndWebDav);
	}

	private final transient ActionListener autoMarkAndWebDav = new ActionListener()
	{
		@SuppressWarnings("nls")
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( autoMarkAsResources.isSelected() && webdav.isSelected() )
			{
				JOptionPane
					.showMessageDialog(autoMarkAsResources,
						getString("conflict"), null,
						JOptionPane.WARNING_MESSAGE);
			}
		}
	};
}
