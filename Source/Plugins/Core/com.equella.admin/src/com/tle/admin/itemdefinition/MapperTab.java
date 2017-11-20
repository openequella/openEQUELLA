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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dytech.gui.TableLayout;
import com.tle.admin.itemdefinition.mapping.HTMLMapping;
import com.tle.admin.itemdefinition.mapping.IMSMapping;
import com.tle.admin.itemdefinition.mapping.Mapping;
import com.tle.admin.itemdefinition.mapping.ScriptedLiteralMapping;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.common.i18n.CurrentLocale;

public class MapperTab extends AbstractItemdefTab implements ItemListener
{
	public static final int DEFAULT_DISPLAY = 0;

	private JComboBox mappingBox;
	private JPanel middlePanel;

	@Override
	public void init(Component parent)
	{
		setupTab();
		updateButtons();
	}

	@Override
	public void validation()
	{
		// No validation to be done here
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.mappertab.title"); //$NON-NLS-1$
	}

	@Override
	public void save()
	{
		final ItemDefinition itemdef = state.getEntity();
		MetadataMapping mm = itemdef.getMetadataMapping();
		if( mm == null )
		{
			mm = new MetadataMapping();
			itemdef.setMetadataMapping(mm);
		}

		int count = mappingBox.getItemCount();
		for( int i = 0; i < count; i++ )
		{
			Mapping mapping = (Mapping) mappingBox.getItemAt(i);
			mapping.save(mm);
		}
	}

	@Override
	public void load()
	{
		MetadataMapping mm = state.getEntity().getMetadataMapping();

		int count = mappingBox.getItemCount();
		for( int i = 0; i < count; i++ )
		{
			Mapping mapping = (Mapping) mappingBox.getItemAt(i);
			mapping.loadItem(mm);
		}
	}

	// // SETUP SWING COMPONENTS
	// ////////////////////////////////////////////////

	protected void setupTab()
	{
		JComponent top = createTop();
		middlePanel = new JPanel(new GridLayout(1, 1));

		final int[] rows = new int[]{top.getPreferredSize().height, TableLayout.FILL};
		final int[] cols = new int[]{TableLayout.TRIPLE_FILL, TableLayout.FILL};

		setLayout(new TableLayout(rows, cols, 5, 5));

		add(top, new Rectangle(0, 0, 1, 1));
		add(middlePanel, new Rectangle(0, 1, 1, 1));
	}

	private JComponent createTop()
	{
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mappingBox = new JComboBox();
		mappingBox.addItem(new IMSMapping(schema));
		mappingBox.addItem(new HTMLMapping(schema));
		mappingBox.addItem(new ScriptedLiteralMapping(schema));
		mappingBox.addItemListener(this);
		panel1.add(mappingBox);
		return panel1;
	}

	private void updateButtons()
	{
		Mapping mapping = (Mapping) mappingBox.getSelectedItem();
		JComponent component = mapping.getComponent();
		if( middlePanel.getComponentCount() == 0 || middlePanel.getComponent(0) != component )
		{
			middlePanel.removeAll();
			middlePanel.add(component, new Rectangle(0, 0, 1, 1));
			middlePanel.updateUI();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		updateButtons();
	}
}
