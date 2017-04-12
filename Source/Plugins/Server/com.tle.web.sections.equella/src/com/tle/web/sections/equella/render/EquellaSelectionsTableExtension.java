package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * @author aholland
 */
@Bind
@Singleton
public class EquellaSelectionsTableExtension implements RendererFactoryExtension
{
	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		final SelectionsTableState stuff = (SelectionsTableState) state; // NOSONAR
		return new SelectionsTableRenderer(stuff, rendererFactory);
	}
}
