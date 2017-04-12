package com.tle.web.payment.shop.section.cart;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.core.payment.storefront.CurrencyTotal;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.service.ShopMoneyLabelService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
public class ShopCartBoxSection extends AbstractPrototypeSection<ShopCartBoxSection.ShopCartBoxModel>
	implements
		HtmlRenderer
{
	@PlugKey("shop.cart.label.thereare")
	private static String KEY_THERE_ARE;
	@PlugKey("shop.cart.label.xitems")
	private static String KEY_XITEMS;
	@PlugKey("shop.cart.label.inyourcart")
	private static String KEY_IN_YOUR_CART;

	@PlugKey("shop.cart.label.shoppingcart")
	@Component
	private Box box;
	@PlugKey("shop.cart.button.checkout")
	@Component
	private Button viewCartButton;
	@Component
	private Link xItemsLink;

	@Inject
	private OrderService orderService;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private ShopMoneyLabelService moneyLabelService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( aclService.filterNonGrantedPrivileges(StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART).isEmpty() )
		{
			return null;
		}

		final ShopCartBoxModel model = getModel(context);

		final Order shoppingCart = orderService.getShoppingCart();

		final int itemCount = orderService.getItemCount(shoppingCart);
		if( itemCount == 0 )
		{
			model.setEmpty(true);
		}
		else
		{
			final SubmitValuesHandler viewCart = events.getNamedHandler("viewCart", shoppingCart.getUuid());
			viewCartButton.setClickHandler(context, viewCart);
			xItemsLink.setClickHandler(context, viewCart);
			xItemsLink.setLabel(context, new PluralKeyLabel(KEY_XITEMS, itemCount));
			model.setThereAreLabel(new PluralKeyLabel(KEY_THERE_ARE, itemCount));
			model.setInYourCartLabel(new PluralKeyLabel(KEY_IN_YOUR_CART, itemCount));
		}

		final Map<Currency, CurrencyTotal> currencyTotals = orderService.calculateCurrencyTotals(shoppingCart);

		final List<Label> totalLabels = Lists.newArrayList();
		final List<Currency> currencies = Lists.newArrayList(currencyTotals.keySet());
		Collections.sort(currencies, new Comparator<Currency>()
		{
			@Override
			public int compare(Currency o1, Currency o2)
			{
				if( o1 == null )
				{
					return -1;
				}
				if( o2 == null )
				{
					return 1;
				}
				return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
			}
		});
		for( Currency currency : currencies )
		{
			final CurrencyTotal currencyTotal = currencyTotals.get(currency);
			totalLabels.add(moneyLabelService.getLabel(currencyTotal.getValue(), currencyTotal.getCombinedTaxTotal(),
				currency));
		}
		if( totalLabels.size() == 0 && itemCount != 0 )
		{
			totalLabels.add(moneyLabelService.getLabel(0L, 0L, null));
		}

		model.setTotalLabels(totalLabels);

		return view.createResult("shop/cartbox.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, AbstractSearchActionsSection.AREA_SELECT);
		box.setNoMinMaxOnHeader(true);
	}

	@EventHandlerMethod
	public void viewCart(SectionInfo info, String cartUuid)
	{
		final SectionInfo fwd = info.createForward(ShopConstants.URL_CART);
		final ShopViewCartSection viewCart = fwd.lookupSection(ShopViewCartSection.class);
		viewCart.setOrderUuid(fwd, cartUuid);
		info.forwardAsBookmark(fwd);
	}

	@Override
	public ShopCartBoxModel instantiateModel(SectionInfo info)
	{
		return new ShopCartBoxModel();
	}

	public Box getCartBox()
	{
		return box;
	}

	public Button getViewCartButton()
	{
		return viewCartButton;
	}

	public Link getxItemsLink()
	{
		return xItemsLink;
	}

	public static class ShopCartBoxModel
	{
		private Label thereAreLabel;
		private Label inYourCartLabel;
		private List<Label> totalLabels;
		private boolean empty;

		public Label getThereAreLabel()
		{
			return thereAreLabel;
		}

		public void setThereAreLabel(Label thereAreLabel)
		{
			this.thereAreLabel = thereAreLabel;
		}

		public Label getInYourCartLabel()
		{
			return inYourCartLabel;
		}

		public void setInYourCartLabel(Label inYourCartLabel)
		{
			this.inYourCartLabel = inYourCartLabel;
		}

		public List<Label> getTotalLabels()
		{
			return totalLabels;
		}

		public void setTotalLabels(List<Label> totalLabels)
		{
			this.totalLabels = totalLabels;
		}

		public boolean isEmpty()
		{
			return empty;
		}

		public void setEmpty(boolean empty)
		{
			this.empty = empty;
		}
	}
}
