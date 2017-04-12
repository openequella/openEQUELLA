package com.tle.web.sections.equella.impl;

import com.tle.web.errors.DefaultErrorSection;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.template.Decorations;

public class ModalErrorSection extends DefaultErrorSection
{
	@Override
	public String getDefaultPropertyName()
	{
		return "moderr"; //$NON-NLS-1$
	}

	@Override
	public SectionResult renderErrorHtml(DefaultErrorModel model, RenderEventContext context) throws Exception
	{
		Decorations.getDecorations(context).clearAllDecorations();
		return super.renderErrorHtml(model, context);
	}
}
