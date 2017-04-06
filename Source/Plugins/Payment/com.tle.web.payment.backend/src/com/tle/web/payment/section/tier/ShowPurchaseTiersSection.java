package com.tle.web.payment.section.tier;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.PricingTierService;
import com.tle.web.payment.service.PaymentWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.MoneyLabel;

/**
 * @author Aaron
 */
@TreeIndexed
@SuppressWarnings("nls")
public class ShowPurchaseTiersSection extends AbstractShowTiersSection
{
	public static final String AJAX_ID = "purt";

	@PlugKey("tier.purchase.heading")
	private static Label LABEL_TITLE;
	@PlugKey("tier.purchase.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("tier.purchase.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("tier.purchase.column.price")
	private static Label LABEL_COLUMN_PRICE;
	@PlugKey("tier.purchase.prices.cell.notavailable")
	private static Label LABEL_NOT_AVAILABLE;

	@Inject
	private PaymentWebService paymentWebService;
	@Inject
	private PricingTierService tierService;

	public ShowPurchaseTiersSection()
	{
		super(true);
		noArea = true;
	}

	@Override
	protected List<Object> getColumnHeadings()
	{
		return Lists.newArrayList((Object) getEntityColumnLabel(), (Object) LABEL_COLUMN_PRICE);
	}

	@Override
	protected void addDynamicColumnData(SectionInfo info, PricingTier tier, SelectionsTableSelection row)
	{
		final Price price = tierService.getPriceForPurchaseTier(tier);
		if( price == null )
		{
			row.addColumn(LABEL_NOT_AVAILABLE);
		}
		else
		{
			row.addColumn(new MoneyLabel(price.getValue(), price.getCurrency()));
		}
	}

	@Override
	public String getAjaxId()
	{
		return AJAX_ID;
	}

	@Override
	protected boolean isDisplayed(SectionInfo info)
	{
		final PaymentSettings settings = paymentWebService.getSettings(info);
		return settings.isPurchaseEnabled();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

}
