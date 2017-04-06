package com.tle.web.qti.viewer.questions.renderer;

import java.io.IOException;
import java.net.URI;

import uk.ac.ed.ph.jqtiplus.node.item.Stylesheet;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.Check;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

/**
 * @author Aaron
 */
public class StylesheetRenderer extends QtiNodeRenderer
{
	private final Stylesheet model;

	@AssistedInject
	public StylesheetRenderer(@Assisted Stylesheet model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preProcess()
	{
		// Nah
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		final URI href = model.getHref();
		if( href != null )
		{
			final String url = (isRelativeUrl(href) ? getContext().getViewResourceUrl(href.toString()).getHref() : href
				.toString());
			info.addCss(url);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		// Nothing
	}

	private boolean isRelativeUrl(URI url)
	{
		return Check.isEmpty(url.getHost());
	}
}
