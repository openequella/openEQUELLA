package com.tle.web.portal.standard.renderer;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class HtmlPortletRenderer extends PortletContentRenderer<HtmlPortletRenderer.Model>
{
	@Inject
	private HtmlEditorService htmlEditorService;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		getModel(context).setHtml(htmlEditorService.getHtmlRenderable(context, portlet.getConfig()));
		return viewFactory.createResult("htmlportlet.ftl", this);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pht";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private SectionRenderable html;

		public SectionRenderable getHtml()
		{
			return html;
		}

		public void setHtml(SectionRenderable html)
		{
			this.html = html;
		}
	}
}
