package com.tle.web.controls.universal;

import com.tle.web.sections.render.Label;

public class AttachmentHandlerLabel implements Label
{
	private final Label name;
	private final Label description;

	public AttachmentHandlerLabel(Label name, Label description)
	{
		this.name = name;
		this.description = description;
	}

	@Override
	@SuppressWarnings("nls")
	public String getText()
	{
		return "<h4>" + name.getText() + "</h4>" + description.getText();
	}

	@Override
	public boolean isHtml()
	{
		return true;
	}
}