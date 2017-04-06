package com.tle.core.qti.parse.v1x;

import java.io.Serializable;

/**
 * @author will
 */
public class QTIResponseElement implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String id;
	private QTIMaterial display;
	private String type;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public QTIMaterial getDisplay()
	{
		return display;
	}

	public void setDisplay(QTIMaterial display)
	{
		this.display = display;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}