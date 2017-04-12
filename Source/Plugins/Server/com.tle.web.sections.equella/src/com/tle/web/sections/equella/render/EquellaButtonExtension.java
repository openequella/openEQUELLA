package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonSize;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EquellaButtonExtension implements RendererFactoryExtension
{
	public static final String ACTION_BUTTON = "action-button";
	public static final String BOOTSTRAP_BUTTON = "bootstrap-button";

	public static final String CLASS_BUTTON = "btn btn-equella btn-mini";

	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		if( renderer.equals(RendererConstants.BUTTON) || renderer.equals(BOOTSTRAP_BUTTON) )
		{
			return new ButtonRenderer(state).setSize(ButtonSize.SMALL);
		}
		else if( renderer.equals(ACTION_BUTTON) )
		{
			return new com.tle.web.sections.standard.renderers.ButtonRenderer(state).addClass("action-button");
		}
		return new com.tle.web.sections.standard.renderers.ButtonRenderer(state).addClass(CLASS_BUTTON);
	}
}
