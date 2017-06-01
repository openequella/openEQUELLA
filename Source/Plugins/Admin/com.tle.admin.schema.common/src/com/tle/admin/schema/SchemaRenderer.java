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

package com.tle.admin.schema;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("nls")
public class SchemaRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;

	private static final Color TEMP_NAME_COLOUR = new Color(180, 180, 180);
	private static final ImageIcon ROOT_ICON = new ImageIcon(SchemaRenderer.class.getResource("/icons/root.gif"));
	private static final ImageIcon LOCKED_SEARCH_ICON = new ImageIcon(
		SchemaRenderer.class.getResource("/icons/locked_search.gif"));
	private static final ImageIcon LOCKED_NO_SEARCH_ICON = new ImageIcon(
		SchemaRenderer.class.getResource("/icons/locked_no_search.gif"));
	private static final ImageIcon EDITABLE_SEARCH_ICON = new ImageIcon(
		SchemaRenderer.class.getResource("/icons/editable_search.gif"));
	private static final ImageIcon EDITABLE_NO_SEARCH_ICON = new ImageIcon(
		SchemaRenderer.class.getResource("/icons/editable_no_search.gif"));
	private static final ImageIcon SCHEMA_ATTRIBUTE_ICON = new ImageIcon(
		SchemaRenderer.class.getResource("/icons/schema_attribute.gif"));

	private final boolean greyNonIndexed;

	public SchemaRenderer(boolean greyNonIndexed)
	{
		this.greyNonIndexed = greyNonIndexed;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
		boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		SchemaNode node = (SchemaNode) value;

		if( !tree.isEnabled() || (greyNonIndexed && !node.isField()) )
		{
			setForeground(TEMP_NAME_COLOUR);
		}

		setText(node.getName());

		if( node.isRoot() )
		{
			setIcon(ROOT_ICON);
			return this;
		}

		if( node.isAttribute() )
		{
			setIcon(SCHEMA_ATTRIBUTE_ICON);
		}
		else if( node.isLocked() )
		{
			if( node.isSearchable() || node.isField() )
			{
				setIcon(LOCKED_SEARCH_ICON);
			}
			else
			{
				setIcon(LOCKED_NO_SEARCH_ICON);
			}
		}
		else
		{
			if( node.isSearchable() || node.isField() )
			{
				setIcon(EDITABLE_SEARCH_ICON);
			}
			else
			{
				setIcon(EDITABLE_NO_SEARCH_ICON);
			}
		}
		return this;
	}

	@Override
	public void setEnabled(boolean b)
	{
		// Don't let the cell renderer become disabled!
	}
}
