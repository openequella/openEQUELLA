package com.tle.web.payment.shop.menu;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ShopMenuContributor implements MenuContributor
{
	private static final String SESSION_KEY = "SHOP-MENU";

	@PlugKey("menu")
	private static Label LABEL_MENU;
	@PlugURL("images/shop.png")
	private static String ICON_PATH;

	@Inject
	private UserSessionService userSessionService;
	@Inject
	private StoreService storeService;
	@Inject
	private OrderService orderService;

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		Boolean show = userSessionService.getAttribute(SESSION_KEY);

		if( show == null )
		{
			show = false;
			List<Store> browsableStores = storeService.enumerateBrowsable();
			if( !browsableStores.isEmpty() )
			{
				show = true;
			}
			else
			{
				show = orderService.isCurrentUserAnApprover() || orderService.isCurrentUserAPayer();
			}
			userSessionService.setAttribute(SESSION_KEY, show);
		}

		if( show )
		{
			HtmlLinkState hls = new HtmlLinkState(new InfoBookmark(info.createForward(ShopConstants.URL_SHOPS)));
			hls.setLabel(LABEL_MENU);
			MenuContribution mc = new MenuContribution(hls, ICON_PATH, 1, 32);
			return Collections.singletonList(mc);
		}
		else
		{
			return Collections.emptyList();
		}

	}

	@Override
	public void clearCachedData()
	{
		userSessionService.removeAttribute(SESSION_KEY);
	}

}
