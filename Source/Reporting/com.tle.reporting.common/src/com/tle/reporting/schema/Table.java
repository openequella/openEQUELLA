package com.tle.reporting.schema;

import java.io.Serializable;
import java.util.List;

public class Table implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	private boolean view;
	private List<Column> columns;

	public Table(String name, boolean view, List<Column> columns)
	{
		this.name = name;
		this.view = view;
		this.columns = columns;
	}

	public boolean isView()
	{
		return view;
	}

	public String getName()
	{
		return name;
	}

	public List<Column> getColumns()
	{
		return columns;
	}
}
