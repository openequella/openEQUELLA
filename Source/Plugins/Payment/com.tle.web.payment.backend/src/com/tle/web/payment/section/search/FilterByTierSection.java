package com.tle.web.payment.section.search;

import static com.tle.core.payment.PaymentIndexFields.FIELD_FREE_TIER;
import static com.tle.core.payment.PaymentIndexFields.FIELD_PURCHASE_TIER;
import static com.tle.core.payment.PaymentIndexFields.FIELD_SUBSCRIPTION_TIER;
import static com.tle.core.payment.PaymentIndexFields.FIELD_WHITELISTED;
import static com.tle.web.payment.section.search.CataloguesDropdownExtension.WITHIN_ID;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.PresetSearch;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.itemadmin.WithinEntry;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.payment.service.PaymentWebService;
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
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@Bind
public class FilterByTierSection extends AbstractPrototypeSection<FilterByTierSection.FilterByTierModel>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener
{
	public static final String AJAX_ID = "tier-filter"; //$NON-NLS-1$

	private enum CatalogueType
	{
		NONE, DYNAMIC, MANUAL
	}

	@PlugKey("search.tier.all")
	private static String KEY_SELECT;

	@PlugKey("search.tier.none")
	private static String KEY_SELECT_NONE;

	@Inject
	private PricingTierService tierService;
	@Inject
	private CatalogueService catalogueService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private PaymentWebService paymentWebService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@TreeLookup
	private ItemAdminQuerySection querySection;
	@TreeLookup
	private CataloguesDropdownExtension cataloguesDropdownSection;

	@Component(name = "stl", parameter = "stl", supported = true)
	private SingleSelectionList<BaseEntityLabel> subscriptionTierList;

	@Component(name = "ptl", parameter = "ptl", supported = true)
	private SingleSelectionList<BaseEntityLabel> purchaseTierList;

	@PlugKey("search.filter.free")
	@Component(name = "fb", parameter = "fb", supported = true)
	private Checkbox freeBox;
	@PlugKey("search.filter.manual")
	@Component(name = "mb", parameter = "mb", supported = true)
	private Checkbox manualBox;
	@PlugKey("search.filter.auto")
	@Component(name = "ab", parameter = "ab", supported = true)
	private Checkbox autoBox;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		subscriptionTierList.setListModel(new TierListModel(false));
		purchaseTierList.setListModel(new TierListModel(true));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		StatementHandler search = new StatementHandler(searchResults.getRestartSearchHandler(tree));
		subscriptionTierList.setEventHandler(JSHandler.EVENT_CHANGE, search);
		purchaseTierList.setEventHandler(JSHandler.EVENT_CHANGE, search);
		freeBox.setEventHandler(JSHandler.EVENT_CHANGE, search);
		manualBox.setEventHandler(JSHandler.EVENT_CHANGE, search);
		autoBox.setEventHandler(JSHandler.EVENT_CHANGE, search);

	}

	private CatalogueType catalogueType(SectionInfo info)
	{
		WithinEntry selectedValue = querySection.getCollectionList().getSelectedValue(info);
		if( selectedValue == null || !WITHIN_ID.equals(selectedValue.getTypeId()) )
		{
			return CatalogueType.NONE;
		}
		DynaCollection dc = catalogueService.get(selectedValue.getBel().getId()).getDynamicCollection();
		if( dc == null )
		{
			return CatalogueType.MANUAL;
		}

		return CatalogueType.DYNAMIC;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		FilterByTierModel model = getModel(context);
		CatalogueType catalogueSelected = catalogueType(context);
		if( !catalogueSelected.equals(CatalogueType.DYNAMIC) )
		{
			manualBox.setChecked(context, false);
			autoBox.setChecked(context, false);
			model.setShowMembership(false);
		}
		else
		{
			model.setShowMembership(true);
			String selection = cataloguesDropdownSection.getCatalogueWhereList().getSelectedValueAsString(context);
			if( cataloguesDropdownSection.getExculduedKey().equals(selection) )
			{
				manualBox.setChecked(context, false);
				autoBox.setChecked(context, false);
				model.setShowMembership(false);
			}
			else
			{
				model.setShowMembership(true);
			}
		}

		PaymentSettings settings = paymentWebService.getSettings(context);
		model.setShowFree(settings.isFreeEnabled());
		model.setShowSubscription(settings.isSubscriptionEnabled());
		model.setShowPurchase(settings.isPurchaseEnabled());

		return viewFactory.createResult("filterbytier.ftl", context); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		FreeTextBooleanQuery paymentQuery = new FreeTextBooleanQuery(false, true);
		CatalogueType catalogueSelected = catalogueType(info);

		if( freeBox.isChecked(info) )
		{
			event.filterByTerm(false, FIELD_FREE_TIER, "true");
		}

		String purchaseTier = purchaseTierList.getSelectedValueAsString(info);
		if( !Check.isEmpty(purchaseTier) )
		{
			if( purchaseTier.equals("none") )
			{
				paymentQuery.add(getNoTierQuery(true));
			}
			else
			{
				event.filterByTerm(false, FIELD_PURCHASE_TIER, purchaseTier);
			}
		}

		String subscriptionTier = subscriptionTierList.getSelectedValueAsString(info);
		if( !Check.isEmpty(subscriptionTier) )
		{
			if( subscriptionTier.equals("none") )
			{
				paymentQuery.add(getNoTierQuery(false));
			}
			else
			{
				event.filterByTerm(false, FIELD_SUBSCRIPTION_TIER, subscriptionTier);
			}
		}

		if( catalogueSelected.equals(CatalogueType.DYNAMIC) && manualBox.isChecked(info) )
		{
			BaseEntityLabel catalogue = querySection.getCollectionList().getSelectedValue(info).getBel();
			event.filterByTerm(false, FIELD_WHITELISTED, catalogue.getUuid());
		}

		if( catalogueSelected.equals(CatalogueType.DYNAMIC) && autoBox.isChecked(info) )
		{
			BaseEntityLabel catalogue = querySection.getCollectionList().getSelectedValue(info).getBel();
			PresetSearch dynamicOnly = catalogueService.createSearch(catalogue.getUuid(), true);

			event.filterByTextQuery(dynamicOnly.getQuery(), false);
			paymentQuery.add(dynamicOnly.getFreeTextQuery());
		}

		DefaultSearch defaultSeach = event.getDefaultSeach();
		defaultSeach.setFreeTextQuery(paymentQuery);

	}

	private FreeTextQuery getNoTierQuery(boolean purchase)
	{
		FreeTextBooleanQuery noTierQuery = new FreeTextBooleanQuery(true, false);

		List<BaseEntityLabel> tiers = tierService.listAll(purchase);
		for( BaseEntityLabel tier : tiers )
		{
			noTierQuery.add(new FreeTextFieldQuery(purchase ? FIELD_PURCHASE_TIER : FIELD_SUBSCRIPTION_TIER, tier
				.getUuid()));
		}

		return noTierQuery;
	}

	public class TierListModel extends DynamicHtmlListModel<BaseEntityLabel>
	{
		private final boolean purchase;

		public TierListModel(boolean purchase)
		{
			this.purchase = purchase;
			setSort(true);
		}

		@Override
		protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
		{
			return tierService.listAll(purchase);
		}

		@SuppressWarnings("nls")
		@Override
		protected Option<BaseEntityLabel> getTopOption()
		{
			return new KeyOption<BaseEntityLabel>(KEY_SELECT, "", null);
		}

		@Override
		protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel bent)
		{
			return new NameValueOption<BaseEntityLabel>(new BundleNameValue(bent.getBundleId(), bent.getUuid(),
				bundleCache), bent);
		}

		@Override
		protected Iterable<Option<BaseEntityLabel>> populateOptions(SectionInfo info)
		{
			ArrayList<Option<BaseEntityLabel>> options = new ArrayList<Option<BaseEntityLabel>>();
			options.add(new KeyOption<BaseEntityLabel>(KEY_SELECT_NONE, "none", null));
			return options;
		}

	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbt"; //$NON-NLS-1$
	}

	@Override
	public void reset(SectionInfo info)
	{
		subscriptionTierList.setSelectedValue(info, null);
		purchaseTierList.setSelectedValue(info, null);
		freeBox.setChecked(info, false);
		autoBox.setChecked(info, false);
		manualBox.setChecked(info, false);
	}

	@Override
	public Class<FilterByTierModel> getModelClass()
	{
		return FilterByTierModel.class;
	}

	public SingleSelectionList<BaseEntityLabel> getSubscriptionTierList()
	{
		return subscriptionTierList;
	}

	public SingleSelectionList<BaseEntityLabel> getPurchaseTierList()
	{
		return purchaseTierList;
	}

	public Checkbox getFreeBox()
	{
		return freeBox;
	}

	public Checkbox getManualBox()
	{
		return manualBox;
	}

	public Checkbox getAutoBox()
	{
		return autoBox;
	}

	public static class FilterByTierModel
	{
		private boolean showMembership;
		private boolean showFree;
		private boolean showPurchase;
		private boolean showSubscription;

		public boolean isShowMembership()
		{
			return showMembership;
		}

		public void setShowMembership(boolean showMembership)
		{
			this.showMembership = showMembership;
		}

		public boolean isShowFree()
		{
			return showFree;
		}

		public void setShowFree(boolean showFree)
		{
			this.showFree = showFree;
		}

		public boolean isShowPurchase()
		{
			return showPurchase;
		}

		public void setShowPurchase(boolean showPurchase)
		{
			this.showPurchase = showPurchase;
		}

		public boolean isShowSubscription()
		{
			return showSubscription;
		}

		public void setShowSubscription(boolean showSubscription)
		{
			this.showSubscription = showSubscription;
		}
	}
}
