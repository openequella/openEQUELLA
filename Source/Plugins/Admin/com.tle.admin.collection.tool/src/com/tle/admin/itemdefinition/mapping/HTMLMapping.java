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
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.common.i18n.CurrentLocale;

public class HTMLMapping extends AbstractTableMapping<com.tle.beans.entity.itemdef.mapping.HTMLMapping>
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("nls")
	private static final String[] DEFAULT_META_NAMES = new String[]{"description", "keywords", "dc.title",
			"dc.creator", "dc.subject", "dc.description", "dc.publisher", "dc.contributor", "dc.date", "dc.type",
			"dc.format", "dc.identifier", "dc.source", "dc.language", "dc.relation", "dc.coverage", "dc.rights",};

	public HTMLMapping(SchemaModel schema)
	{
		super(schema);
	}

	@Override
	public String toString()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.mapping.htmlmapping.title"); //$NON-NLS-1$
	}

	@Override
	protected String[] getColumnNames()
	{
		return new String[]{CurrentLocale.get("com.tle.admin.itemdefinition.mapping.htmlmapping.tagname"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.htmlmapping.schema")}; //$NON-NLS-1$
	}

	@Override
	protected Collection<com.tle.beans.entity.itemdef.mapping.HTMLMapping> getMappings(MetadataMapping mapping)
	{
		return mapping.getHtmlMapping();
	}

	@Override
	protected TableCellEditor getCellEditor(int column)
	{
		TableCellEditor cell = null;
		if( column == 0 )
		{
			JComboBox combo = new JComboBox(DEFAULT_META_NAMES);
			combo.setEditable(true);
			cell = new DefaultCellEditor(combo);
		}
		else
		{
			cell = new SchemaCellEditor(schema);
		}
		return cell;
	}

	@Override
	protected Object getNode(Object data, int column)
	{
		com.tle.beans.entity.itemdef.mapping.HTMLMapping mapping = (com.tle.beans.entity.itemdef.mapping.HTMLMapping) data;
		Object val = null;
		if( column == 0 )
		{
			val = mapping.getHtml();
		}
		else
		{
			val = mapping.getItemdef();
		}
		return val;
	}

	@Override
	protected void setNode(Object data, int column, String value)
	{
		com.tle.beans.entity.itemdef.mapping.HTMLMapping mapping = (com.tle.beans.entity.itemdef.mapping.HTMLMapping) data;
		if( column == 0 )
		{
			mapping.setHtml(value);
		}
		else
		{
			mapping.setItemdef(value);
		}
	}

	@Override
	protected com.tle.beans.entity.itemdef.mapping.HTMLMapping newMapping()
	{
		return new com.tle.beans.entity.itemdef.mapping.HTMLMapping();
	}
}
