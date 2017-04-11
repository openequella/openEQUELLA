package com.tle.web.sections.render;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;

public class RenderFirst extends AbstractPrototypeSection<Object> implements HtmlRenderer
{

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "renderfirst"; //$NON-NLS-1$
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return renderFirstResult(context);
	}

}
