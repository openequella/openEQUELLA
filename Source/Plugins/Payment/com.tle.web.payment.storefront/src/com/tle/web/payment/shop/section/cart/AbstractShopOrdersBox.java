package com.tle.web.payment.shop.section.cart;

import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.core.payment.storefront.CurrencyTotal;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.services.user.UserSessionService;
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
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author Aaron
 */
@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractShopOrdersBox extends AbstractPrototypeSection<AbstractShopOrdersBox.ShopOrdersBoxModel>
	implements
		HtmlRenderer
{
	@PlugKey("shop.orders.label.multicurrency")
	private static Label LABEL_MULTI_CURRENCY;

	@Component
	private Box box;

	@Inject
	private UserSessionService session;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private ShopMoneyLabelService moneyLabelService;

	@Inject
	private OrderService orderService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	protected abstract boolean isShown(SectionInfo info);

	/**
	 * Render the name of the user instead of the order status
	 * 
	 * @param info
	 * @return
	 */
	protected abstract boolean isShowUser(SectionInfo info);

	protected abstract Label getBoxTitle();

	protected abstract String getMinimisedKey();

	protected abstract List<Order> getOrders(SectionInfo info);

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		if( isShown(context) )
		{
			final List<Order> orders = getOrders(context);
			if( !orders.isEmpty() )
			{
				final ShopOrdersBoxModel model = getModel(context);
				box.setLabel(context, getBoxTitle());

				model.setOrders(Lists.transform(orders, new Function<Order, OrderDisplayModel>()
				{
					@Override
					public OrderDisplayModel apply(Order order)
					{
						final OrderDisplayModel od = new OrderDisplayModel();
						final SectionInfo fwd = context.createForward(ShopConstants.URL_CART);
						final ShopViewCartSection viewCart = fwd.lookupSection(ShopViewCartSection.class);
						viewCart.setOrderUuid(fwd, order.getUuid());
						final HtmlLinkState link = new HtmlLinkState(new InfoBookmark(fwd));
						od.setLink(link);
						od.setDate(order.getLastActionDate());
						if( isShowUser(context) )
						{
							od.setUserLabel(userLinkSection.createLabel(context, order.getCreatedBy()));
						}
						else
						{
							od.setStatus(order.getStatus().toString());
						}

						final Map<Currency, CurrencyTotal> totes = orderService.calculateCurrencyTotals(order);
						final int currencies = totes.size();
						if( currencies > 1 )
						{
							od.setTotal(LABEL_MULTI_CURRENCY);
						}
						else if( currencies == 0 )
						{
							od.setTotal(moneyLabelService.getLabel(0L, 0L, null));
						}
						else
						{
							final Entry<Currency, CurrencyTotal> tote = totes.entrySet().iterator().next();
							final CurrencyTotal currencyTotal = tote.getValue();
							od.setTotal(moneyLabelService.getLabel(currencyTotal.getValue(),
								currencyTotal.getCombinedTaxTotal(), currencyTotal.getCurrency()));
						}

						return od;
					}
				}));

				return view.createResult("shop/orderbox.ftl", context);
			}
		}
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		box.addMinimiseHandler(events.getEventHandler("toggleMinimised"));
		userLinkSection = userLinkService.register(tree, id);
	}

	@EventHandlerMethod
	public void toggleMinimised(SectionInfo info)
	{
		final String minimisedKey = getMinimisedKey();
		Boolean minimised = (Boolean) session.getAttribute(minimisedKey);
		if( minimised == null )
		{
			minimised = false;
		}
		box.setMinimised(info, !minimised);
		session.setAttribute(minimisedKey, !minimised);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShopOrdersBoxModel();
	}

	public Box getBox()
	{
		return box;
	}

	public static class OrderDisplayModel
	{
		private HtmlLinkState link;
		private Date date;
		private String status;
		private Label userLabel;
		private Label total;

		public HtmlLinkState getLink()
		{
			return link;
		}

		public void setLink(HtmlLinkState link)
		{
			this.link = link;
		}

		public Date getDate()
		{
			return date;
		}

		public void setDate(Date date)
		{
			this.date = date;
		}

		public String getStatus()
		{
			return status;
		}

		public void setStatus(String status)
		{
			this.status = status;
		}

		public Label getUserLabel()
		{
			return userLabel;
		}

		public void setUserLabel(Label userLabel)
		{
			this.userLabel = userLabel;
		}

		public Label getTotal()
		{
			return total;
		}

		public void setTotal(Label total)
		{
			this.total = total;
		}
	}

	public static class ShopOrdersBoxModel
	{
		private List<OrderDisplayModel> orders;

		// private SectionRenderable contents;

		public List<OrderDisplayModel> getOrders()
		{
			return orders;
		}

		public void setOrders(List<OrderDisplayModel> orders)
		{
			this.orders = orders;
		}

		// public SectionRenderable getContents()
		// {
		// return contents;
		// }
		//
		// public void setContents(SectionRenderable contents)
		// {
		// this.contents = contents;
		// }
	}
}
