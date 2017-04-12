package com.tle.web.payment.shop.section.viewitem;

import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserSessionService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.shop.PriceSelection;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.service.ShopMoneyLabelService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.js.StandardExpressions;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.NotEqualsExpression;
import com.tle.web.sections.js.generic.expression.OrExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.ListOption;
import com.tle.web.sections.standard.NumberField;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.list.BooleanListRenderer;
import com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ShopItemPurchaseDetailsSection
	extends
		AbstractPrototypeSection<ShopItemPurchaseDetailsSection.PurchaseDetailsDisplayModel> implements HtmlRenderer
{
	private static final IncludeFile INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		ShopItemPurchaseDetailsSection.class).url("scripts/viewitem.js"), StandardExpressions.STANDARD_JS);
	private static final ExternallyDefinedFunction DELAY_RELOAD_FUNCTION = new ExternallyDefinedFunction("delayReload",
		2, INCLUDE);

	private static final String KEY_INVALID_NUMBER_OUT = "invalid.number.out";
	private static final String KEY_INVALID_NUMBER_SUB = "invalid.number.sub";
	private static final String KEY_NO_SUB_DURATION = "no.duration";
	private static final String KEY_NO_DATE = "no.date";

	private static final int DEFAULT_USERS_NUMBER = 1;

	private enum PricingTypes
	{
		PURCHASE, SUBSCRIBE, FREE
	}

	private enum DateTypes
	{
		PAYDATE, OTHERDATE
	}

	@PlugKey("shop.browse.error.accessdenied")
	private static String KEY_ERROR_ACCESS_DENIED;

	@PlugKey("shop.viewitem.pricing.subscribe.duration.table.header.duration")
	private static Label LABEL_HEADER_DURATION;
	@PlugKey("shop.viewitem.pricing.subscribe.duration.table.header.cost")
	private static Label LABEL_HEADER_COST;
	@PlugKey("shop.viewitem.pricing.subscribe.duration.table.header.costperuser")
	private static Label LABEL_HEADER_COSTPERUSER;
	@PlugKey("shop.viewitem.pricing.label.free")
	private static Label LABEL_FREE;

	@PlugKey("shop.viewitem.error.users")
	private static Label LABEL_ERROR_USERS;
	@PlugKey("shop.viewitem.error.duration")
	private static Label LABEL_ERROR_DURATION;
	@PlugKey("shop.viewitem.error.date")
	private static Label LABEL_ERROR_DATE;

	@PlugKey("shop.viewitem.error.")
	private static String KEY_ERROR_PREFIX;
	@PlugKey("shop.viewitem.pricing.")
	private static String KEY_PRICING_PREFIX;
	@PlugKey("shop.viewitem.pricing.subscribe.date.")
	private static String KEY_DATE_PREFIX;

	@PlugKey("shop.viewitem.table.header.purchaser")
	private static Label LABEL_HEADER_PURCHASER;
	@PlugKey("shop.viewitem.table.header.start")
	private static Label LABEL_HEADER_START_DATE;
	@PlugKey("shop.viewitem.table.header.purchasedate")
	private static Label LABEL_HEADER_PURCHASE_DATE;
	@PlugKey("shop.viewitem.table.header.startandpurchased")
	private static Label LABEL_HEADER_START_AND_PURCHASE;
	@PlugKey("shop.viewitem.table.header.finish")
	private static Label LABEL_HEADER_FINISH_DATE;
	@PlugKey("shop.viewitem.table.header.users")
	private static Label LABEL_HEADER_NUM_USERS;
	@PlugKey("shop.viewitem.table.flatrate")
	private static Label LABEL_FLAT_RATE;

	@PlugKey("shop.viewitem.warning.subscribed")
	private static Label LABEL_WARNING_SUBSCRIBED;
	@PlugKey("shop.viewitem.warning.subexpired")
	private static Label LABEL_WARNING_SUB_EXPIRED;
	@PlugKey("shop.viewitem.warning.purchased")
	private static Label LABEL_WARNING_PURCHASED;
	@PlugKey("shop.viewitem.warning.purchandsub")
	private static Label LABEL_WARNING_PURCHANDSUB;
	@PlugKey("shop.viewitem.warning.purchwithusers")
	private static Label LABEL_WARNING_PURCH_PER_USER;

	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private OrderService orderService;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private PurchaseService purchaseService;
	@Inject
	private UserSessionService userSessionService;
	@Inject
	private ShopMoneyLabelService moneyLabelService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(name = "pbt")
	private Table purchasedBeforeTable;
	@Component
	private Table pricingTable;

	// You should be able to set these via URL
	@Component(name = "nup", stateful = true)
	private NumberField numberOfUsersPurchase;
	@Component(name = "nus", stateful = true)
	private NumberField numberOfUsersSubscribe;
	@Component(name = "ds", stateful = true)
	private SingleSelectionList<StorePriceBean> durationSelector;
	@Component(name = "pt", stateful = true)
	private SingleSelectionList<PricingTypes> pricingType;
	@Component(name = "sd", stateful = true)
	private SingleSelectionList<DateTypes> startDate;

	@Component(name = "sdt")
	private Calendar otherDate;
	@PlugKey("shop.viewitem.pricing.button.addtocart")
	@Component
	private Button addToCartButton;
	@PlugKey("shop.viewitem.pricing.button.removefromcart")
	@Component
	private Button removeFromCartButton;

	private StatementHandler updateTotalHandler;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final PurchaseDetailsDisplayModel model = getModel(context);
		final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(context);
		final StoreCatalogueItemBean item = iinfo.getItem();

		// ensure BROWSE priv
		final Store store = iinfo.getStore();
		if( aclService.filterNonGrantedObjects(Collections.singleton(StoreFrontConstants.PRIV_BROWSE_STORE),
			Collections.singleton(store)).isEmpty() )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_ACCESS_DENIED,
				StoreFrontConstants.PRIV_BROWSE_STORE));
		}

		Order shoppingCart = null;

		if( !aclService.filterNonGrantedPrivileges(StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART).isEmpty() )
		{
			model.setShoppingCartAccessible(true);
			shoppingCart = orderService.getShoppingCart();
		}

		OrderItem addedItem = null;
		if( shoppingCart != null )
		{
			addedItem = orderService.getOrderItem(shoppingCart, store, item.getUuid());
			model.setMultiplePricingOptions(pricingType.getListModel().getOptions(context).size() > 1);
		}
		model.setInCart(addedItem != null);

		List<PurchaseItem> purchaseList = purchaseService.enumerateForSourceItem(item.getUuid());
		if( !purchaseList.isEmpty() )
		{
			model.setOtherPurchasers(true);
			setUpPurchasersTable(context, purchaseList);
		}

		if( model.isMultiplePricingOptions() )
		{
			if( pricingType.getSelectedValue(context) == PricingTypes.PURCHASE )
			{
				numberOfUsersPurchase.setDisabled(context, false);
				numberOfUsersSubscribe.setDisabled(context, true);
				startDate.setDisabled(context, true);
				otherDate.setDisabled(context, true);
				durationSelector.setDisabled(context, true);
			}
			else
			{
				numberOfUsersPurchase.setDisabled(context, true);
				if( startDate.getSelectedValue(context) == DateTypes.PAYDATE )
				{
					otherDate.setDisabled(context, true);
				}
			}
		}

		// getting info from shopping cart, if item is in shopping cart
		if( addedItem != null )
		{
			int users = (addedItem.getUsers() == 0) ? 1 : addedItem.getUsers();
			numberOfUsersPurchase.setValue(context, users);
			numberOfUsersSubscribe.setValue(context, users);

			Date subscriptionStartDate = addedItem.getSubscriptionStartDate();
			if( subscriptionStartDate != null )
			{
				otherDate.setDate(context, new UtcDate(subscriptionStartDate));
				startDate.setSelectedValue(context, DateTypes.OTHERDATE);
			}
			else
			{
				otherDate.clearDate(context);
				startDate.setSelectedValue(context, DateTypes.PAYDATE);
			}

			String periodUuid = addedItem.getPeriodUuid();
			if( periodUuid != null )
			{
				durationSelector.setSelectedStringValue(context, periodUuid);
				pricingType.setSelectedValue(context, PricingTypes.SUBSCRIBE);
			}

			durationSelector.setDisabled(context, true);
			numberOfUsersPurchase.setDisabled(context, true);
			numberOfUsersSubscribe.setDisabled(context, true);
			startDate.setDisabled(context, true);
			pricingType.setDisabled(context, true);
			otherDate.setDisabled(context, true);
		}

		boolean free = item.isFree();
		if( free )
		{
			model.setTotalMoneyLabel(LABEL_FREE);
			model.setFreeItem(true);
		}
		else
		{
			final boolean showTax = moneyLabelService.isShowTax();
			StorePurchaseTierBean purchaseTier = item.getPurchaseTier();
			StoreSubscriptionTierBean subscriptionTier = item.getSubscriptionTier();

			if( purchaseTier != null && subscriptionTier == null )
			{
				setupPurchaseTier(context, purchaseTier, showTax);
			}
			else if( subscriptionTier != null && purchaseTier == null )
			{
				if( startDate.getSelectedValue(context) == DateTypes.PAYDATE )
				{
					otherDate.setDisabled(context, true);
				}
				setupSubscriptionTier(context, subscriptionTier, showTax);
			}
			else if( purchaseTier != null && subscriptionTier != null )
			{
				loadBothTiers(context, purchaseTier, subscriptionTier, showTax);
			}

			setUpPurchaseTypeRadios(context);
			model.setTotalMoneyLabel(calculateTotalLabel(context, showTax));
		}
		return view.createResult("shop/itempurchasedetails.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		addToCartButton.setClickHandler(events.getNamedHandler("addToCart"));

		UpdateDomFunction updateTotalFunction = ajax.getAjaxUpdateDomFunction(tree, this, null,
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "updateTotal");
		updateTotalHandler = new StatementHandler(updateTotalFunction);

		JSHandler subscribeHandler = Js.handler((JSCallable) CallAndReferenceFunction.get(
			Js.function(Js.call_s(DELAY_RELOAD_FUNCTION, Jq.$(numberOfUsersSubscribe),
				CallAndReferenceFunction.get(updateTotalFunction, numberOfUsersSubscribe))), numberOfUsersSubscribe));

		removeFromCartButton.setClickHandler(events.getNamedHandler("removeFromCart"));

		numberOfUsersSubscribe.setEventHandler(JSHandler.EVENT_CHANGE, subscribeHandler);
		numberOfUsersSubscribe.setEventHandler(JSHandler.EVENT_KEYUP, subscribeHandler);
		numberOfUsersSubscribe.setMin(0);
		numberOfUsersSubscribe.setStep(1);
		numberOfUsersSubscribe.setDefaultNumber(DEFAULT_USERS_NUMBER);

		JSHandler purchaseHandler = Js.handler((JSCallable) CallAndReferenceFunction.get(
			Js.function(Js.call_s(DELAY_RELOAD_FUNCTION, Jq.$(numberOfUsersPurchase),
				CallAndReferenceFunction.get(updateTotalFunction, numberOfUsersPurchase))), numberOfUsersPurchase));

		numberOfUsersPurchase.setEventHandler(JSHandler.EVENT_CHANGE, purchaseHandler);
		numberOfUsersPurchase.setEventHandler(JSHandler.EVENT_KEYUP, purchaseHandler);
		numberOfUsersPurchase.setMin(0);
		numberOfUsersPurchase.setStep(1);
		numberOfUsersPurchase.setDefaultNumber(DEFAULT_USERS_NUMBER);

		startDate.setListModel(new EnumListModel<DateTypes>(KEY_DATE_PREFIX, true, DateTypes.values()));
		startDate.setAlwaysSelect(true);

		CombinedDisableable dateDisabler = new CombinedDisableable(otherDate, otherDate);

		JSExpression otherDateSelected = new NotEqualsExpression(startDate.createGetExpression(), new StringExpression(
			DateTypes.OTHERDATE.name().toLowerCase()));

		FunctionCallStatement dateCallStatement = new FunctionCallStatement(dateDisabler.createDisableFunction(),
			otherDateSelected);

		startDate.addChangeEventHandler(new StatementHandler(dateCallStatement));

		pricingType.setListModel(new PricingListModel());
		pricingType.setAlwaysSelect(true);

		durationSelector.setListModel(new SubscriptionDurationListModel());
		durationSelector.setAlwaysSelect(true);
		durationSelector.setDefaultRenderer(RendererConstants.BOOLEANLIST);
		durationSelector.setEventHandler(JSHandler.EVENT_CHANGE, updateTotalHandler);
	}

	@DirectEvent
	public void loadDefaults(SectionInfo info)
	{
		final PurchaseDetailsDisplayModel model = getModel(info);

		if( !model.isLoaded() )
		{
			// force default selection for list models:
			pricingType.setSelectedStringValue(info, null);
			durationSelector.setSelectedStringValue(info, null);

			final PriceSelection defaults = userSessionService.getAttribute(ShopConstants.KEY_PRICE_SELECTIONS);
			if( defaults != null )
			{
				final Date start = defaults.getStart();
				if( start != null )
				{
					startDate.setSelectedValue(info, DateTypes.OTHERDATE);
					otherDate.setDate(info, new UtcDate(start));
				}

				// final Boolean sessSub = defaults.getSubscription();
				// pricingType.setSelectedValue(info, PricingTypes.PURCHASE);
				// if( sessSub != null && sessSub )
				// {
				// pricingType.setSelectedValue(info, PricingTypes.SUBSCRIBE);
				//
				// final String period = defaults.getPeriod();
				// if( period != null )
				// {
				// final StorePriceBean price =
				// durationSelector.getListModel().getValue(info, period);
				// if( price != null )
				// {
				// durationSelector.setSelectedStringValue(info, period);
				// }
				// else
				// {
				// durationSelector.setSelectedStringValue(info, null);
				// }
				// }
				// }

				int numberOfUsers = defaults.getNumUsers();
				if( numberOfUsers == 0 )
				{
					numberOfUsers = 1;
				}
				final StoreCatalogueItemBean item = ShopItemSectionInfo.getItemInfo(info).getItem();
				final StorePurchaseTierBean purchaseTier = item.getPurchaseTier();
				if( purchaseTier != null && purchaseTier.isPerUser() )
				{
					numberOfUsersPurchase.setValue(info, numberOfUsers);
				}
				final StoreSubscriptionTierBean subscriptionTier = item.getSubscriptionTier();
				if( subscriptionTier != null && subscriptionTier.isPerUser() )
				{
					numberOfUsersSubscribe.setValue(info, numberOfUsers);
				}
			}
			model.setLoaded(true);
		}
	}

	private Label calculateTotalLabel(SectionInfo info, boolean showTax)
	{
		final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
		final StoreCatalogueItemBean item = iinfo.getItem();
		final StoreSubscriptionTierBean subscriptionTier = item.getSubscriptionTier();
		final StorePurchaseTierBean purchaseTier = item.getPurchaseTier();

		Label totalLabel = null;
		if( getModel(info).isMultiplePricingOptions() )
		{
			if( pricingType.getSelectedValue(info) == PricingTypes.SUBSCRIBE )
			{
				totalLabel = getTotalAsLabelFromSubscriptionTier(info, subscriptionTier, showTax);
			}
			else if( pricingType.getSelectedValue(info) == PricingTypes.PURCHASE )
			{
				totalLabel = getTotalAsLabelFromPurchaseTier(info, purchaseTier, showTax);
			}
		}
		else if( subscriptionTier != null )
		{
			totalLabel = getTotalAsLabelFromSubscriptionTier(info, subscriptionTier, showTax);
		}
		else if( purchaseTier != null )
		{
			totalLabel = getTotalAsLabelFromPurchaseTier(info, purchaseTier, showTax);
		}

		return totalLabel;
	}

	private void setUpPurchaseTypeRadios(SectionInfo info)
	{
		final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
		StoreCatalogueItemBean item = iinfo.getItem();
		StatementBlock statements = new StatementBlock();
		boolean updateTotalNeeded = false;
		CombinedDisableable subscribeDisabler;

		PurchaseDetailsDisplayModel model = getModel(info);

		if( item.getPurchaseTier() != null && item.getPurchaseTier().isPerUser() && model.isShoppingCartAccessible() )
		{
			CombinedDisableable purchaseDisabler = new CombinedDisableable(numberOfUsersPurchase, numberOfUsersPurchase);

			FunctionCallStatement purchaseCallStatement = new FunctionCallStatement(
				purchaseDisabler.createDisableFunction(), new NotEqualsExpression(pricingType.createGetExpression(),
					new StringExpression(PricingTypes.PURCHASE.name().toLowerCase())));

			if( pricingType.getListModel().getOption(info, PricingTypes.PURCHASE.name().toLowerCase()) != null )
			{
				statements.addStatements(purchaseCallStatement);
				updateTotalNeeded = true;
			}
		}

		if( item.getSubscriptionTier() != null && item.getSubscriptionTier().isPerUser()
			&& model.isShoppingCartAccessible() )
		{
			subscribeDisabler = new CombinedDisableable(numberOfUsersSubscribe, numberOfUsersSubscribe, startDate,
				durationSelector);
		}
		else
		{
			if( model.isShoppingCartAccessible() )
			{
				subscribeDisabler = new CombinedDisableable(startDate, startDate, durationSelector);
			}
			else
			{
				subscribeDisabler = new CombinedDisableable(durationSelector, durationSelector);
			}
		}

		if( item.getSubscriptionTier() != null )
		{
			CombinedDisableable dateDisabler = new CombinedDisableable(otherDate, otherDate);

			JSExpression otherDateSelected = new NotEqualsExpression(startDate.createGetExpression(),
				new StringExpression(DateTypes.OTHERDATE.name().toLowerCase()));
			JSExpression subscriptionSelected = new NotEqualsExpression(pricingType.createGetExpression(),
				new StringExpression(PricingTypes.SUBSCRIBE.name().toLowerCase()));
			JSExpression orExp = new OrExpression(otherDateSelected, subscriptionSelected);

			FunctionCallStatement dateCallStatement = new FunctionCallStatement(dateDisabler.createDisableFunction(),
				orExp);

			statements.addStatements(dateCallStatement);
		}

		FunctionCallStatement subscribeCallStatement = new FunctionCallStatement(
			subscribeDisabler.createDisableFunction(), new NotEqualsExpression(pricingType.createGetExpression(),
				new StringExpression(PricingTypes.SUBSCRIBE.name().toLowerCase())));

		if( pricingType.getListModel().getOption(info, PricingTypes.SUBSCRIBE.name().toLowerCase()) != null )
		{
			statements.addStatements(subscribeCallStatement);
			updateTotalNeeded = true;
		}

		if( updateTotalNeeded )
		{
			statements.addStatements(updateTotalHandler);
		}

		pricingType.setEventHandler(info, JSHandler.EVENT_CHANGE, new StatementHandler(statements));
	}

	/**
	 * If the only purchases are subscriptions, all of which have expired, then
	 * we don't have anything to show in the purchaser/subscribers' table. (We
	 * do in that case display a simple label which informs the user that all
	 * subscriptions have expired.) Otherwise show outright purchases, and/or
	 * active and future subscriptions.
	 * 
	 * @param info
	 * @param list
	 */
	private void setUpPurchasersTable(SectionInfo info, List<PurchaseItem> list)
	{
		PurchaseDetailsDisplayModel model = getModel(info);
		Date now = new Date();

		// set flags
		boolean subscription = false, purchase = false, purchPerUser = false, activeSubscriptionsExist = false;

		for( PurchaseItem purchItem : list )
		{
			if( purchItem.getSubscriptionEndDate() != null )
			{
				subscription = true;
				if( now.before(purchItem.getSubscriptionEndDate()) )
				{
					// subscription is either current or future
					activeSubscriptionsExist = true;
				}
			}
			else
			{
				purchase = true;
			}
			if( purchItem.getUsers() != 0 )
			{
				if( purchase )
				{
					purchPerUser = true;
				}
			}
		}

		if( subscription && !activeSubscriptionsExist )
		{
			// nothing to show in a table - just show a label to inform that
			// all subscriptions have expired
			model.setPurchasedWarning(LABEL_WARNING_SUB_EXPIRED);
			return;
		}

		// otherwise we're dealing either with a purchase, or with active or
		// future subscription(s) so we have something to populate a table with.
		final TableState state = purchasedBeforeTable.getState(info);
		final TableHeaderRow headerRow = state.addHeaderRow();
		headerRow.addCell(LABEL_HEADER_PURCHASER);
		SectionRenderable dateRenderable;
		Date purchaseDate = new Date();

		// render items
		for( PurchaseItem purchItem : list )
		{
			// ignoring non-active subscriptions. To reach this point when
			// subscription is true, we have determined that activeSubscriptions
			// do exist
			if( subscription && now.after(purchItem.getSubscriptionEndDate()) )
			{
				continue;
			}

			final TableRow row = state.addRow();
			// purchaser
			row.addCell(userLinkSection.createLink(info, purchItem.getPurchase().getCheckoutBy()));
			// sub start / finish or outright purchase
			if( subscription )
			{
				dateRenderable = view.createResultWithModel("shop/date.ftl", purchItem.getSubscriptionStartDate());
				row.addCell(dateRenderable);
				dateRenderable = view.createResultWithModel("shop/date.ftl", purchItem.getSubscriptionEndDate());
				row.addCell(dateRenderable);
			}
			else
			{
				dateRenderable = view.createResultWithModel("shop/date.ftl", purchItem.getPurchase().getPaidDate());
				row.addCell(dateRenderable);
				if( !subscription && !purchPerUser )
				{
					purchaseDate = purchItem.getPurchase().getPaidDate();
				}
				if( subscription )
				{
					row.addCell(new TextLabel("--"));
				}
			}
			// no. user or flat rate
			if( purchItem.getUsers() == 0 )
			{
				row.addCell(LABEL_FLAT_RATE);
			}
			else
			{
				row.addCell(new NumberLabel(purchItem.getUsers()));
			}
		}

		if( subscription && purchase && purchPerUser )
		{
			model.setPurchasedWarning(LABEL_WARNING_PURCHANDSUB);
			headerRow.addCell(LABEL_HEADER_START_AND_PURCHASE);
			headerRow.addCell(LABEL_HEADER_FINISH_DATE);
		}
		else if( purchase && purchPerUser )
		{
			model.setPurchasedWarning(LABEL_WARNING_PURCH_PER_USER);
			headerRow.addCell(LABEL_HEADER_PURCHASE_DATE);
			// If the item has been already bought before and now it is flat
			// rate,then you cannot buy again
			final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
			final StoreCatalogueItemBean item = iinfo.getItem();
			final StorePurchaseTierBean purchaseTier = item.getPurchaseTier();
			if( purchaseTier != null && !purchaseTier.isPerUser() )
			{
				addToCartButton.setDisplayed(info, false);
			}
		}
		else if( purchase )
		{
			model.setPurchasedWarning(LABEL_WARNING_PURCHASED);
			model.setPurchasedOutrightFlatRate(true);
			model.setDateRenderable(view.createResultWithModel("shop/date.ftl", purchaseDate));
			// If the item has been already bought before,then you cannot buy
			// again
			addToCartButton.setDisplayed(info, false);
			pricingType.setDisabled(info, true);
		}
		else if( subscription )
		{
			model.setPurchasedWarning(LABEL_WARNING_SUBSCRIBED);
			headerRow.addCell(LABEL_HEADER_START_DATE);
			headerRow.addCell(LABEL_HEADER_FINISH_DATE);
		}
		headerRow.addCell(LABEL_HEADER_NUM_USERS);
	}

	private void setupPurchaseTier(SectionInfo info, StorePurchaseTierBean purchaseTier, boolean showTax)
	{
		final PurchaseDetailsDisplayModel model = getModel(info);
		final boolean perUser = purchaseTier.isPerUser();
		model.setPurchasePerUser(perUser);

		final StorePriceBean price = purchaseTier.getPrice();
		model.setPurchaseMoneyLabel(moneyLabelService.getLabel(price, 1L, showTax, false));
	}

	private void setupSubscriptionTier(RenderContext info, StoreSubscriptionTierBean subscriptionTier, boolean showTax)
	{
		final PurchaseDetailsDisplayModel model = getModel(info);
		final BooleanListRenderer renderer = (BooleanListRenderer) renderSection(info, durationSelector);
		final List<ListOption<StorePriceBean>> options = renderer.renderOptionList(info);
		final boolean perUser = subscriptionTier.isPerUser();
		model.setSubscribePerUser(perUser);

		final List<TableHeaderCell> columnHeadings = pricingTable.setColumnHeadings(info, "", LABEL_HEADER_DURATION,
			perUser ? LABEL_HEADER_COSTPERUSER : LABEL_HEADER_COST).getCells();
		columnHeadings.get(1).addClass("duration-heading-align");
		columnHeadings.get(2).addClass("cost-heading-align");

		final TableState tableState = pricingTable.getState(info);
		for( ListOption<StorePriceBean> priceListOption : options )
		{
			Option<StorePriceBean> priceOption = priceListOption.getOption();
			StorePriceBean priceBean = priceOption.getObject();
			StoreSubscriptionPeriodBean period = priceBean.getPeriod();
			String name = period.getName();
			TableCell costCell = new TableCell(moneyLabelService.getLabel(priceBean, 1L, showTax, false));
			costCell.addClass("cost-cell-align");

			TableCell durationCell = new TableCell(new LabelRenderer(new TextLabel(name)));
			durationCell.addClass("duration-cell-align");

			if( model.isShoppingCartAccessible() )
			{
				TableCell radioCell = new TableCell(new RadioButtonRenderer(priceListOption.getBooleanState()));
				tableState.addRow(radioCell, durationCell, costCell);
			}
			else
			{
				tableState.addRow("", durationCell, costCell);
			}
		}
	}

	private void loadBothTiers(RenderContext info, StorePurchaseTierBean purchaseTier,
		StoreSubscriptionTierBean subscriptionTier, boolean showTax)
	{
		setupPurchaseTier(info, purchaseTier, showTax);
		setupSubscriptionTier(info, subscriptionTier, showTax);
	}

	@EventHandlerMethod
	public void addToCart(SectionInfo info)
	{
		final PurchaseDetailsDisplayModel model = getModel(info);
		final Map<String, Label> errors = new HashMap<String, Label>();
		final ShopItemSectionInfo item = ShopItemSectionInfo.getItemInfo(info);
		final Store store = item.getStore();

		final OrderItem orderItem = new OrderItem();
		orderItem.setCatUuid(item.getCatUuid());

		final StoreCatalogueItemBean catalogueItem = item.getItem();
		orderItem.setItemUuid(catalogueItem.getUuid());
		orderItem.setItemVersion(catalogueItem.getVersion());
		if( catalogueItem.getName() == null )
		{
			orderItem.setName(catalogueItem.getUuid());
		}
		else
		{
			orderItem.setName(catalogueItem.getName().toString());
		}
		// add a free resource to a shopping cart
		if( catalogueItem.isFree() )
		{
			setPrice(orderItem, null, 0L, false);
		}
		else
		{
			final StorePurchaseTierBean purchaseTier = catalogueItem.getPurchaseTier();
			final StoreSubscriptionTierBean subscriptionTier = catalogueItem.getSubscriptionTier();

			// either the purchase radio is checked or it's purchase only ->
			// otherwise it's subscription
			if( pricingType.getSelectedValue(info) == PricingTypes.PURCHASE
				|| (purchaseTier != null && subscriptionTier == null) )
			{
				// It can't really be
				if( purchaseTier != null )
				{
					orderItem.setPurchaseTierUuid(purchaseTier.getUuid());
					final boolean perUser = purchaseTier.isPerUser();
					orderItem.setPerUser(perUser);
					int numberOfUsers = DEFAULT_USERS_NUMBER;
					if( perUser )
					{
						try
						{
							numberOfUsers = (Integer) numberOfUsersPurchase.getValue(info);
							if( numberOfUsers < 1 )
							{
								throw new NumberFormatException();
							}
						}
						catch( NumberFormatException e )
						{
							errors.put(KEY_INVALID_NUMBER_OUT, LABEL_ERROR_USERS);
						}
					}
					else
					{
						numberOfUsers = 0;
					}
					setPrice(orderItem, purchaseTier.getPrice(), numberOfUsers, perUser);
				}
			}
			else
			{
				// It can't really be
				if( subscriptionTier != null )
				{
					final boolean perUser = subscriptionTier.isPerUser();
					orderItem.setPerUser(perUser);
					int numberOfUsers = DEFAULT_USERS_NUMBER;
					if( perUser )
					{
						try
						{
							numberOfUsers = (Integer) numberOfUsersSubscribe.getValue(info);
							if( numberOfUsers < 1 )
							{
								throw new NumberFormatException();
							}
						}
						catch( NumberFormatException e )
						{
							errors.put(KEY_INVALID_NUMBER_SUB, LABEL_ERROR_USERS);
						}
						orderItem.setUsers(numberOfUsers);
					}
					else
					{
						numberOfUsers = 0;
					}

					final StorePriceBean priceBean = durationSelector.getSelectedValue(info);
					if( priceBean == null )
					{
						errors.put(KEY_NO_SUB_DURATION, LABEL_ERROR_DURATION);
					}
					else
					{
						orderItem.setSubscriptionTierUuid(subscriptionTier.getUuid());
						orderItem.setPeriodUuid(priceBean.getPeriod().getUuid());
						setPrice(orderItem, priceBean, numberOfUsers, perUser);
					}

					if( startDate.getSelectedValue(info) == DateTypes.OTHERDATE )
					{
						// triggers if neither radio is selected, or other w/o a
						// date entered
						final TleDate date = otherDate.getDate(info);
						if( date == null )
						{
							errors.put(KEY_NO_DATE, LABEL_ERROR_DATE);
						}
						else
						{
							orderItem.setSubscriptionStartDate(date.toDate());
						}
					}
				}
			}
		}
		if( errors.isEmpty() )
		{
			try
			{
				orderService.addToShoppingCart(orderService.getShoppingCart(), store, orderItem);
				addDefaultsToSession(orderItem.getSubscriptionStartDate(), orderItem.getUsers(),
					orderItem.getPeriodUuid());
			}
			catch( InvalidDataException e )
			{
				for( ValidationError validationError : e.getErrors() )
				{
					errors.put(validationError.getField(), new KeyLabel(KEY_ERROR_PREFIX + validationError.getKey()));
				}

				model.setErrorMap(errors);
				info.preventGET();
			}
		}
		else
		{
			model.setErrorMap(errors);
			info.preventGET();
		}
	}

	private OrderItem setPrice(OrderItem orderItem, StorePriceBean price, long qty, boolean perUser)
	{
		final long unitPrice = price == null ? 0L : price.getValue().getValue();
		final long unitTax = price == null ? 0L : price.getTaxValue().getValue();
		final String currencyCode = price == null ? null : price.getCurrency();
		final Currency currency = currencyCode == null ? null : Currency.getInstance(currencyCode);

		orderItem.setUnitPrice(unitPrice);
		orderItem.setUnitTax(unitTax);
		orderItem.setUsers((int) qty);
		final long qtyMul = (perUser ? qty : 1L);
		orderItem.setPrice(qtyMul * unitPrice);
		orderItem.setTax(qtyMul * unitTax);
		orderItem.setCurrency(currency);

		final List<StoreTaxBean> taxes = (price == null ? null : price.getTaxes());
		if( taxes != null )
		{
			for( StoreTaxBean tax : taxes )
			{
				// Bit dodge, only accounts for one tax, but that's ok for now
				orderItem.setTaxCode(tax.getCode());
			}
		}
		return orderItem;
	}

	private void addDefaultsToSession(Date subStart, int users, String periodUuid)
	{
		final PriceSelection defaults = new PriceSelection(subStart, periodUuid != null, users, periodUuid);
		userSessionService.setAttribute(ShopConstants.KEY_PRICE_SELECTIONS, defaults);
	}

	@EventHandlerMethod
	public void removeFromCart(SectionInfo info)
	{
		final Order shoppingCart = orderService.getShoppingCart();
		final ShopItemSectionInfo item = ShopItemSectionInfo.getItemInfo(info);
		final Store store = item.getStore();

		final OrderItem orderItem = orderService.getOrderItem(shoppingCart, store, item.getItem().getUuid());
		orderService.removeFromShoppingCart(shoppingCart, store, orderItem);

		final String period = orderItem.getPeriodUuid();
		if( period == null )
		{
			pricingType.setSelectedStringValue(info, PricingTypes.PURCHASE.toString().toLowerCase());
		}
		else
		{
			pricingType.setSelectedStringValue(info, PricingTypes.SUBSCRIBE.toString().toLowerCase());
			final StorePriceBean price = durationSelector.getListModel().getValue(info, period);
			if( price != null )
			{
				durationSelector.setSelectedStringValue(info, period);
			}
			final Date start = orderItem.getSubscriptionStartDate();
			if( start != null )
			{
				otherDate.setDate(info, new UtcDate(start));
				startDate.setSelectedValue(info, DateTypes.OTHERDATE);
			}
		}
		numberOfUsersPurchase.setValue(info, orderItem.getUsers());
		numberOfUsersSubscribe.setValue(info, orderItem.getUsers());
	}

	// For re-subscription
	public void setValues(SectionInfo info, boolean subscription, int users, String period, Date start)
	{
		pricingType.setSelectedStringValue(info, subscription ? PricingTypes.SUBSCRIBE.toString().toLowerCase()
			: PricingTypes.PURCHASE.toString().toLowerCase());
		numberOfUsersPurchase.setValue(info, users);
		numberOfUsersSubscribe.setValue(info, users);
		getModel(info).setLoaded(true);
	}

	/**
	 * AH: hatchet job. Default should always be selected by the control.
	 * 
	 * @param info
	 * @param subscriptionTier
	 * @param iinfo
	 * @return
	 */
	private Label getTotalAsLabelFromSubscriptionTier(SectionInfo info, StoreSubscriptionTierBean subscriptionTier,
		boolean showTax)
	{
		long qty = 1;
		if( subscriptionTier.isPerUser() )
		{
			try
			{
				qty = numberOfUsersSubscribe.getIntValue(info);
			}
			catch( NumberFormatException ex )
			{
				qty = 0;
			}
		}
		final StorePriceBean price = durationSelector.getSelectedValue(info);
		return moneyLabelService.getLabel(price, qty, showTax, true);
	}

	private Label getTotalAsLabelFromPurchaseTier(SectionInfo info, StorePurchaseTierBean purchaseTier, boolean showTax)
	{
		final StorePriceBean price = purchaseTier.getPrice();
		long qty = 1;
		if( purchaseTier.isPerUser() )
		{
			try
			{
				qty = numberOfUsersPurchase.getIntValue(info);
			}
			catch( NumberFormatException ex )
			{
				qty = 0L;
			}
		}
		return moneyLabelService.getLabel(price, qty, showTax, true);
	}

	private class PricingListModel extends DynamicHtmlListModel<PricingTypes>
	{
		@Override
		protected Iterable<PricingTypes> populateModel(SectionInfo info)
		{
			final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
			final StoreCatalogueItemBean item = iinfo.getItem();

			List<PricingTypes> pricingTypes = Lists.newArrayList();

			if( item.isFree() )
			{
				pricingTypes.add(PricingTypes.FREE);
			}
			else
			{
				if( item.getPurchaseTier() != null )
				{
					pricingTypes.add(PricingTypes.PURCHASE);
				}
				if( item.getSubscriptionTier() != null )
				{
					pricingTypes.add(PricingTypes.SUBSCRIBE);
				}
			}

			return pricingTypes;
		}

		@Override
		protected Option<PricingTypes> convertToOption(SectionInfo info, PricingTypes obj)
		{
			return new KeyOption<PricingTypes>(KEY_PRICING_PREFIX + obj.name().toLowerCase(), obj.name().toLowerCase(),
				obj);
		}

		@Override
		public String getDefaultValue(SectionInfo info)
		{
			final PriceSelection defaults = userSessionService.getAttribute(ShopConstants.KEY_PRICE_SELECTIONS);
			if( defaults != null )
			{
				Boolean subscribe = defaults.getSubscription();
				if( subscribe != null && subscribe )
				{
					return PricingTypes.SUBSCRIBE.name().toLowerCase();
				}
			}
			return PricingTypes.PURCHASE.name().toLowerCase();
		}
	}

	private class SubscriptionDurationListModel extends DynamicHtmlListModel<StorePriceBean>
	{
		@Override
		protected Iterable<StorePriceBean> populateModel(SectionInfo info)
		{
			final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
			StoreSubscriptionTierBean subscriptionTier = iinfo.getItem().getSubscriptionTier();

			return subscriptionTier.getPrices();
		}

		@Override
		public String getDefaultValue(SectionInfo info)
		{
			final PriceSelection defaults = userSessionService.getAttribute(ShopConstants.KEY_PRICE_SELECTIONS);
			if( defaults != null )
			{
				final String period = defaults.getPeriod();
				if( period != null )
				{
					// Make sure it's a valid selection
					final StorePriceBean price = durationSelector.getListModel().getValue(info, period);
					if( price != null )
					{
						return period;
					}
				}
			}
			return super.getDefaultValue(info);
		}

		@Override
		protected Option<StorePriceBean> convertToOption(SectionInfo info, StorePriceBean obj)
		{
			return new SimpleOption<StorePriceBean>("", obj.getPeriod().getUuid(), obj);
		}
	}

	public Button getAddToCartButton()
	{
		return addToCartButton;
	}

	public Button getRemoveFromCartButton()
	{
		return removeFromCartButton;
	}

	public Table getPricingTable()
	{
		return pricingTable;
	}

	public NumberField getNumberOfUsersSubscribe()
	{
		return numberOfUsersSubscribe;
	}

	public Calendar getOtherDate()
	{
		return otherDate;
	}

	public SingleSelectionList<DateTypes> getStartDate()
	{
		return startDate;
	}

	public void setOtherDate(Calendar otherDate)
	{
		this.otherDate = otherDate;
	}

	public SingleSelectionList<StorePriceBean> getDurationSelector()
	{
		return durationSelector;
	}

	public SingleSelectionList<PricingTypes> getPricingType()
	{
		return pricingType;
	}

	public NumberField getNumberOfUsersPurchase()
	{
		return numberOfUsersPurchase;
	}

	public Table getPurchasedBeforeTable()
	{
		return purchasedBeforeTable;
	}

	@Override
	public PurchaseDetailsDisplayModel instantiateModel(SectionInfo info)
	{
		return new PurchaseDetailsDisplayModel();
	}

	public static class PurchaseDetailsDisplayModel
	{
		@Bookmarked(name = "l")
		private boolean loaded;

		private boolean subscribePerUser;
		private boolean purchasePerUser;
		private Label totalMoneyLabel;
		private Label purchaseMoneyLabel;
		private Map<String, Label> errorMap;
		private boolean inCart;
		private boolean otherPurchasers;
		private boolean multiplePricingOptions;
		private boolean doDefaultMoneyLabel = true;
		private boolean shoppingCartAccessible;
		private boolean freeItem;
		private Label purchasedWarning;
		private boolean purchasedOutrightFlatRate;
		private SectionRenderable dateRenderable;

		public SectionRenderable getDateRenderable()
		{
			return dateRenderable;
		}

		public void setDateRenderable(SectionRenderable dateRenderable)
		{
			this.dateRenderable = dateRenderable;
		}

		public boolean isDoDefaultMoneyLabel()
		{
			return doDefaultMoneyLabel;
		}

		public void setDoDefaultMoneyLabel(boolean doDefaultMoneyLabel)
		{
			this.doDefaultMoneyLabel = doDefaultMoneyLabel;
		}

		public boolean isMultiplePricingOptions()
		{
			return multiplePricingOptions;
		}

		public void setMultiplePricingOptions(boolean multiplePricingOptions)
		{
			this.multiplePricingOptions = multiplePricingOptions;
		}

		public boolean isOtherPurchasers()
		{
			return otherPurchasers;
		}

		public void setOtherPurchasers(boolean otherPurchasers)
		{
			this.otherPurchasers = otherPurchasers;
		}

		public boolean isInCart()
		{
			return inCart;
		}

		public void setInCart(boolean inCart)
		{
			this.inCart = inCart;
		}

		public Map<String, Label> getErrorMap()
		{
			if( errorMap == null )
			{
				return Collections.emptyMap();
			}
			return errorMap;
		}

		public void setErrorMap(Map<String, Label> errorMap)
		{
			this.errorMap = errorMap;
		}

		public Label getTotalMoneyLabel()
		{
			return totalMoneyLabel;
		}

		public void setTotalMoneyLabel(Label totalMoneyLabel)
		{
			this.totalMoneyLabel = totalMoneyLabel;
		}

		public Label getPurchaseMoneyLabel()
		{
			return purchaseMoneyLabel;
		}

		public void setPurchaseMoneyLabel(Label purchaseMoneyLabel)
		{
			this.purchaseMoneyLabel = purchaseMoneyLabel;
		}

		public boolean isSubscribePerUser()
		{
			return subscribePerUser;
		}

		public void setSubscribePerUser(boolean subscribePerUser)
		{
			this.subscribePerUser = subscribePerUser;
		}

		public boolean isPurchasePerUser()
		{
			return purchasePerUser;
		}

		public void setPurchasePerUser(boolean purchasePerUser)
		{
			this.purchasePerUser = purchasePerUser;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}

		public boolean isShoppingCartAccessible()
		{
			return shoppingCartAccessible;
		}

		public void setShoppingCartAccessible(boolean shoppingCartAccessible)
		{
			this.shoppingCartAccessible = shoppingCartAccessible;
		}

		public void setFreeItem(boolean isFreeItem)
		{
			this.freeItem = isFreeItem;
		}

		public boolean isFreeItem()
		{
			return freeItem;
		}

		public Label getPurchasedWarning()
		{
			return purchasedWarning;
		}

		public void setPurchasedWarning(Label purchasedWarning)
		{
			this.purchasedWarning = purchasedWarning;
		}

		public boolean isPurchasedOutrightFlatRate()
		{
			return purchasedOutrightFlatRate;
		}

		public void setPurchasedOutrightFlatRate(boolean purchasedOutrightFlatRate)
		{
			this.purchasedOutrightFlatRate = purchasedOutrightFlatRate;
		}
	}
}
