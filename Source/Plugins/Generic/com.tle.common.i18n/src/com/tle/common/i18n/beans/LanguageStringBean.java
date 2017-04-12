package com.tle.common.i18n.beans;

import java.io.Serializable;

/**
 * @author Aaron
 */
public class LanguageStringBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private String locale;
	private int priority;
	private String text;

	public LanguageStringBean()
	{
	}

	public LanguageStringBean(String locale, String text)
	{
		this.locale = locale;
		this.text = text;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getLocale()
	{
		return locale;
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
}
