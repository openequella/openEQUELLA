package com.tle.web.payment.shop.section.search;

import com.tle.web.payment.shop.event.ShopSearchEvent;
import com.tle.web.payment.shop.section.search.ShopSearchQuerySection.ShopSearchQueryModel;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.result.util.KeyLabel;

/**
 * @author dustin
 */
@SuppressWarnings("nls")
public class ShopSearchQuerySection extends AbstractResetFiltersQuerySection<ShopSearchQueryModel, ShopSearchEvent>
{
	private static final String DIV_RESULTS = "searchresults-cont";

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private ShopSearchResultsSection searchResults;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		searchButton.setLabel(new KeyLabel("item.section.query.search"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("shop/shopsearchquery.ftl", this);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler searchHandler = new OverrideHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("saveQuery"), DIV_RESULTS));
		searchButton.setClickHandler(searchHandler);
	}

	@EventHandlerMethod
	public void saveQuery(SectionInfo info)
	{

	}

	public static class ShopSearchQueryModel
	{
		// NOthing to do here
	}
}
