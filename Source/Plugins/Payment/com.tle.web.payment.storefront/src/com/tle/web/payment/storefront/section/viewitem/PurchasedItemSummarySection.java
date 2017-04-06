package com.tle.web.payment.storefront.section.viewitem;

import java.util.Date;

import com.google.inject.Inject;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.shop.service.ShopMoneyLabelService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.summary.section.DisplaySectionConfiguration;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class PurchasedItemSummarySection
	extends
		AbstractParentViewItemSection<PurchasedItemSummarySection.PurchasedItemModel>
	implements
		DisplaySectionConfiguration
{

	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@PlugKey("purchased.summary.tableheader.buyer")
	private static Label LABEL_HEADER_BUYER;
	@PlugKey("purchased.summary.tableheader.paid")
	private static Label LABEL_HEADER_PAID;
	@PlugKey("purchased.summary.tableheader.datepurchased")
	private static Label LABEL_HEADER_PUR_DATE;
	@PlugKey("purchased.summary.tableheader.numusers")
	private static Label LABEL_HEADER_USERS;
	@PlugKey("purchased.summary.tableheader.startdate")
	private static Label LABEL_HEADER_ST_DATE;
	@PlugKey("purchased.summary.tableheader.enddate")
	private static Label LABEL_HEADER_END_DATE;
	@PlugKey("purchased.summary.table.flatrate")
	private static Label LABEL_FLAT_RATE;
	@PlugKey("purchased.summary.table.nodate")
	private static Label LABEL_NO_DATE;

	@Inject
	private PurchaseService purchaseService;
	@Inject
	private ShopMoneyLabelService moneyLabelService;

	@Component
	private Table purchasedInfoTable;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}

		// need to first check if this item was even purchased
		ItemSectionInfo itemInfo = getItemInfo(context);
		ItemId itemId = ItemId.fromKey(itemInfo.getItemId());

		if( !purchaseService.isPurchased(itemInfo.getItem().getUuid()) )
		{
			return null;
		}

		final boolean showTax = moneyLabelService.isShowTax();
		final TableState state = purchasedInfoTable.getState(context);

		for( PurchaseItem purchasedItem : purchaseService.enumerateForItem(itemId) )
		{
			final TableRow row = state.addRow();
			SectionRenderable dateRenderable;

			final Date paidDate = purchasedItem.getPurchase().getPaidDate();
			dateRenderable = view.createResultWithModel("shop/date.ftl", paidDate); //$NON-NLS-1$
			row.addCell(dateRenderable).setSortData(paidDate);

			row.addCell(userLinkSection.createLink(context, purchasedItem.getPurchase().getCheckoutBy()));

			// Price paid
			final long price = purchasedItem.getPrice();
			final TableCell priceCell = row.addCell(moneyLabelService.getLabel(price, purchasedItem.getTax(), 1L,
				purchasedItem.getPurchase().getCurrency(), showTax, false));
			priceCell.setSortData(price).addClass("price"); //$NON-NLS-1$

			// No. Users
			final TableCell usersCell;
			final int numUsers = purchasedItem.getUsers();
			if( numUsers == 0 )
			{
				usersCell = row.addCell(LABEL_FLAT_RATE).setSortData(Integer.MAX_VALUE);
			}
			else
			{
				usersCell = row.addCell(new NumberLabel(numUsers)).setSortData(numUsers);
			}
			usersCell.addClass("users"); //$NON-NLS-1$

			// sub start/end date
			if( purchasedItem.getSubscriptionEndDate() != null )
			{
				dateRenderable = view.createResultWithModel("shop/date.ftl", purchasedItem.getSubscriptionStartDate()); //$NON-NLS-1$
				row.addCell(dateRenderable).setSortData(purchasedItem.getSubscriptionStartDate());

				dateRenderable = view.createResultWithModel("shop/date.ftl", purchasedItem.getSubscriptionEndDate()); //$NON-NLS-1$
				row.addCell(dateRenderable).setSortData(purchasedItem.getSubscriptionEndDate());
			}
			else
			{
				row.addCell(LABEL_NO_DATE).setSortData(new Date(0));
				row.addCell(LABEL_NO_DATE).setSortData(new Date(0));
			}
		}
		return view.createResult("purchasedsummary.ftl", this); //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		purchasedInfoTable.setColumnHeadings(LABEL_HEADER_PUR_DATE, LABEL_HEADER_BUYER, LABEL_HEADER_PAID,
			LABEL_HEADER_USERS, LABEL_HEADER_ST_DATE, LABEL_HEADER_END_DATE);
		purchasedInfoTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC,
			Sort.SORTABLE_ASC, Sort.SORTABLE_ASC);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new PurchasedItemModel();
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		// Nada
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		final ItemSectionInfo itemInfo = getItemInfo(info);
		return itemInfo.hasPrivilege(StoreFrontConstants.PRIV_VIEW_PURCHASE_DETAILS_FOR_ITEM);

	}

	public Table getPurchasedInfoTable()
	{
		return purchasedInfoTable;
	}

	public static class PurchasedItemModel
	{
		boolean purchasedItem;

		public boolean isPurchasedItem()
		{
			return purchasedItem;
		}

		public void setPurchasedItem(boolean purchasedItem)
		{
			this.purchasedItem = purchasedItem;
		}

	}
}
