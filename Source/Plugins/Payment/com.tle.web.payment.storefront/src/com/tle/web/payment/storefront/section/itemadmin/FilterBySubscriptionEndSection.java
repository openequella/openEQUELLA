package com.tle.web.payment.storefront.section.itemadmin;

import java.util.Date;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.searching.DateFilter;
import com.tle.core.guice.Bind;
import com.tle.core.payment.StoreFrontIndexFields;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.AbstractFilterByDateRangeSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;

@NonNullByDefault
@Bind
public class FilterBySubscriptionEndSection extends AbstractFilterByDateRangeSection<FreetextSearchEvent>
{
	@PlugKey("filter.byenddate.title")
	private static Label LABEL_TITLE;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	private String layout = SearchResultsActionsSection.AREA_FILTER;

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
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		super.renderHtml(context);
		return viewFactory.createResult("shop/filterbysubscription.ftl", context); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		getDatePrimary()
			.setEventHandler(
				JSHandler.EVENT_CHANGE,
				new OverrideHandler(searchResults.getResultsUpdater(tree, events.getEventHandler("showClear"),
					"clear-sub")));
		getDateSecondary()
			.setEventHandler(
				JSHandler.EVENT_CHANGE,
				new OverrideHandler(searchResults.getResultsUpdater(tree, events.getEventHandler("showClear"),
					"clear-sub")));
		// When trying to override this the clear divs got mixed up with the
		// other date picker so I had to override some stuff
	}

	public static class FilterBySubscriptionEndModel
	{
		//
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		Date[] dateRange = getDateRange(info);
		if( dateRange != null )
		{
			event.addDateFilter(new DateFilter(StoreFrontIndexFields.FIELD_SUBSCRIPTION_END, dateRange));
		}
	}

	@SuppressWarnings("nls")
	@Override
	protected String[] getParameterNames()
	{
		return new String[]{"sdp", "sds", "sdr"};
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public String getAjaxDiv()
	{
		return "subscription-end-filter"; //$NON-NLS-1$
	}
}
