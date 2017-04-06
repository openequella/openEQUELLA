package com.tle.web.portal.standard.renderer;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class ShowcasePortletRenderer extends PortletContentRenderer<Object>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(ShowcasePortletRenderer.class);

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		context.getPreRenderContext().addCss(RESOURCES.url("css/showcase.css"));
		return view.createResult("showcaseportlet.ftl", context);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "psc";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
