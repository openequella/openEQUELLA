package com.tle.web.sections.jquery;

import com.tle.web.sections.js.ElementId;

public class ClickElement extends JQueryStatement
{
	public ClickElement(ElementId elementId)
	{
		super(elementId, "click()"); //$NON-NLS-1$
	}
}
