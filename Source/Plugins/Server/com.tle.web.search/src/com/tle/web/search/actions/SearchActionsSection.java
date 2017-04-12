package com.tle.web.search.actions;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.HtmlRenderer;

@NonNullByDefault
@SuppressWarnings("nls")
public class SearchActionsSection
	extends
		AbstractSearchActionsSection<AbstractSearchActionsSection.AbstractSearchActionsModel> implements HtmlRenderer
{
	@EventFactory
	private EventGenerator events;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, TwoColumnLayout.RIGHT);
	}

	@Override
	public String[] getResetFilterAjaxIds()
	{
		return new String[]{};
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		renderSectionsToModel(context);

		return viewFactory.createResult("searchactions.ftl", this);
	}

	public List<SectionId> getTopSections()
	{
		return topSections;
	}

	@Override
	public Class<AbstractSearchActionsModel> getModelClass()
	{
		return AbstractSearchActionsModel.class;
	}
}
