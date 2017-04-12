package com.tle.beans.item;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public enum ViewableItemType
{
	ITEMS("items"), PREVIEW("preview"), GENERIC("integ/gen"), BLACKBOARD("integ/bb");

	private final String context;

	private ViewableItemType(String s)
	{
		this.context = s;
	}

	public String getContext()
	{
		return context;
	}
}