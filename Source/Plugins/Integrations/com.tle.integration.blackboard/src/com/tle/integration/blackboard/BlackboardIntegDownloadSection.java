package com.tle.integration.blackboard;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

@Bind
public class BlackboardIntegDownloadSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("blackboarddownload.ftl", context); //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return null;
	}
}
