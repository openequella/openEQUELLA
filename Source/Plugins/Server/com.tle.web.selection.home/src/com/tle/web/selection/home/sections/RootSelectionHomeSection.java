package com.tle.web.selection.home.sections;

import javax.inject.Inject;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout.TwoColumnModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class RootSelectionHomeSection extends TwoColumnLayout<TwoColumnModel>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(RootSelectionHomeSection.class);

	@Inject
	private SelectionService selectionService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SelectionSession session = selectionService.getCurrentSession(context);
		if( session == null )
		{
			throw new RuntimeException(RESOURCES.getString("error.requiresselectionsession"));
		}

		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setContentBodyClass("selectiondashboard");
	}

	@Override
	public Class<TwoColumnModel> getModelClass()
	{
		return TwoColumnModel.class;
	}
}
