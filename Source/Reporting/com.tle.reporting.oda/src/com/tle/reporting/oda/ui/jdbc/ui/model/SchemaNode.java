/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Actuate Corporation -
 * initial API and implementation
 *******************************************************************************/

package com.tle.reporting.oda.ui.jdbc.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import com.tle.reporting.oda.ui.TLEOdaPlugin;
import com.tle.reporting.oda.ui.jdbc.ui.provider.JdbcMetaDataProvider;
import com.tle.reporting.oda.ui.jdbc.ui.util.Utility;
import com.tle.reporting.schema.Table;

public class SchemaNode extends ChildrenAllowedNode implements Comparable<SchemaNode>
{
	private static String SCHEMA_ICON = SchemaNode.class.getName() + ".SchemaIcon";
	static
	{
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(SCHEMA_ICON, ImageDescriptor.createFromFile(TLEOdaPlugin.class, "icons/schema.gif"));//$NON-NLS-1$
	}

	private final String schemaName;

	public SchemaNode(String schemaName)
	{
		this.schemaName = schemaName;
	}

	@Override
	protected IDBNode[] refetchChildren(FilterConfig fc)
	{
		String[] tableTypes = fc.getTableTypesForJDBC();
		List<IDBNode> children = new ArrayList<IDBNode>();
		if( tableTypes != null )
		{
			Collection<Table> rs = JdbcMetaDataProvider.getInstance().getFilteredTables(fc.getNamePattern());

			if( rs != null )
			{
				for( Table tableRes : rs )
				{
					TableNode table = new TableNode(schemaName, tableRes.getName(), tableRes.isView());
					children.add(table);
				}
			}
		}
		if( JdbcMetaDataProvider.getInstance().isSupportProcedure()
			&& (fc.getType() == FilterConfig.Type.ALL || fc.getType() == FilterConfig.Type.PROCEDURE) )
		{
			children.add(new ProcedureFlagNode(schemaName));
		}
		return children.toArray(new IDBNode[0]);
	}

	public int compareTo(SchemaNode o)
	{
		return schemaName.compareTo(o.schemaName);
	}

	public String getDisplayName()
	{
		return schemaName;
	}

	public Image getImage()
	{
		return JFaceResources.getImageRegistry().get(SCHEMA_ICON);
	}

	public String getQualifiedNameInSQL(boolean useIdentifierQuoteString, boolean includeSchema)
	{
		String quoteFlag = "";
		if( useIdentifierQuoteString )
		{
			quoteFlag = JdbcMetaDataProvider.getInstance().getIdentifierQuoteString();
		}
		return Utility.quoteString(schemaName, quoteFlag);
	}

}
