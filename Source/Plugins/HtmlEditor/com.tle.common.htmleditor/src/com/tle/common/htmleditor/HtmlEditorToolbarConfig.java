package com.tle.common.htmleditor;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.PropertyList;

public class HtmlEditorToolbarConfig implements ConfigurationProperties
{
	private static final long serialVersionUID = 1L;

	@PropertyList(key = "buttons")
	private final List<String> buttons = Lists.newArrayList();

	public List<String> getButtons()
	{
		return buttons;
	}
}
