package com.dytech.edge.admin.script.model;

import com.dytech.edge.admin.script.Row;

public class Node implements Row
{
	protected Object parent;
	protected String text;

	public void setParent(Object parent)
	{
		this.parent = parent;
	}

	public Object getParent()
	{
		return parent;
	}

	public void setText(String text)
	{
		this.text = "<html>" + text;
	}

	public void appendText(String text)
	{
		this.text += text;
	}

	@Override
	public String toString()
	{
		return text;
	}
}
