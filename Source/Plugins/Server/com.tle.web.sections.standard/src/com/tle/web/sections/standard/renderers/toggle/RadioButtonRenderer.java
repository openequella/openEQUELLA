package com.tle.web.sections.standard.renderers.toggle;

import com.tle.web.sections.standard.model.HtmlBooleanState;

public class RadioButtonRenderer extends CheckboxRenderer
{
	public RadioButtonRenderer(HtmlBooleanState state)
	{
		super(state, "radio"); //$NON-NLS-1$
	}
}
