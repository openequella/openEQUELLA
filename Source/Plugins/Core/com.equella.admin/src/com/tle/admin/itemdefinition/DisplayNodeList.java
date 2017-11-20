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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.common.i18n.CurrentLocale;

public class DisplayNodeList extends ListWithView<DisplayNode, DisplayNodePanel>
{
	private final String UNTITLED = CurrentLocale.get("com.tle.admin.itemdefinition.abstracttemplatetab.untitlednode"); //$NON-NLS-1$

	private final SchemaModel schemaModel;
	private final boolean extrasVisible;

	public DisplayNodeList(final SchemaModel schemaModel, final boolean extrasVisible)
	{
		super(true);

		this.schemaModel = schemaModel;
		this.extrasVisible = extrasVisible;

		setListCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				String title = CurrentLocale.get(((DisplayNode) value).getTitle(), null);
				setText(title != null ? title : UNTITLED);
				return this;
			}
		});
	}

	@Override
	protected DisplayNode createElement()
	{
		return new DisplayNode();
	}
	
	@Override
	protected boolean listTypeEquals(DisplayNode lhs, DisplayNode rhs)
	{
		return lhs == rhs;
	}

	@Override
	protected ListWithViewInterface<DisplayNode> getEditor(DisplayNode currentSelection)
	{
		return currentSelection == null ? null : new DisplayNodePanel(schemaModel, extrasVisible);
	}
}