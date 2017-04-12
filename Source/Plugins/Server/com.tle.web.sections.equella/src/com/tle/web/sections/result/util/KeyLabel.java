package com.tle.web.sections.result.util;

import java.io.Serializable;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;

public class KeyLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	private final String key;
	private final Object[] args;
	private boolean html = true;

	public KeyLabel(boolean html, String key)
	{
		this.key = key;
		this.args = null;
		this.html = html;
	}

	public KeyLabel(String key)
	{
		this.key = key;
		this.args = null;
	}

	public KeyLabel(String key, Label... args)
	{
		this.key = key;
		this.args = new Object[args.length];
		int i = 0;
		for( Label label : args )
		{
			this.args[i++] = new LabelRenderer(label);
		}
	}

	public KeyLabel(String key, Object... args)
	{
		this.key = key;
		this.args = args;
	}

	@Override
	public String getText()
	{
		return CurrentLocale.get(key, args);
	}

	@Override
	public String toString()
	{
		throw new UnsupportedOperationException("Please user LabelRenderer instead"); //$NON-NLS-1$
	}

	@Override
	public boolean isHtml()
	{
		return html;
	}

	public void setHtml(boolean html)
	{
		this.html = html;
	}
}
