package com.tle.web.payment.storefront.section.itemadmin;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.payment.StoreFrontIndexFields;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

@Bind
@SuppressWarnings("nls")
public class FilterByPurchasedSection extends AbstractPrototypeSection<FilterByPurchasedSection.FilterByPurchasedModel>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	private String layout = SearchResultsActionsSection.AREA_FILTER;

	@PlugKey("filter.bypurchased.checkbox")
	@Component
	private Checkbox filter;

	@Override
	public void registered(String id, SectionTree tree)
	{
		if( !Check.isEmpty(layout) )
		{
			tree.setLayout(id, layout);
		}
		super.registered(id, tree);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("shop/filterbypurchased.ftl", context);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler searchHandler = new OverrideHandler(searchResults.getResultsUpdater(tree, null));
		filter.addClickStatements(searchHandler);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( filter.isChecked(info) )
		{
			event.filterByTerm(false, StoreFrontIndexFields.FIELD_IS_PURCHASED, "true");
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		filter.setChecked(info, false);
	}

	public static class FilterByPurchasedModel
	{
		//
	}

	public Checkbox getFilter()
	{
		return filter;
	}
}
