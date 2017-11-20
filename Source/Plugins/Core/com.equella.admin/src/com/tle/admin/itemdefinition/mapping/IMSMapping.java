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

package com.tle.admin.itemdefinition.mapping;

import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.common.i18n.CurrentLocale;

public class IMSMapping extends AbstractTableMapping<com.tle.beans.entity.itemdef.mapping.IMSMapping>
{
	private static final long serialVersionUID = 1L;
	private static final String TOP_NODE_NAME = "imspackage"; //$NON-NLS-1$
	private static final String[] COLUMN_NODE_NAMES = new String[]{"ims", "itemdef", "@type", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"@replace"}; //$NON-NLS-1$
	private static final BiMap<String, String> TYPE_MAP = HashBiMap.create();

	static
	{
		TYPE_MAP.put("simple", "Simple"); //$NON-NLS-1$
		TYPE_MAP.put("repeat", CurrentLocale //$NON-NLS-1$
			.get("com.tle.admin.itemdefinition.mapping.imsmapping.repeating")); //$NON-NLS-1$
		TYPE_MAP.put("compound", "Compound"); //$NON-NLS-1$
	}

	public IMSMapping(SchemaModel tree)
	{
		super(tree);
	}

	@Override
	public String toString()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.title"); //$NON-NLS-1$
	}

	@Override
	public void add()
	{
		model.addRow(new Object[]{"", "", //$NON-NLS-1$ //$NON-NLS-2$
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.simple"), //$NON-NLS-1$
				Boolean.FALSE});
	}

	@Override
	protected String[] getColumnNames()
	{
		return new String[]{CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.package"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.schema"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.type"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.replace")}; //$NON-NLS-1$
	}

	protected String getTopNodeName()
	{
		return TOP_NODE_NAME;
	}

	protected String getNodeName(int column)
	{
		return COLUMN_NODE_NAMES[column];
	}

	@Override
	protected void processTableColumn(TableColumn column, int col)
	{
		super.processTableColumn(column, col);
		if( col == 2 )
		{
			column.setPreferredWidth(50);
		}
		else if( col == 3 )
		{
			column.setPreferredWidth(40);
		}
		else
		{
			column.setPreferredWidth(200);
		}

	}

	@Override
	protected TableCellEditor getCellEditor(int column)
	{
		TableCellEditor cell = null;
		if( column == 0 )
		{
			cell = new DefaultCellEditor(new JTextField());
		}
		else if( column == 1 )
		{
			SchemaCellEditor scell = new SchemaCellEditor(schema);
			cell = scell;
			scell.addCellEditedListener(new SchemaCellEditor.CellEditedListener()
			{
				@Override
				public void edited(SchemaCellEditor editor, int row, int column2)
				{
					boolean non = false;
					if( column2 == 1 )
					{
						Object value = TYPE_MAP.inverse().get(model.getValueAt(row, 2));
						non = value != null && value.toString().equals("compound");
					}
					editor.setNonLeafSelection(non);
				}
			});
		}
		else if( column == 2 )
		{
			cell = new DefaultCellEditor(new JComboBox(new String[]{
					CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.simple"), //$NON-NLS-1$
					CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.repeating"), //$NON-NLS-1$
					CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.compound")})); //$NON-NLS-1$
		}
		else if( column == 3 )
		{
			cell = new DefaultCellEditor(new JCheckBox());
		}
		return cell;
	}

	@Override
	protected Collection<com.tle.beans.entity.itemdef.mapping.IMSMapping> getMappings(MetadataMapping mapping)
	{
		return mapping.getImsMapping();
	}

	@Override
	protected Object getNode(Object data, int column)
	{
		com.tle.beans.entity.itemdef.mapping.IMSMapping mapping = (com.tle.beans.entity.itemdef.mapping.IMSMapping) data;
		Object val = null;
		if( column == 0 )
		{
			val = mapping.getIms();
		}
		else if( column == 1 )
		{
			val = mapping.getItemdef();
		}
		else if( column == 2 )
		{
			if( mapping.isRepeat() )
			{
				val = CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.repeating"); //$NON-NLS-1$
			}
			else
			{
				val = TYPE_MAP.get(mapping.getType());
			}
			if( val == null )
			{
				val = CurrentLocale.get("com.tle.admin.itemdefinition.mapping.imsmapping.simple"); //$NON-NLS-1$
			}
		}
		else if( column == 3 )
		{
			val = mapping.isReplace();
		}
		return val;
	}

	@Override
	protected void setNode(Object data, int column, String value)
	{
		com.tle.beans.entity.itemdef.mapping.IMSMapping mapping = (com.tle.beans.entity.itemdef.mapping.IMSMapping) data;
		if( column == 0 )
		{
			mapping.setIms(value);
		}
		else if( column == 1 )
		{
			mapping.setItemdef(value);
		}
		else if( column == 2 )
		{
			Object o = TYPE_MAP.inverse().get(value);
			if( o != null )
			{
				mapping.setType(o.toString());
			}
		}
		else if( column == 3 )
		{
			mapping.setReplace(Boolean.valueOf(value));
		}
	}

	@Override
	protected com.tle.beans.entity.itemdef.mapping.IMSMapping newMapping()
	{
		return new com.tle.beans.entity.itemdef.mapping.IMSMapping();
	}
}
