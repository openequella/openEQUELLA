package com.tle.web.sections.standard.renderers;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class FreemarkerComponentRendererFactory implements RendererFactoryExtension
{
	@Inject
	protected FreemarkerFactory factory;

	private String template;

	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		return new ComponentRenderer(factory.createResultWithModel(getTemplate(), state), state);
	}

	public String getTemplate()
	{
		return template;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}
}