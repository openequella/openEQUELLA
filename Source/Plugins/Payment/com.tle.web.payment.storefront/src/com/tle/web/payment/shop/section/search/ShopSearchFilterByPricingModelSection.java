package com.tle.web.payment.shop.section.search;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.tle.common.Check;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.payment.shop.event.ShopSearchEvent;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
public class ShopSearchFilterByPricingModelSection
	extends
		AbstractPrototypeSection<ShopSearchFilterByPricingModelSection.ShopSearchFilterByPricingModelModel>
	implements
		HtmlRenderer,
		SearchEventListener<ShopSearchEvent>,
		ResetFiltersListener
{
	// These values are passed to the REST query
	private static final String VAL_ANY = "any";
	private static final String VAL_FREE = "free";
	private static final String VAL_OUTRIGHT = "purchase";

	private static final String ajaxDiv = "price-filter";
	private static final String VAL_SUBSCRIPTION = "subscription";

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@ResourceHelper
	private PluginResourceHelper resources;

	@Inject
	private ShopService shopService;

	@TreeLookup
	private ShopSearchResultsSection searchResults;

	@Component(name = "pf", parameter = "price", supported = true)
	private SingleSelectionList<BundleNameValue> filterOptions;
	@Component
	private Button clearButton;

	private String layout = SearchResultsActionsSection.AREA_FILTER;

	public static String getValOutright()
	{
		return VAL_OUTRIGHT;
	}

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
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler searchHandler = new OverrideHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("filterChanged"), getAjaxDiv()));
		filterOptions.addChangeEventHandler(searchHandler);
		clearButton.setClickHandler(new OverrideHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("clear"), getAjaxDiv())));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		List<BundleNameValue> sorts = new ArrayList<BundleNameValue>();
		addFilterOptions(sorts, context);
		filterOptions.setAlwaysSelect(true);
		filterOptions.setListModel(new SimpleHtmlListModel<BundleNameValue>(sorts));
		return viewFactory.createResult("shop/shopsearchpricingmodelfilter.ftl", this);
	}

	@EventHandlerMethod
	public void filterChanged(SectionInfo info)
	{
		getModel(info).setShowClearButton(true);
		getModel(info).setSelectedValue(filterOptions.getSelectedValueAsString(info));
	}

	@EventHandlerMethod
	public void clear(SectionInfo info)
	{
		reset(info);
	}

	public void addFilterOptions(List<BundleNameValue> sorts, SectionInfo info)
	{
		Store store = ShopSearchSectionInfo.getSearchInfo(info).getStore();
		StorePricingInformationBean pricingInfo = shopService.getPricingInformation(store, false);
		sorts.add(new BundleNameValue(resources.key("store.search.filter.any"), VAL_ANY));
		if( pricingInfo.isAllowFree() )
		{
			sorts.add(new BundleNameValue(resources.key("store.search.filter.free"), VAL_FREE));
		}

		if( pricingInfo.isAllowPurchase() )
		{
			sorts.add(new BundleNameValue(resources.key("store.search.filter.outright"), VAL_OUTRIGHT));
		}

		if( pricingInfo.isAllowSubscription() )
		{
			sorts.add(new BundleNameValue(resources.key("store.search.filter.subscription"), VAL_SUBSCRIPTION));
		}

		getModel(info).setHideSection(sorts.size() <= 2);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShopSearchFilterByPricingModelModel();
	}

	@Override
	public Class<ShopSearchFilterByPricingModelModel> getModelClass()
	{
		return ShopSearchFilterByPricingModelModel.class;
	}

	public class ShopSearchFilterByPricingModelModel
	{
		private boolean showClearButton;
		private String selectedValue;
		private boolean hideSection;

		public boolean isHideSection()
		{
			return hideSection;
		}

		public void setHideSection(boolean hideSection)
		{
			this.hideSection = hideSection;
		}

		public boolean isShowClearButton()
		{
			return showClearButton;
		}

		public void setShowClearButton(boolean showClearButton)
		{
			this.showClearButton = showClearButton;
		}

		public String getSelectedValue()
		{
			return selectedValue;
		}

		public void setSelectedValue(String selectedValue)
		{
			this.selectedValue = selectedValue;
		}
	}

	public SingleSelectionList<BundleNameValue> getFilterOptions()
	{
		return filterOptions;
	}

	public String getFilter(SectionInfo info)
	{
		return filterOptions.getSelectedValueAsString(info);
	}

	@Override
	public void prepareSearch(SectionInfo info, ShopSearchEvent event) throws Exception
	{
		event.setPriceFilter(filterOptions.getSelectedValueAsString(info));
	}

	public Button getClearButton()
	{
		return clearButton;
	}

	public String getAjaxDiv()
	{
		return ajaxDiv;
	}

	@Override
	public void reset(SectionInfo info)
	{
		filterOptions.setSelectedStringValue(info, "all");
		getModel(info).setShowClearButton(false);
	}
}
