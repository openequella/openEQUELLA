package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.Label;

public class LabelOption<T> implements Option<T>
{
	private final Label label;
	private final String value;
	private final T object;
	private boolean disabled;

	public LabelOption(Label label, String value, T object)
	{
		this.label = label;
		this.value = value;
		this.object = object;
	}

	@Override
	public String getName()
	{
		return label.getText();
	}

	@Override
	public boolean isNameHtml()
	{
		return label.isHtml();
	}

	@Override
	public T getObject()
	{
		return object;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public boolean isDisabled()
	{
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	@Override
	public boolean hasAltTitleAttr()
	{
		return false;
	}

	@Override
	public String getAltTitleAttr()
	{
		return null;
	}

	@Override
	public String getGroupName()
	{
		return null;
	}
}
