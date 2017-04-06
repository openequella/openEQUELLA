package com.tle.web.institution.tab;

import java.util.List;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
public class ServerTab extends AbstractInstitutionTab<ServerTab.ServerTabModel>
{
	@PlugKey("institutions.server.settings.name")
	private static Label LINK_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		ServerTabModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());
		return viewFactory.createResult("tab/server.ftl", context);
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Class<ServerTabModel> getModelClass()
	{
		return ServerTabModel.class;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "servertab";
	}

	public static class ServerTabModel
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
