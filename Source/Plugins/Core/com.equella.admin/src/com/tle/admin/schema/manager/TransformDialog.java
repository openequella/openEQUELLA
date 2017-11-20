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

package com.tle.admin.schema.manager;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.dytech.gui.file.FileFilterAdapter;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.FileSelector;
import com.tle.beans.entity.SchemaTransform;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

public class TransformDialog implements ActionListener
{
	private JComboBox schemaType;
	private FileSelector fileSelector;
	private JButton ok;
	private JPanel all;
	private JDialog dialog;
	private boolean okSelected = false;

	public TransformDialog()
	{
		setupGui();
		setupSchemaTypes();
	}

	private void setupGui()
	{
		JLabel typeLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.transformdialog.name")); //$NON-NLS-1$
		JLabel fileLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.transformdialog.xsl")); //$NON-NLS-1$

		schemaType = new JComboBox();
		schemaType.setEditable(true);
		fileSelector = new FileSelector(CurrentLocale.get("com.tle.admin.schema.manager.transformdialog.browse")); //$NON-NLS-1$
		fileSelector.setFileFilter(FileFilterAdapter.XSLT());

		ok = new JButton(CurrentLocale.get("com.tle.admin.ok")); //$NON-NLS-1$
		JButton cancel = new JButton(CurrentLocale.get("com.tle.admin.cancel")); //$NON-NLS-1$

		ok.addActionListener(this);
		cancel.addActionListener(this);

		final int height1 = typeLabel.getPreferredSize().height;
		final int height2 = schemaType.getPreferredSize().height;
		final int height3 = fileSelector.getPreferredSize().height;
		final int height4 = ok.getPreferredSize().height;
		final int width1 = cancel.getPreferredSize().width;

		final int[] rows = {height1, height2, height1, height3, height4,};
		final int[] cols = {TableLayout.FILL, width1, width1,};

		all = new JPanel(new TableLayout(rows, cols));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(typeLabel, new Rectangle(0, 0, 3, 1));
		all.add(schemaType, new Rectangle(0, 1, 3, 1));
		all.add(fileLabel, new Rectangle(0, 2, 3, 1));
		all.add(fileSelector, new Rectangle(0, 3, 3, 1));
		all.add(ok, new Rectangle(1, 4, 1, 1));
		all.add(cancel, new Rectangle(2, 4, 1, 1));
	}

	private void setupSchemaTypes()
	{
		schemaType.addItem("OAI_DC"); //$NON-NLS-1$
		schemaType.addItem("OAI_LOM"); //$NON-NLS-1$
		schemaType.addItem("HARVESTER"); //$NON-NLS-1$
	}

	public boolean showEditDialog(Component parent, SchemaTransform transform)
	{
		if( transform != null )
		{
			schemaType.setSelectedItem(transform.getType());
		}

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(CurrentLocale.get("com.tle.admin.schema.manager.transformdialog.schema")); //$NON-NLS-1$
		dialog.setContentPane(all);
		dialog.setResizable(false);
		dialog.setModal(true);
		dialog.pack();

		dialog.setSize(500, dialog.getHeight());

		ComponentHelper.centreOnScreen(dialog);
		dialog.setVisible(true);

		return okSelected;
	}

	public File getSelectedFile()
	{
		return fileSelector.getSelectedFile();
	}

	public String getSchemaType()
	{
		return schemaType.getSelectedItem().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		okSelected = e.getSource() == ok;
		if( okSelected )
		{
			if( Check.isEmpty(getSchemaType()) )
			{
				Driver.displayInformation(dialog,
					CurrentLocale.get("com.tle.admin.schema.manager.transformdialog.chars")); //$NON-NLS-1$
			}
			else if( getSelectedFile() == null || !getSelectedFile().exists() )
			{
				Driver.displayInformation(dialog,
					CurrentLocale.get("com.tle.admin.schema.manager.transformdialog.select")); //$NON-NLS-1$
			}
			else
			{
				dialog.dispose();
			}
		}
		else
		{
			dialog.dispose();
		}
	}
}
