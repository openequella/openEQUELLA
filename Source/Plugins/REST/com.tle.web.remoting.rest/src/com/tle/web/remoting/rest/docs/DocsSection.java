package com.tle.web.remoting.rest.docs;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.template.Decorations;

@Bind
public class DocsSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugKey("docs.title")
	private static Label LABEL_TITLE;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations decorations = Decorations.getDecorations(context);
		decorations.setTitle(LABEL_TITLE);
		// decorations.clearAllDecorations();
		return viewFactory.createResult("swagger.ftl", this);
	}

}
