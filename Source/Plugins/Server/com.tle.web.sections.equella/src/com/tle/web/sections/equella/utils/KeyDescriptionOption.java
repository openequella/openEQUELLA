package com.tle.web.sections.equella.utils;

import com.tle.web.sections.render.Label;

public class KeyDescriptionOption<T> extends KeyOption<T>
{
	private final Label description;

	public KeyDescriptionOption(String name, Label description, String value, T obj)
	{
		super(name, value, obj);
		this.description = description;
	}

	public Label getDescription()
	{
		return description;
	}

}
