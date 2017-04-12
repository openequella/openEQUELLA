package com.tle.web.institution.tab;

import java.util.List;

import javax.inject.Inject;

import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;

public class IntegrationDownloadTab extends AbstractInstitutionTab<IntegrationDownloadTab.IntegrationDownloadModel>
{
	@PlugKey("institutions.integdownload.link.name")
	private static Label LINK_LABEL;

	@Inject
	private PluginService pluginService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public String getDefaultPropertyName()
	{
		return "integdownload"; //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		// TODO: The won't work if new integrationDownloadSection extensions are
		// added during runtime.

		PluginTracker<Section> downloadSections = new PluginTracker<Section>(pluginService, getClass(),
			"integrationDownloadSection", "class", //$NON-NLS-1$ //$NON-NLS-2$
			new PluginTracker.ExtensionParamComparator("order")).setBeanKey("class"); //$NON-NLS-1$ //$NON-NLS-2$

		for( Section section : downloadSections.getBeanList() )
		{
			tree.registerSections(section, id);
		}
	}

	@Override
	public Class<IntegrationDownloadModel> getModelClass()
	{
		return IntegrationDownloadModel.class;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		IntegrationDownloadModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());
		return viewFactory.createResult("tab/integdownload.ftl", context); //$NON-NLS-1$
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	public static class IntegrationDownloadModel
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
