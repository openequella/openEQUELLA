package com.tle.web.viewitem.summary.section;

import java.util.List;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewitem.summary.ItemSummarySidebar;

@SuppressWarnings("nls")
public class ItemSummarySidebarSection
	extends
		AbstractPrototypeSection<ItemSummarySidebarSection.ItemSummarySidebarModel>
	implements
		ViewableChildInterface,
		HtmlRenderer,
		ItemSummarySidebar
{
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ItemSummarySidebarModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());
		return view.createResult("viewitem/summary/itemsummarysidebar.ftl", context);
	}

	@Override
	public Class<ItemSummarySidebarModel> getModelClass()
	{
		return ItemSummarySidebarModel.class;
	}

	public static class ItemSummarySidebarModel
	{
		private List<SectionRenderable> sections;

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}
	}
}
