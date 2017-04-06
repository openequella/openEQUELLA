package com.tle.web.payment.shop.section.cart;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.NotFoundException;
import com.dytech.edge.exceptions.WebException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.Pair;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.Order.Status;
import com.tle.common.payment.storefront.entity.OrderHistory;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StorePaymentGatewayBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.storefront.CurrencyTotal;
import com.tle.core.payment.storefront.OrderWorkflowInfo;
import com.tle.core.payment.storefront.StoreTotal;
import com.tle.core.payment.storefront.TaxTotal;
import com.tle.core.payment.storefront.exception.OrderValueChangedException;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.services.UrlService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.viewitem.RootShopViewItemSection;
import com.tle.web.payment.shop.service.ShopMoneyLabelService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.equella.render.UnselectLinkRenderer;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@TreeIndexed
public class ShopViewCartSection extends AbstractPrototypeSection<ShopViewCartSection.ShopViewCartModel>
	implements
		HtmlRenderer
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(ShopViewCartSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/viewcart.js"));
	private static final JSCallable FUNCTION_DISABLE_REJECT = new ExternallyDefinedFunction("disableReject", INCLUDE);

	@PlugKey("shop.search.breadcrumb.stores")
	private static Label LABEL_BACK_TO_STORES;
	@PlugKey("shop.viewcart.title.cart")
	private static Label LABEL_TITLE_CART;
	@PlugKey("shop.viewcart.title.approve")
	private static Label LABEL_TITLE_APPROVE;
	@PlugKey("shop.viewcart.title.pay")
	private static Label LABEL_TITLE_PAY;
	@PlugKey("shop.viewcart.title.order")
	private static Label LABEL_TITLE_ORDER;
	@PlugKey("shop.viewcart.label.zeroitems")
	private static Label LABEL_ZERO_ITEMS;
	@PlugKey("shop.viewcart.label.multicurrency")
	private static Label LABEL_MULTIPLE_CURRENCY;
	@PlugKey("shop.viewcart.label.paymentdate")
	private static Label LABEL_PAYMENT_DATE;
	@PlugKey("shop.viewcart.label.flatrate")
	private static Label LABEL_FLAT_RATE;

	@PlugKey("shop.viewcart.column.resource")
	private static Label LABEL_COLUMN_RESOURCE;
	@PlugKey("shop.viewcart.column.durationprice")
	private static Label LABEL_COLUMN_DURATION_PRICE;
	@PlugKey("shop.viewcart.column.price")
	private static Label LABEL_COLUMN_PRICE;
	@PlugKey("shop.viewcart.column.startdate")
	private static Label LABEL_COLUMN_START_DATE;
	@PlugKey("shop.viewcart.column.numusers")
	private static Label LABEL_COLUMN_NUM_USERS;
	@PlugKey("shop.viewcart.column.total")
	private static Label LABEL_COLUMN_TOTAL;
	@PlugKey("shop.viewcart.confirm.removeall")
	private static Label LABEL_CONFIRM_REMOVEALL;
	@PlugKey("shop.viewcart.button.removeall")
	private static Label LABEL_BUTTON_REMOVEALL;
	@PlugKey("shop.viewcart.confirm.removerow")
	private static Label LABEL_CONFIRM_REMOVE_ROW;
	@PlugKey("shop.viewcart.button.removerow")
	private static Label LABEL_BUTTON_REMOVE_ROW;
	@PlugKey("shop.viewcart.button.paywith")
	private static String KEY_BUTTON_PAYWITH;
	@PlugKey("shop.viewcart.button.pay.free")
	private static Label LABEL_BUTTON_FREE_PAY;
	@PlugKey("shop.viewcart.label.storestatus.paid")
	private static Label LABEL_STORE_STATUS_PAID;
	@PlugKey("shop.viewcart.label.storestatus.paid.free")
	private static Label LABEL_STORE_STATUS_PAID_FREE;
	@PlugKey("shop.viewcart.label.storestatus.pending")
	private static Label LABEL_STORE_STATUS_PENDING;
	@PlugKey("shop.viewcart.label.storestatus.submitted")
	private static Label LABEL_STORE_STATUS_SUBMITTED;
	@PlugKey("shop.viewcart.confirm.redraft")
	private static Label LABEL_CONFIRM_REDRAFT;
	@PlugKey("shop.viewcart.confirm.delete")
	private static Label LABEL_CONFIRM_DELETE;
	@PlugKey("shop.viewcart.label.includestax")
	private static Label LABEL_INCLUDES_TAX;
	@PlugKey("shop.viewcart.label.plustax")
	private static Label LABEL_PLUS_TAX;

	@PlugKey("shop.viewcart.error.invalidfield")
	private static Label LABEL_ERROR_INVALID_FIELD;
	@PlugKey("shop.viewcart.error.comment")
	private static Label LABEL_ERROR_COMMENT;
	@PlugKey("shop.viewcart.error.cannotview")
	private static Label LABEL_ERROR_CANNOTVIEW;
	@PlugKey("shop.viewcart.error.price.changed")
	private static Label LABEL_ERROR_PRICE_CHANGED;
	@PlugKey("shop.viewcart.error.invalidselections")
	private static Label LABEL_ERROR_INVALID_SELECTIONS;

	@PlugKey("shop.viewcart.button.submitapproval")
	@Component
	private Button submitButton;
	@PlugKey("shop.viewcart.button.submitpayment")
	@Component
	private Button approveButton;
	@PlugKey("shop.viewcart.button.reject")
	@Component
	private Button rejectButton;
	@PlugKey("shop.viewcart.button.redraft")
	@Component
	private Button redraftButton;
	@PlugKey("shop.viewcart.button.delete")
	@Component
	private Button deleteButton;

	@Component(name = "c", stateful = false)
	private TextField comment;

	@Inject
	private StoreService storeService;
	@Inject
	private ShopService shopService;
	@Inject
	private OrderService orderService;
	@Inject
	private UrlService urlService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private ShopMoneyLabelService moneyLabelService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ShopViewCartModel model = getModel(context);
		final Order order = getOrder(context);

		if( !orderService.canView(order) )
		{
			throw new AccessDeniedException(LABEL_ERROR_CANNOTVIEW.getText());
		}

		HtmlLinkState breadcrumb = new HtmlLinkState(LABEL_BACK_TO_STORES);
		breadcrumb.setClickHandler(events.getNamedHandler("backToStoresPage"));
		Breadcrumbs.get(context).add(breadcrumb);

		final OrderWorkflowInfo winfo = orderService.getOrderProcess(order);
		model.setHeading(setupTitle(context, winfo));
		model.setCart(winfo.isCart());
		model.setSubmitting(winfo.isSubmitApprove());
		model.setApproving(winfo.isSubmitPay());
		model.setPaying(winfo.isPay());
		model.setRejecting(winfo.isReject());
		if( winfo.isRedraft() )
		{
			final boolean cartEmpty = orderService.isEmpty(orderService.getShoppingCart());

			final SubmitValuesHandler redraft = events.getNamedHandler("redraft");
			if( !cartEmpty )
			{
				redraft.addValidator(Js.confirm(LABEL_CONFIRM_REDRAFT));
			}
			redraftButton.setClickHandler(context, redraft);
			model.setRedrafting(winfo.isRedraft());
		}

		boolean errored = false;

		final List<ShopViewCartStoreDisplayModel> storeDisplays = Lists.newArrayList();
		for( OrderStorePart storePart : order.getStoreParts() )
		{
			final Store store = storePart.getStore();

			Exception error = null;
			try
			{
				final StoreCheckoutBean scb = storePart.getCheckoutUuid() == null ? null : shopService.getCheckout(
					store, storePart.getCheckoutUuid());

				// Not the ideal place to do this..
				final StoreCheckoutBean.PaidStatus status = scb == null ? null : scb.getPaidStatus();
				if( status == StoreCheckoutBean.PaidStatus.PAID && order.getStatus() == Status.PENDING )
				{
					orderService.startCheckCurrentOrders(true);
				}

				final StorePricingInformationBean pricing = shopService.getPricingInformation(store, true);
				final StoreTotal storeTotal = orderService.calculateStoreTotal(order, store, pricing);
				errored |= storeTotal.isErrored();
				final ShopViewCartStoreDisplayModel storeModel = setupStoreModel(context, storePart, winfo, status,
					pricing, storeTotal);
				storeDisplays.add(storeModel);
			}
			catch( NotFoundException e )
			{
				error = e;
			}
			catch( AccessDeniedException e )
			{
				error = e;
			}
			catch( WebException e )
			{
				error = e;
			}
			if( error != null )
			{
				final ShopViewCartStoreDisplayModel storeModel = new ShopViewCartStoreDisplayModel();
				storeModel.setErrored(true);
				final Label storeTitle = new TextLabel(CurrentLocale.get(store.getName()));
				storeModel.setTitle(storeTitle);
				storeDisplays.add(storeModel);
			}
		}
		sortViaStoreName(storeDisplays);

		if( errored )
		{
			model.setErrorMessage(LABEL_ERROR_INVALID_SELECTIONS);
		}

		final boolean empty = orderService.isEmpty(order);
		model.setEmpty(empty);
		model.setStores(storeDisplays);
		model.setHistory(setupHistory(context, order));
		final Pair<Label, List<TaxDisplayModel>> total = setupTotal(order, empty);
		model.setTotalLabel(total.getFirst());
		model.setTotalTax(total.getSecond());

		return view.createResult("shop/viewcart.ftl", context);
	}

	@EventHandlerMethod
	public void backToStoresPage(SectionInfo info)
	{
		final SectionInfo fwd = info.createForward(ShopConstants.URL_SHOPS);
		info.forward(fwd);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		submitButton.setClickHandler(events.getNamedHandler("submit"));
		approveButton.setClickHandler(events.getNamedHandler("approve"));
		rejectButton.setClickHandler(events.getNamedHandler("reject").addValidator(
			comment.createNotBlankValidator().setFailureStatements(Js.alert_s(LABEL_ERROR_COMMENT))));
		userLinkSection = userLinkService.register(tree, id);

		final JSHandler disableReject = Js.handler((JSCallable) CallAndReferenceFunction.get(
			Js.function(Js.call_s(FUNCTION_DISABLE_REJECT, Jq.$(comment), Jq.$(rejectButton))), rejectButton));
		comment.setEventHandler("keyup", disableReject);
		rejectButton.addReadyStatements(disableReject);
		deleteButton.setClickHandler(events.getNamedHandler("delete").addValidator(Js.confirm(LABEL_CONFIRM_DELETE)));
	}

	private Label setupTitle(SectionInfo info, OrderWorkflowInfo winfo)
	{
		final Label title;
		if( winfo.isCart() )
		{
			title = LABEL_TITLE_CART;
		}
		else if( winfo.isSubmitPay() )
		{
			title = LABEL_TITLE_APPROVE;
		}
		else if( winfo.isPay() )
		{
			title = LABEL_TITLE_PAY;
		}
		else
		{
			title = LABEL_TITLE_ORDER;
		}
		Decorations.setTitle(info, title);
		return title;
	}

	private ShopViewCartStoreDisplayModel setupStoreModel(SectionInfo info, OrderStorePart storePart,
		OrderWorkflowInfo winfo, StoreCheckoutBean.PaidStatus status, StorePricingInformationBean pricing,
		StoreTotal storeTotal)
	{
		final Store store = storePart.getStore();
		final StoreBean storeBean = shopService.getStoreInformation(store, winfo.isPay());

		final boolean paid = (status == StoreCheckoutBean.PaidStatus.PAID);
		final boolean pending = (status == StoreCheckoutBean.PaidStatus.PENDING);
		final boolean submitted = (status == StoreCheckoutBean.PaidStatus.SUBMITTED);
		final boolean readOnly = paid || pending || !winfo.isCart();

		final ShopViewCartStoreDisplayModel storeModel = new ShopViewCartStoreDisplayModel();
		final Label storeTitle = new TextLabel(storeBean.getName().toString());
		storeModel.setTitle(storeTitle);
		storeModel.setIcon(new ImageRenderer(storeBean.getIcon(), storeTitle));
		storeModel.setReadOnly(readOnly);

		if( !readOnly )
		{
			HtmlComponentState removeAllButton = new HtmlComponentState(LABEL_BUTTON_REMOVEALL, events.getNamedHandler(
				"removeAll", storePart.getStore().getUuid()).addValidator(new Confirm(LABEL_CONFIRM_REMOVEALL)));
			storeModel.setRemoveAll(new ButtonRenderer(removeAllButton).showAs(ButtonType.DELETE));
		}
		storeModel.setTable(setupTableState(info, storePart, pricing, readOnly, storeTotal.getErrors()));
		storeModel.setEmpty(storeModel.getTable().getRows().size() == 0);

		final BigDecimal value = storeTotal.getValue();

		final List<ShopViewCartGatewayDisplayModel> gateways = Lists.newArrayList();

		if( !storeTotal.isErrored() )
		{
			if( winfo.isPay() && !paid && !pending )
			{
				if( isZero(value) )
				{
					final ShopViewCartGatewayDisplayModel freeGateway = new ShopViewCartGatewayDisplayModel();
					freeGateway.setCheckoutLink(new HtmlLinkState(LABEL_BUTTON_FREE_PAY, events.getNamedHandler("pay",
						storePart.getStore().getUuid(), "free")));
					gateways.add(freeGateway);
				}
				else if( winfo.isPay() )
				{
					List<StorePaymentGatewayBean> paymentGateways = Lists.newArrayList(pricing.getPaymentGateways());
					Collections.sort(paymentGateways, new NumberStringComparator<StorePaymentGatewayBean>()
					{
						@Override
						public String convertToString(StorePaymentGatewayBean g)
						{
							return g.getGatewayType();
						}
					});
					for( StorePaymentGatewayBean gateway : paymentGateways )
					{
						final ShopViewCartGatewayDisplayModel gw = new ShopViewCartGatewayDisplayModel();
						gw.setCheckoutImage(new ImageRenderer(gateway.getButtonUrl(), new KeyLabel(KEY_BUTTON_PAYWITH,
							gateway.getName().toString())));
						gw.setCheckoutLink(new HtmlLinkState(events.getNamedHandler("pay", storePart.getStore()
							.getUuid(), gateway.getUuid())));
						gateways.add(gw);
					}
				}
			}
		}
		storeModel.setGateways(gateways);

		if( submitted || paid || pending )
		{
			storeModel.setStatus(paid ? (isZero(value) ? LABEL_STORE_STATUS_PAID_FREE : LABEL_STORE_STATUS_PAID)
				: (pending ? LABEL_STORE_STATUS_PENDING : LABEL_STORE_STATUS_SUBMITTED));
		}

		final ShopViewCartModel model = getModel(info);

		final boolean rejecting = model.isRejecting();
		model.setRejecting(rejecting && !(paid || pending));

		final BigDecimal taxValue = storeTotal.getTaxValue();
		storeModel.setTotal(moneyLabelService.getLabel(value, taxValue, storeTotal.getCurrency()));
		if( !isZero(taxValue) )
		{
			final TaxDisplayModel tax = new TaxDisplayModel();
			tax.setModifier(moneyLabelService.isShowTax() ? LABEL_INCLUDES_TAX : LABEL_PLUS_TAX);
			tax.setAmount(moneyLabelService.getLabel(taxValue, null, storeTotal.getCurrency()));
			// Bit dodgy
			if( storeTotal.getTaxCodes().size() > 0 )
			{
				tax.setCode(new TextLabel(storeTotal.getTaxCodes().iterator().next()));
			}
			storeModel.setTax(tax);
		}
		return storeModel;
	}

	private boolean isZero(BigDecimal bd)
	{
		return (bd.compareTo(BigDecimal.ZERO) == 0);
	}

	private TableState setupTableState(SectionInfo info, OrderStorePart storePart, StorePricingInformationBean pricing,
		boolean readOnly, Map<String, ValidationError> errors)
	{
		// Need to determine which columns to show
		boolean showUsers = false;
		boolean showStartDate = false;
		for( OrderItem item : storePart.getOrderItems() )
		{
			if( item.getSubscriptionTierUuid() != null )
			{
				showStartDate = true;
				if( pricing.isSubscriptionPerUser() )
				{
					showUsers = true;
				}
			}
			else if( item.getPurchaseTierUuid() != null && pricing.isPurchasePerUser() )
			{
				showUsers = true;
			}
		}

		final List<Label> columnHeadings = Lists.newArrayList();
		final List<Sort> columnSorts = Lists.newArrayList();
		columnHeadings.add(LABEL_COLUMN_RESOURCE);
		columnSorts.add(Sort.PRIMARY_ASC);
		columnHeadings.add(showStartDate ? LABEL_COLUMN_DURATION_PRICE : LABEL_COLUMN_PRICE);
		columnSorts.add(Sort.SORTABLE_ASC);
		if( showStartDate )
		{
			columnHeadings.add(LABEL_COLUMN_START_DATE);
			columnSorts.add(Sort.SORTABLE_ASC);
		}
		if( showUsers )
		{
			columnHeadings.add(LABEL_COLUMN_NUM_USERS);
			columnSorts.add(Sort.SORTABLE_ASC);
		}
		columnHeadings.add(LABEL_COLUMN_TOTAL);
		columnSorts.add(Sort.SORTABLE_ASC);
		// delete column
		columnHeadings.add(null);

		final TableState tableState = new TableState();
		tableState.setFilterable(false);
		tableState.setColumnHeadings(columnHeadings.toArray());
		tableState.setColumnSorts(columnSorts.toArray(new Sort[columnSorts.size()]));

		for( OrderItem item : storePart.getOrderItems() )
		{
			final TableRow row = tableState.addRow();

			final ValidationError error = errors.get(item.getUuid());
			if( error != null )
			{
				row.addClass("error");
			}

			final SectionInfo fwd = info.createForward(ShopConstants.URL_VIEWITEM);
			final RootShopViewItemSection viewItem = fwd.lookupSection(RootShopViewItemSection.class);
			viewItem.setItem(fwd, storePart.getStore().getUuid(), item.getCatUuid(), item.getItemUuid());
			final HtmlLinkState itemLink = new HtmlLinkState(new InfoBookmark(fwd));
			itemLink.setLabel(new TextLabel(item.getName()));

			TableCell cell = new TableCell(new DivRenderer("namecol wrapped", new LinkRenderer(itemLink)));

			cell.setSortData(item.getName());
			row.addCell(cell);

			final String subscriptionTierUuid = item.getSubscriptionTierUuid();
			final boolean perUser = item.isPerUser();

			final Currency currency = item.getCurrency();
			final long unitPrice = item.getUnitPrice();
			final long unitTax = item.getUnitTax();

			final Label unitPriceLabel;
			if( subscriptionTierUuid != null )
			{
				final String periodUuid = item.getPeriodUuid();
				final StoreSubscriptionPeriodBean subscriptionPeriod = pricing.getSubscriptionPeriod(periodUuid);
				// it can happen (see #7025)
				if( subscriptionPeriod == null )
				{
					unitPriceLabel = LABEL_ERROR_INVALID_FIELD;
				}
				else
				{
					// TODO: remove evil appended label
					unitPriceLabel = AppendedLabel.get(
						new TextLabel(LangUtils.getClosestObjectForLocale(subscriptionPeriod.getNameStrings(),
							CurrentLocale.getLocale())), moneyLabelService.getLabel(unitPrice, unitTax, currency),
						TextLabel.SPACE);
				}
			}
			else
			{
				unitPriceLabel = moneyLabelService.getLabel(unitPrice, unitTax, currency);
			}
			cell = new TableCell(unitPriceLabel);
			cell.setSortData(unitPrice);
			row.addCell(cell);

			if( showStartDate )
			{
				final SectionRenderable startDateRenderable;
				if( subscriptionTierUuid != null )
				{
					final Date subscriptionStartDate = item.getSubscriptionStartDate();
					if( subscriptionStartDate == null )
					{
						startDateRenderable = new LabelRenderer(LABEL_PAYMENT_DATE);
					}
					else
					{
						startDateRenderable = view.createResultWithModel("shop/date.ftl", subscriptionStartDate);
					}
				}
				else
				{
					startDateRenderable = null;
				}
				row.addCell(startDateRenderable);
			}

			if( showUsers )
			{
				if( perUser )
				{
					cell = new TableCell(new TextLabel(Integer.toString(item.getUsers())));
					cell.setSortData(item.getUsers());
				}
				else
				{
					cell = new TableCell(LABEL_FLAT_RATE);
					cell.setSortData(0);
				}
				row.addCell(cell);
			}

			final long price = item.getPrice();
			final long tax = item.getTax();
			cell = new TableCell(moneyLabelService.getLabel(price, tax, currency));
			cell.setSortData(price);
			row.addCell(cell);

			if( !readOnly )
			{
				final HtmlLinkState actionLink = new HtmlLinkState(events.getNamedHandler("removeRow",
					storePart.getStore().getUuid(), item.getItemUuid()).addValidator(
					new Confirm(LABEL_CONFIRM_REMOVE_ROW)));
				TableCell actionCell = new TableCell(new UnselectLinkRenderer(actionLink, LABEL_BUTTON_REMOVE_ROW));
				actionCell.addClass("actions");
				row.addCell(actionCell);
			}
			else
			{
				row.addCell(new TableCell());
			}
		}
		return tableState;
	}

	private List<ShopViewCartHistoryDisplayModel> setupHistory(SectionInfo info, Order order)
	{
		final List<ShopViewCartHistoryDisplayModel> history = Lists.newArrayList();
		for( OrderHistory historyItem : Lists.reverse(order.getHistory()) )
		{
			final ShopViewCartHistoryDisplayModel cd = new ShopViewCartHistoryDisplayModel();
			cd.setComment(historyItem.getComment());
			cd.setDate(historyItem.getDate());
			cd.setUserLink(userLinkSection.createLink(info, historyItem.getUserId()));
			cd.setReason(historyItem.getStatus().toString().toLowerCase());
			history.add(cd);
		}
		return history;
	}

	private Pair<Label, List<TaxDisplayModel>> setupTotal(Order order, boolean empty)
	{
		final Map<Currency, CurrencyTotal> currencyTotals = orderService.calculateCurrencyTotals(order);
		final Set<Currency> keySet = currencyTotals.keySet();
		final int currencies = keySet.size();

		final Label totalLabel;
		final List<TaxDisplayModel> taxes = Lists.newArrayList();

		if( empty )
		{
			totalLabel = LABEL_ZERO_ITEMS;
		}
		else if( currencies > 1 )
		{
			totalLabel = LABEL_MULTIPLE_CURRENCY;
		}
		else
		{
			if( currencies == 0 )
			{
				totalLabel = moneyLabelService.getLabel(0L, 0L, null);
			}
			else
			{
				final Currency currency = keySet.iterator().next();
				final CurrencyTotal currencyTotal = currencyTotals.get(currency);
				totalLabel = moneyLabelService.getLabel(currencyTotal.getValue(), currencyTotal.getCombinedTaxTotal(),
					currency);
				// FIXME: add taxes
				for( TaxTotal taxTotal : currencyTotal.getTaxes() )
				{
					final TaxDisplayModel tax = new TaxDisplayModel();
					tax.setModifier(moneyLabelService.isShowTax() ? LABEL_INCLUDES_TAX : LABEL_PLUS_TAX);
					tax.setCode(new TextLabel(taxTotal.getCode()));
					tax.setAmount(moneyLabelService.getLabel(taxTotal.getValue(), null, currency));
					taxes.add(tax);
				}
			}
		}
		return new Pair<Label, List<TaxDisplayModel>>(totalLabel, taxes);
	}

	private Order getOrder(SectionInfo info)
	{
		final ShopViewCartModel model = getModel(info);
		final String orderUuid = model.getOrderUuid();
		return (orderUuid == null ? orderService.getShoppingCart() : orderService.getByUuid(orderUuid));
	}

	@EventHandlerMethod
	public void removeRow(SectionInfo info, String storeUuid, String itemUuid)
	{
		final Order order = getOrder(info);
		final Store store = storeService.getByUuid(storeUuid);
		orderService.removeFromShoppingCart(order, store, orderService.getOrderItem(order, store, itemUuid));
	}

	@EventHandlerMethod
	public void removeAll(SectionInfo info, String storeUuid)
	{
		final Order order = orderService.getShoppingCart();
		orderService.removeStoreFromShoppingCart(order, storeService.getByUuid(storeUuid));
	}

	@EventHandlerMethod
	public void submit(SectionInfo info)
	{
		final Order order = getOrder(info);
		if( order.getStatus() == Status.CART )
		{
			orderService.submitShoppingCart(order, comment.getValue(info));
		}
		else
		{
			orderService.submitOrder(order, comment.getValue(info));
		}
	}

	@EventHandlerMethod
	public void approve(SectionInfo info)
	{
		final Order order = getOrder(info);
		orderService.submitOrder(order, comment.getValue(info));
		checkRedirect(info, order);
	}

	@EventHandlerMethod
	public void reject(SectionInfo info)
	{
		final Order order = getOrder(info);

		final String commentText = comment.getValue(info);
		if( Strings.isNullOrEmpty(commentText) )
		{
			return;
		}

		orderService.rejectOrder(order, commentText);
		checkRedirect(info, order);
	}

	@EventHandlerMethod
	public void redraft(SectionInfo info)
	{
		orderService.redraftOrder(getOrder(info));
	}

	@EventHandlerMethod
	public void delete(SectionInfo info)
	{
		orderService.deleteOrder(getOrder(info));
		backToStoresPage(info);
	}

	@EventHandlerMethod
	public void pay(SectionInfo info, String storeUuid, String gatewayUuid)
	{
		final Store store = storeService.getByUuid(storeUuid);
		final Order order = getOrder(info);

		final StoreCheckoutBean checkout;
		try
		{
			// Submit to the Store backend as a checkout
			checkout = orderService.submitToStore(order, store);
		}
		catch( OrderValueChangedException e )
		{
			getModel(info).setErrorMessage(LABEL_ERROR_PRICE_CHANGED);
			info.preventGET();
			return;
		}

		// Free checkouts are automatically marked as paid
		if( checkout.getPaidStatus() != StoreCheckoutBean.PaidStatus.PAID )
		{
			final StorePaymentGatewayBean gateway = shopService.getPaymentGateway(store, gatewayUuid, true);

			// build a return URL to come back to
			getModel(info).setOrderUuid(order.getUuid());
			final String returnUrl = urlService.institutionalise(new InfoBookmark(info).getHref());

			// Forward to gateway (see PaypalExpressCheckoutSection for params)
			final Map<String, String> params = Maps.newHashMap();
			params.put("gatewayUuid", gatewayUuid);
			params.put("checkoutUuid", checkout.getUuid());
			params.put("returnUrl", returnUrl);

			String url = URLUtils.appendQueryString(gateway.getCheckoutUrl(), URLUtils.getParameterString(params));
			info.forwardToUrl(url);
		}
		else
		{
			checkRedirect(info, order);
		}
	}

	private void checkRedirect(SectionInfo info, Order order)
	{
		if( !orderService.canView(order) )
		{
			backToStoresPage(info);
		}
	}

	private void sortViaStoreName(List<ShopViewCartStoreDisplayModel> storeDisplays)
	{
		Collections.sort(storeDisplays, new Comparator<ShopViewCartStoreDisplayModel>()
		{
			@Override
			public int compare(ShopViewCartStoreDisplayModel m1, ShopViewCartStoreDisplayModel m2)
			{
				return getStoreName(m1).compareToIgnoreCase(getStoreName(m2));
			}

			private String getStoreName(ShopViewCartStoreDisplayModel model)
			{
				return model.getTitle().getText();
			}
		});
	}

	public void setOrderUuid(SectionInfo info, String orderUuid)
	{
		getModel(info).setOrderUuid(orderUuid);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShopViewCartModel();
	}

	public Button getSubmitButton()
	{
		return submitButton;
	}

	public Button getApproveButton()
	{
		return approveButton;
	}

	public Button getRejectButton()
	{
		return rejectButton;
	}

	public Button getRedraftButton()
	{
		return redraftButton;
	}

	public Button getDeleteButton()
	{
		return deleteButton;
	}

	public TextField getComment()
	{
		return comment;
	}

	public static class ShopViewCartGatewayDisplayModel
	{
		private ImageRenderer checkoutImage;
		private HtmlComponentState checkoutLink;

		public ImageRenderer getCheckoutImage()
		{
			return checkoutImage;
		}

		public void setCheckoutImage(ImageRenderer checkoutImage)
		{
			this.checkoutImage = checkoutImage;
		}

		public HtmlComponentState getCheckoutLink()
		{
			return checkoutLink;
		}

		public void setCheckoutLink(HtmlComponentState checkoutLink)
		{
			this.checkoutLink = checkoutLink;
		}
	}

	public static class ShopViewCartStoreDisplayModel
	{
		private Label title;
		private TableState table;
		private Label total;
		private TaxDisplayModel tax;
		private SectionRenderable removeAll;
		private List<ShopViewCartGatewayDisplayModel> gateways;
		private Label status;
		private boolean readOnly;
		private boolean empty;
		private SectionRenderable icon;
		private boolean errored;

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public TableState getTable()
		{
			return table;
		}

		public void setTable(TableState table)
		{
			this.table = table;
		}

		public Label getTotal()
		{
			return total;
		}

		public void setTotal(Label total)
		{
			this.total = total;
		}

		public TaxDisplayModel getTax()
		{
			return tax;
		}

		public void setTax(TaxDisplayModel tax)
		{
			this.tax = tax;
		}

		public SectionRenderable getRemoveAll()
		{
			return removeAll;
		}

		public void setRemoveAll(SectionRenderable removeAll)
		{
			this.removeAll = removeAll;
		}

		public List<ShopViewCartGatewayDisplayModel> getGateways()
		{
			return gateways;
		}

		public void setGateways(List<ShopViewCartGatewayDisplayModel> gateways)
		{
			this.gateways = gateways;
		}

		public Label getStatus()
		{
			return status;
		}

		public void setStatus(Label status)
		{
			this.status = status;
		}

		public boolean isReadOnly()
		{
			return readOnly;
		}

		public void setReadOnly(boolean readOnly)
		{
			this.readOnly = readOnly;
		}

		public boolean isEmpty()
		{
			return empty;
		}

		public void setEmpty(boolean empty)
		{
			this.empty = empty;
		}

		public SectionRenderable getIcon()
		{
			return icon;
		}

		public void setIcon(SectionRenderable icon)
		{
			this.icon = icon;
		}

		public boolean isErrored()
		{
			return errored;
		}

		public void setErrored(boolean errored)
		{
			this.errored = errored;
		}
	}

	public static class TaxDisplayModel
	{
		private Label amount;
		/**
		 * Either 'includes' or 'plus'
		 */
		private Label modifier;
		private Label code;

		public Label getAmount()
		{
			return amount;
		}

		public void setAmount(Label amount)
		{
			this.amount = amount;
		}

		public Label getModifier()
		{
			return modifier;
		}

		public void setModifier(Label modifier)
		{
			this.modifier = modifier;
		}

		public Label getCode()
		{
			return code;
		}

		public void setCode(Label code)
		{
			this.code = code;
		}
	}

	public static class ShopViewCartHistoryDisplayModel
	{
		private HtmlLinkState userLink;
		private Date date;
		private String comment;
		private String reason;

		public HtmlLinkState getUserLink()
		{
			return userLink;
		}

		public void setUserLink(HtmlLinkState userLink)
		{
			this.userLink = userLink;
		}

		public Date getDate()
		{
			return date;
		}

		public void setDate(Date date)
		{
			this.date = date;
		}

		public String getComment()
		{
			return comment;
		}

		public void setComment(String comment)
		{
			this.comment = comment;
		}

		public String getReason()
		{
			return reason;
		}

		public void setReason(String reason)
		{
			this.reason = reason;
		}
	}

	public static class ShopViewCartModel
	{
		@Bookmarked(name = "o")
		private String orderUuid;
		private boolean empty;
		private List<ShopViewCartStoreDisplayModel> stores;
		private List<ShopViewCartHistoryDisplayModel> history;
		private Label totalLabel;
		private List<TaxDisplayModel> totalTax;
		private boolean cart;
		private boolean paying;
		private boolean submitting;
		private boolean approving;
		private boolean rejecting;
		private boolean redrafting;
		private Label errorMessage;
		private Label heading;

		public String getOrderUuid()
		{
			return orderUuid;
		}

		public void setOrderUuid(String orderUuid)
		{
			this.orderUuid = orderUuid;
		}

		public boolean isEmpty()
		{
			return empty;
		}

		public void setEmpty(boolean empty)
		{
			this.empty = empty;
		}

		public List<ShopViewCartStoreDisplayModel> getStores()
		{
			return stores;
		}

		public void setStores(List<ShopViewCartStoreDisplayModel> stores)
		{
			this.stores = stores;
		}

		public List<ShopViewCartHistoryDisplayModel> getHistory()
		{
			return history;
		}

		public void setHistory(List<ShopViewCartHistoryDisplayModel> history)
		{
			this.history = history;
		}

		public Label getTotalLabel()
		{
			return totalLabel;
		}

		public void setTotalLabel(Label totalLabel)
		{
			this.totalLabel = totalLabel;
		}

		public List<TaxDisplayModel> getTotalTax()
		{
			return totalTax;
		}

		public void setTotalTax(List<TaxDisplayModel> totalTax)
		{
			this.totalTax = totalTax;
		}

		public boolean isCart()
		{
			return cart;
		}

		public void setCart(boolean cart)
		{
			this.cart = cart;
		}

		public boolean isPaying()
		{
			return paying;
		}

		public void setPaying(boolean paying)
		{
			this.paying = paying;
		}

		public boolean isSubmitting()
		{
			return submitting;
		}

		public void setSubmitting(boolean submitting)
		{
			this.submitting = submitting;
		}

		public boolean isApproving()
		{
			return approving;
		}

		public void setApproving(boolean approving)
		{
			this.approving = approving;
		}

		public boolean isRejecting()
		{
			return rejecting;
		}

		public void setRejecting(boolean rejecting)
		{
			this.rejecting = rejecting;
		}

		public boolean isRedrafting()
		{
			return redrafting;
		}

		public void setRedrafting(boolean redrafting)
		{
			this.redrafting = redrafting;
		}

		public Label getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(Label errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		public void setHeading(Label heading)
		{
			this.heading = heading;
		}

		public Label getHeading()
		{
			return heading;
		}
	}
}
