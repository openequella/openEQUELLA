/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Actuate Corporation -
 * initial API and implementation
 *******************************************************************************/
package com.tle.reporting.oda.ui.jdbc.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import com.tle.reporting.oda.ui.TLEOdaPlugin;
import com.tle.reporting.oda.ui.jdbc.ui.provider.JdbcMetaDataProvider;
import com.tle.reporting.oda.ui.jdbc.ui.util.Utility;
import com.tle.reporting.schema.Column;

public class TableNode extends ChildrenAllowedNode implements Comparable<TableNode>
{
	private static String TABLE_ICON = TableNode.class.getName() + ".TableIcon";
	private static String VIEW_ICON = TableNode.class.getName() + ".ViewIcon";
	static
	{
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(TABLE_ICON, ImageDescriptor.createFromFile(TLEOdaPlugin.class, "icons/table.gif"));//$NON-NLS-1$
		reg.put(VIEW_ICON, ImageDescriptor.createFromFile(TLEOdaPlugin.class, "icons/view.gif"));//$NON-NLS-1$
	}

	private final String schemaName;
	private final String tableName;
	private final boolean isView;

	public TableNode(String schemaName, String tableName, boolean isView)
	{
		assert tableName != null;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.isView = isView;
	}

	public String getDisplayName()
	{
		return tableName;
	}

	public Image getImage()
	{
		return isView ? JFaceResources.getImage(VIEW_ICON) : JFaceResources.getImage(TABLE_ICON);
	}

	public int compareTo(TableNode o)
	{
		/**
		 * In our case, 2 <code>TableNode</code> instances need to be compared
		 * <p>
		 * only when they belong to the same schema
		 */
		return this.tableName.compareTo(o.tableName);
	}

	public String getQualifiedNameInSQL(boolean useIdentifierQuoteString, boolean includeSchema)
	{
		StringBuffer sb = new StringBuffer();
		String quoteFlag = "";
		if( useIdentifierQuoteString )
		{
			quoteFlag = JdbcMetaDataProvider.getInstance().getIdentifierQuoteString();
		}
		if( includeSchema && schemaName != null )
		{
			sb.append(Utility.quoteString(schemaName, quoteFlag)).append(".");
		}
		sb.append(Utility.quoteString(tableName, quoteFlag));
		return sb.toString();
	}

	@Override
	protected IDBNode[] refetchChildren(FilterConfig fc)
	{
		List<TableColumnNode> columns = new ArrayList<TableColumnNode>();
		List<Column> rs = JdbcMetaDataProvider.getInstance().getTableColumns(tableName);
		if( rs != null )
		{
			for( Column col : rs )
			{
				TableColumnNode column = new TableColumnNode(schemaName, tableName, col.getName(), col.getType());
				columns.add(column);
			}
		}
		return columns.toArray(new TableColumnNode[0]);
	}
}
