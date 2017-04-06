package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlTextFieldState;

/**
 * plugin defines the stateClassName as HtmlTextFieldState
 * 
 * @author Andrew Gibb
 */

@Bind
@Singleton
public class AutoCompleteTextFieldRendererFactory implements RendererFactoryExtension
{
	@Override
	public SectionRenderable getRenderer(RendererFactory factory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		return new AutoCompleteTextFieldRenderer((HtmlTextFieldState) state); // NOSONAR
	}
}
