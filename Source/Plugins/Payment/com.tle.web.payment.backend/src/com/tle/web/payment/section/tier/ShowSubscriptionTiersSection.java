package com.tle.web.payment.section.tier;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.web.i18n.BundleCache;
import com.tle.web.payment.service.PaymentWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;

/**
 * @author Aaron
 */
@TreeIndexed
@SuppressWarnings("nls")
public class ShowSubscriptionTiersSection extends AbstractShowTiersSection
{
	public static final String AJAX_ID = "subt";

	@PlugKey("tier.subscription.heading")
	private static Label LABEL_TITLE;
	@PlugKey("tier.subscription.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("tier.subscription.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("tier.subscription.prices.cell.notavailable")
	private static Label LABEL_NOT_AVAILABLE;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private PaymentWebService paymentWebService;
	@Inject
	private PaymentService paymentService;
	@Inject
	private PricingTierService tierService;

	public ShowSubscriptionTiersSection()
	{
		super(false);
		noArea = true;
	}

	@Override
	protected void addDynamicColumnHeadings(SectionInfo info, TableHeaderRow header)
	{
		for( SubscriptionPeriod period : paymentService.enumerateSubscriptionPeriods() )
		{
			TableHeaderCell headerCell = header.addCell(new BundleLabel(period.getName(), bundleCache));
			headerCell.setStyle("period");
		}
	}

	@Override
	protected void addDynamicColumnData(SectionInfo info, PricingTier tier, SelectionsTableSelection row)
	{
		// need to print N/A in the appropriate places!
		// This is a bit dodgy
		final List<Price> prices = tierService.enumeratePricesForSubscriptionTier(tier);
		final Map<Long, Price> periodPriceMap = Maps.newHashMap();
		for( Price price : prices )
		{
			periodPriceMap.put(price.getPeriod().getId(), price);
		}

		final List<SubscriptionPeriod> periods = paymentService.enumerateSubscriptionPeriods();
		for( SubscriptionPeriod period : periods )
		{
			Price price = periodPriceMap.get(period.getId());
			if( price == null )
			{
				row.addColumn(LABEL_NOT_AVAILABLE);
			}
			else
			{
				row.addColumn(new MoneyLabel(price.getValue(), price.getCurrency()));
			}
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
		return settings.isSubscriptionEnabled();
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
