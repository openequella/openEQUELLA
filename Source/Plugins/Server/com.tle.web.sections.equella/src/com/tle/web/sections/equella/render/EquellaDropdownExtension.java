package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;

/**
 * plugin defines the stateClassName as HtmlListState
 */
@Bind
@Singleton
public class EquellaDropdownExtension implements RendererFactoryExtension
{
	@Inject
	private AccessibilityModeService acMode;

	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		HtmlListState htmlListState = (HtmlListState) state; // NOSONAR

		if( renderer.equals(BootstrapDropDownRenderer.RENDER_CONSTANT) )
		{
			return new BootstrapDropDownRenderer(htmlListState, false);
		}
		else if( renderer.equals(BootstrapDropDownRenderer.ACTIVE_RENDER_CONSTANT) )
		{
			return new BootstrapDropDownRenderer(htmlListState, true);
		}
		else if( renderer.equals(BootstrapSplitDropDownRenderer.SPLIT_RENDER_CONSTANT) )
		{
			return new BootstrapSplitDropDownRenderer((HtmlListState) state);
		}
		else if( renderer.equals(BootstrapSplitDropDownRenderer.SPLIT_NAVBAR_RENDER_CONSTANT) )
		{
			return new BootstrapSplitDropDownRenderer((HtmlListState) state, ButtonType.NAV);
		}

		if( acMode.isAccessibilityMode() )
		{
			return new DropDownRenderer(htmlListState);
		}
		else
		{
			return new StylishDropDownRenderer(htmlListState);
		}
	}
}
