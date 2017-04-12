package com.tle.web.sections.equella.freemarker;

import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.methods.AbstractRenderDirective;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;

import freemarker.template.TemplateModel;

@NonNullByDefault
public class RenderDirective extends AbstractRenderDirective
{
	private final RendererFactory factory;

	public RenderDirective(RendererFactory factory)
	{
		this.factory = factory;
	}

	@Override
	protected SectionRenderable getRenderable(Object wrapped, Map<String, TemplateModel> params)
	{
		return ChooseRenderer.getSectionRenderable(getSectionWriter(), wrapped, getParam("type", //$NON-NLS-1$
			params), factory);
	}
}
