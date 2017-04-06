package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlFileDropState;
import com.tle.web.sections.standard.renderers.FileDropRenderer;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EquellaFileDropExtension implements RendererFactoryExtension
{
	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		return new FileDropRenderer((HtmlFileDropState) state);
	}
}
