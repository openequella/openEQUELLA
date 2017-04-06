package com.tle.web.payment.viewitem.section;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.CatalogueService.CatalogueInfo;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.summary.section.DisplaySectionConfiguration;
import com.tle.web.viewurl.ItemSectionInfo;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class PaymentSummarySection extends AbstractParentViewItemSection<PaymentSummarySection.PaymentSummaryModel>
	implements
		DisplaySectionConfiguration
{
	@PlugKey("viewitem.summary.catalogues.nocatalogues")
	private static Label LABEL_NO_CATALOGUES;
	@PlugKey("viewitem.summary.prices.subscriptionperiod.notavailable")
	private static Label LABEL_NOT_AVAILABLE;
	@PlugKey("viewitem.summary.pricing.column.tier")
	private static Label LABEL_COLUMN_TIER;
	@PlugKey("viewitem.summary.pricing.column.price")
	private static Label LABEL_COLUMN_PRICE;

	@Inject
	private PaymentService paymentService;
	@Inject
	private PricingTierService tierService;
	@Inject
	private CatalogueService catService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private BundleCache bundleCache;

	@Component(name = "cats")
	private SelectionsTable cataloguesTable;
	@Component(name = "purt")
	private Table purchaseTierTable;
	@Component(name = "subt")
	private Table subscriptionTierTable;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}

		final PaymentSummaryModel model = getModel(context);
		final ItemSectionInfo itemInfo = getItemInfo(context);

		final PaymentSettings enabledTiers = configService.getProperties(new PaymentSettings());
		final PricingTierAssignment pta = tierService.getPricingTierAssignmentForItem(itemInfo.getItemId());
		if( pta != null )
		{
			model.setFree(enabledTiers.isFreeEnabled() && pta.isFreeItem());

			PricingTier purchaseTier = pta.getPurchasePricingTier();
			if( purchaseTier != null && enabledTiers.isPurchaseEnabled() )
			{
				model.setPurchase(true);

				final TableState state = purchaseTierTable.getState(context);
				final TableRow row = state.addRow();
				row.addCell(new BundleLabel(purchaseTier.getName(), bundleCache));
				Price price = tierService.getPriceForPurchaseTier(purchaseTier);
				row.addCell(new MoneyLabel(price.getValue(), price.getCurrency()));
			}

			final PricingTier subscriptionTier = pta.getSubscriptionPricingTier();
			if( subscriptionTier != null && enabledTiers.isSubscriptionEnabled() )
			{
				model.setSubscription(true);

				final TableState state = subscriptionTierTable.getState(context);

				final TableHeaderRow headerRow = state.addHeaderRow();
				headerRow.addCell(LABEL_COLUMN_TIER);
				for( SubscriptionPeriod period : paymentService.enumerateSubscriptionPeriods() )
				{
					TableHeaderCell headerCell = headerRow.addCell(new BundleLabel(period.getName(), bundleCache));
					headerCell.setStyle("period");
				}

				final TableRow row = state.addRow();
				row.addCell(new BundleLabel(subscriptionTier.getName(), bundleCache));

				final Map<Long, Price> periodPriceMap = Maps.uniqueIndex(
					tierService.enumeratePricesForSubscriptionTier(subscriptionTier), new Function<Price, Long>()
					{
						@Override
						public Long apply(Price price)
						{
							return price.getPeriod().getId();
						}
					});
				for( SubscriptionPeriod period : paymentService.enumerateSubscriptionPeriods() )
				{
					final Price price = periodPriceMap.get(period.getId());
					if( price == null )
					{
						row.addCell(LABEL_NOT_AVAILABLE);
					}
					else
					{
						row.addCell(new MoneyLabel(price.getValue(), price.getCurrency()));
					}
				}
			}
		}

		return view.createResult("item/paymentsummary.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		purchaseTierTable.setColumnHeadings(LABEL_COLUMN_TIER, LABEL_COLUMN_PRICE);

		cataloguesTable.setNothingSelectedText(LABEL_NO_CATALOGUES);
		cataloguesTable.setSelectionsModel(new DynamicSelectionsTableModel<Catalogue>()
		{
			@Override
			protected Collection<Catalogue> getSourceList(SectionInfo info)
			{
				final ItemSectionInfo itemInfo = getItemInfo(info);
				final Item item = itemInfo.getItem();

				final CatalogueInfo catInfo = catService.groupCataloguesForItem(item);
				final List<Catalogue> cats = catInfo.getWhitelist();
				cats.addAll(catInfo.getDynamicExWhitelist());
				return cats;
			}

			@Override
			protected void transform(SectionInfo info, SelectionsTableSelection selection, Catalogue thing,
				List<SectionRenderable> actions, int index)
			{
				selection.setName(new BundleLabel(thing.getName(), bundleCache));
			}
		});
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		final ItemSectionInfo itemInfo = getItemInfo(info);
		return itemInfo.hasPrivilege(PaymentConstants.PRIV_VIEW_TIERS_FOR_ITEM);
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		// Nada
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new PaymentSummaryModel();
	}

	public SelectionsTable getCataloguesTable()
	{
		return cataloguesTable;
	}

	public Table getPurchaseTierTable()
	{
		return purchaseTierTable;
	}

	public Table getSubscriptionTierTable()
	{
		return subscriptionTierTable;
	}

	public static class PaymentSummaryModel
	{
		private boolean purchase;
		private boolean subscription;
		private boolean free;

		public boolean isPurchase()
		{
			return purchase;
		}

		public void setPurchase(boolean purchase)
		{
			this.purchase = purchase;
		}

		public boolean isSubscription()
		{
			return subscription;
		}

		public void setSubscription(boolean subscription)
		{
			this.subscription = subscription;
		}

		public boolean isFree()
		{
			return free;
		}

		public void setFree(boolean free)
		{
			this.free = free;
		}
	}
}
