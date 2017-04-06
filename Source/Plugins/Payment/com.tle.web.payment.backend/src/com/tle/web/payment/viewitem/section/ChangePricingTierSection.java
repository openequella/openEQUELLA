package com.tle.web.payment.viewitem.section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.payment.service.PaymentWebService;
import com.tle.web.payment.viewitem.section.ChangePricingTierSection.ChangePricingTierModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;

@NonNullByDefault
@TreeIndexed
@Bind
public class ChangePricingTierSection extends AbstractPrototypeSection<ChangePricingTierModel> implements HtmlRenderer
{
	@PlugKey("viewitem.tier.select")
	private static String KEY_SELECT;
	@PlugKey("viewitem.tier.save.receipt")
	private static Label LABEL_SAVE;
	@PlugKey("viewitem.tier.heading.tier")
	private static Label LABEL_HEADING_TIER;
	@PlugKey("tier.subscription.prices.cell.notavailable")
	private static Label LABEL_NOT_AVAILABLE;
	@PlugKey("viewitem.tier.none")
	private static Label LABEL_NO_TIER;

	@Inject
	private PricingTierService tierService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;
	@Inject
	private PaymentService paymentService;
	@Inject
	private PaymentWebService paymentWebService;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "ptl", stateful = false)
	protected SingleSelectionList<Entry<PricingTier, Collection<Price>>> purchaseTierList;
	@Component(name = "fb", stateful = false)
	protected Checkbox freeBox;
	@Component(name = "sst", stateful = false)
	protected MappedBooleans selectedSubscriptionTier;

	@PlugKey("viewitem.tier.save")
	@Component(stateful = false)
	private Button saveButton;

	@Component(name = "stt", stateful = false)
	private Table subscriptionTierTable;

	private boolean forBulk;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		purchaseTierList.setListModel(new TierListModel(true));

		subscriptionTierTable.setColumnSorts(Sort.NONE, Sort.PRIMARY_ASC);

		saveButton.setClickHandler(new OverrideHandler(events.getSubmitValuesFunction("save"))); //$NON-NLS-1$
		// keyboard accessibility focusing
		if( forBulk )
		{
			freeBox.setStyleClass("focus"); //$NON-NLS-1$
			purchaseTierList.setStyleClass("focus"); //$NON-NLS-1$
		}

	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		PaymentSettings settings = paymentWebService.getSettings(context);
		ChangePricingTierModel model = getModel(context);
		model.setShowFree(settings.isFreeEnabled());
		model.setShowSubscription(settings.isSubscriptionEnabled());
		model.setShowPurchase(settings.isPurchaseEnabled());

		if( !forBulk )
		{
			if( !canView(context) )
			{
				return null;
			}

			ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
			PricingTierAssignment tierAssignment = tierService.getPricingTierAssignmentForItem(itemInfo.getItemId());
			if( tierAssignment != null )
			{
				freeBox.setChecked(context, tierAssignment.isFreeItem());
				PricingTier purcTier = tierAssignment.getPurchasePricingTier();
				purchaseTierList.setSelectedStringValue(context, purcTier != null ? purcTier.getUuid() : null);
				PricingTier subTier = tierAssignment.getSubscriptionPricingTier();
				selectedSubscriptionTier.setCheckedSet(context,
					Collections.singleton(subTier != null ? subTier.getUuid() : null));
			}
			else
			{
				selectedSubscriptionTier.setCheckedSet(context, Collections.<String> emptySet());
			}
		}
		else
		{
			selectedSubscriptionTier.getBooleanState(context, null).addClass("focus"); //$NON-NLS-1$
		}

		final TableState tableState = subscriptionTierTable.getState(context);
		final List<SubscriptionPeriod> periods = paymentService.enumerateSubscriptionPeriods();

		final TableHeaderRow headerRow = tableState.addHeaderRow();
		headerRow.addCell(null);
		headerRow.addCell(LABEL_HEADING_TIER);
		for( SubscriptionPeriod period : periods )
		{
			final TableHeaderCell headerCell = headerRow.addCell(new BundleLabel(period.getName(), bundleCache));
			headerCell.setStyle("period");
		}

		ArrayList<Object> noTier = new ArrayList<Object>();
		noTier.add(new RadioButtonRenderer(selectedSubscriptionTier.getBooleanState(context, null)));
		TableCell noTierLabelCell = new TableCell(LABEL_NO_TIER);
		noTierLabelCell.setSortData("");
		noTier.add(noTierLabelCell);
		for( Object period : periods )
		{
			noTier.add(null);
		}
		tableState.addRow(noTier.toArray());

		ListMultimap<PricingTier, Price> tierPriceMap = tierService.getEnabledTierPriceMap(false);
		Set<PricingTier> tiers = tierPriceMap.keySet();
		for( PricingTier tier : tiers )
		{
			final List<Object> priceCells = Lists.newArrayList();
			List<Price> prices = tierPriceMap.get(tier);
			final Map<Long, Price> periodPriceMap = Maps.newHashMap();
			for( Price price : prices )
			{
				periodPriceMap.put(price.getPeriod().getId(), price);
			}

			for( SubscriptionPeriod period : periods )
			{
				Price price = periodPriceMap.get(period.getId());
				if( price == null )
				{
					priceCells.add(LABEL_NOT_AVAILABLE);
				}
				else
				{
					priceCells.add(new MoneyLabel(price.getValue(), price.getCurrency()));
				}
			}

			final TableRow tableRow = tableState.addRow();
			tableRow.addCell(new TableCell(new RadioButtonRenderer(selectedSubscriptionTier.getBooleanState(context,
				tier.getUuid()))));
			tableRow.addCell(new TableCell(new BundleLabel(tier.getName(), bundleCache)));
			for( Object priceCell : priceCells )
			{
				tableRow.addCell(new TableCell(priceCell));
			}
		}

		selectedSubscriptionTier.getBooleanState(context, null).setChecked(true);

		return viewFactory.createResult("changetier.ftl", context); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		PaymentSettings settings = paymentWebService.getSettings(info);

		boolean free = freeBox.isChecked(info);

		String subUuid = selectedSubscriptionTier.getFirstChecked(info);
		PricingTier subTier = null;
		if( !Check.isEmpty(subUuid) )
		{
			subTier = tierService.getByUuid(subUuid);
		}
		Entry<PricingTier, Collection<Price>> selectedPur = purchaseTierList.getSelectedValue(info);
		PricingTier purTier = selectedPur != null ? tierService.get(selectedPur.getKey().getId()) : null;

		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		ItemKey itemId = itemInfo.getItemId();
		PricingTierAssignment assignmentTier = tierService.getPricingTierAssignmentForItem(itemId);

		if( assignmentTier != null )
		{
			if( settings.isFreeEnabled() )
			{
				assignmentTier.setFreeItem(free);
			}
			if( settings.isPurchaseEnabled() )
			{
				assignmentTier.setPurchasePricingTier(purTier);
			}
			if( settings.isSubscriptionEnabled() )
			{
				assignmentTier.setSubscriptionPricingTier(subTier);
			}
			tierService.savePricingTierAssignment(assignmentTier);
		}
		else
		{
			tierService.createPricingTierAssignment(itemId, settings.isPurchaseEnabled() ? purTier : null,
				settings.isSubscriptionEnabled() ? subTier : null, settings.isFreeEnabled() ? free : false);
		}

		itemService.operation(itemId, workflowFactory.reindexOnly(false));
		receiptService.setReceipt(LABEL_SAVE);
	}

	public static boolean canView(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).hasPrivilege(PaymentConstants.PRIV_SET_TIERS_FOR_ITEM);
	}

	public SingleSelectionList<Entry<PricingTier, Collection<Price>>> getPurchaseTierList()
	{
		return purchaseTierList;
	}

	public Checkbox getFreeBox()
	{
		return freeBox;
	}

	public class TierListModel extends DynamicHtmlListModel<Entry<PricingTier, Collection<Price>>>
	{
		private final boolean purchase;

		public TierListModel(boolean purchase)
		{
			this.purchase = purchase;
			setSort(true);
		}

		@Override
		protected Collection<Entry<PricingTier, Collection<Price>>> populateModel(SectionInfo info)
		{
			ListMultimap<PricingTier, Price> tierPriceMap = tierService.getEnabledTierPriceMap(purchase);

			return tierPriceMap.asMap().entrySet();
		}

		@Override
		protected Option<Entry<PricingTier, Collection<Price>>> getTopOption()
		{
			return new KeyOption<Entry<PricingTier, Collection<Price>>>(KEY_SELECT, "", null);
		}

		@Override
		protected Option<Entry<PricingTier, Collection<Price>>> convertToOption(SectionInfo info,
			Entry<PricingTier, Collection<Price>> entry)
		{
			PricingTier pricingTier = entry.getKey();
			Collection<Price> prices = entry.getValue();
			BundleLabel nameLabel = new BundleLabel(pricingTier.getName(), bundleCache);

			Label combined = nameLabel;

			for( Price price : prices )
			{
				MoneyLabel priceLabel = new MoneyLabel(price.getValue(), price.getCurrency());
				combined = AppendedLabel.get(combined, priceLabel, TextLabel.SPACE);
			}

			return new LabelOption<Entry<PricingTier, Collection<Price>>>(combined, pricingTier.getUuid(), entry);
		}
	}

	@Override
	public Class<ChangePricingTierModel> getModelClass()
	{
		return ChangePricingTierModel.class;
	}

	public boolean isForBulk()
	{
		return forBulk;
	}

	public void setForBulk(boolean forBulk)
	{
		this.forBulk = forBulk;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public void setSaveButton(Button saveButton)
	{
		this.saveButton = saveButton;
	}

	public Table getSubscriptionTierTable()
	{
		return subscriptionTierTable;
	}

	public static class ChangePricingTierModel
	{
		private boolean showFree;
		private boolean showPurchase;
		private boolean showSubscription;

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
