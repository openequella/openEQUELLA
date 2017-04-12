package com.tle.web.viewitem.summary.content;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
public class ExportContentSection extends AbstractContentSection<ExportContentSection.ExportModel>
{
	public static final String EXPORT_ITEM = "EXPORT_ITEM";

	private PluginTracker<SectionId> exporterTracker;
	private List<SectionId> exporterSections;

	@PlugKey("summary.content.export.pagetitle")
	private static Label TITLE_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	public static void assertCanExport(ItemSectionInfo itemInfo)
	{
		if( !itemInfo.getPrivileges().contains(EXPORT_ITEM) )
		{
			throw new AccessDeniedException("You do not privileges to export items");
		}
	}

	public boolean canView(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).getPrivileges().contains(EXPORT_ITEM);
	}

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);

		assertCanExport(itemInfo);

		addDefaultBreadcrumbs(context, itemInfo, TITLE_LABEL);

		getModel(context).setSections(Lists.transform(exporterSections, new Function<SectionId, SectionRenderable>()
		{
			@Override
			public SectionRenderable apply(SectionId exporter)
			{
				return renderSection(context, exporter);
			}
		}));

		displayBackButton(context);
		return viewFactory.createResult("viewitem/exporters.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		exporterSections = exporterTracker.getBeanList();
		for( SectionId exporter : exporterSections )
		{
			tree.registerInnerSection(exporter, id);
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "export";
	}

	@Override
	public Class<ExportModel> getModelClass()
	{
		return ExportModel.class;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		exporterTracker = new PluginTracker<SectionId>(pluginService, getClass(), "itemExporter", "id",
			new ExtensionParamComparator());
		exporterTracker.setBeanKey("class");
	}

	public static class ExportModel
	{
		private List<SectionRenderable> sections;

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}

		public List<SectionRenderable> getSections()
		{
			return sections;
		}
	}
}
