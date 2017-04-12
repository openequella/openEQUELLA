package com.tle.qti.data;

import java.io.Serializable;

/**
 * Represents some text to make up a QTIMaterial
 * 
 * @author will
 */
public class QTIMaterialText implements QTIMaterialElement, Serializable
{
	private static final long serialVersionUID = 1L;
	private String text;
	private boolean bold = false;

	public QTIMaterialText(String text, boolean bold)
	{
		this.text = text;
		this.bold = bold;
	}

	public QTIMaterialText(String text)
	{
		this(text, false);
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	@SuppressWarnings("nls")
	@Override
	public String getHtml()
	{
		if( bold )
		{
			return "<strong>" + text + "</strong>";
		}
		else
		{
			return text;
		}
	}

	public void setBold(boolean bold)
	{
		this.bold = bold;
	}

	public boolean isBold()
	{
		return bold;
	}
}