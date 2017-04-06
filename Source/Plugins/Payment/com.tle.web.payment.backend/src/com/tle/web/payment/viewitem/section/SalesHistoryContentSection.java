package com.tle.web.payment.viewitem.section;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.i18n.LangUtils;
import com.tle.common.payment.entity.SaleItem;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.service.SaleService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class SalesHistoryContentSection extends AbstractContentSection<SalesHistoryContentSection.HistoryModel>
{
	@Inject
	private SaleService saleService;

	@PlugKey("viewitem.saleshistory.table.header.date")
	private static Label HEADER_DATE;
	@PlugKey("viewitem.saleshistory.table.header.shop")
	private static Label HEADER_SHOP;
	@PlugKey("viewitem.saleshistory.table.header.transaction")
	private static Label HEADER_TRANSACTION;
	@PlugKey("viewitem.saleshistory.table.header.total")
	private static Label HEADER_TOTAL;

	@PlugKey("viewitem.saleshistory.title")
	private static Label LABEL_BREADCRUMB;

	@PlugKey("viewitem.saleshistory.nosales")
	private static Label LABEL_NO_SALES;

	@PlugKey("viewitem.saleshistory.table.transaction.free")
	private static Label LABEL_FREE;
	@PlugKey("viewitem.saleshistory.table.transaction.subscribe")
	private static Label LABEL_SUB;
	@PlugKey("viewitem.saleshistory.table.transaction.outright")
	private static Label LABEL_OUTRIGHT;

	@Component
	private Table historyTable;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		historyTable.setColumnHeadings(HEADER_DATE, HEADER_SHOP, HEADER_TRANSACTION, HEADER_TOTAL);
		historyTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);

		final TableState state = historyTable.getState(context);

		String idString = itemInfo.getItem().getUuid();
		List<SaleItem> saleItemList = saleService.getSalesItemsForSourceItem(idString);
		if( saleItemList.size() == 0 )
		{
			state.addRow(LABEL_NO_SALES, "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		for( SaleItem saleItem : saleItemList )
		{
			final TableRow row = state.addRow();

			final SectionRenderable dateRenderable;
			Date paidDate = saleItem.getSale().getPaidDate();
			dateRenderable = view.createResultWithModel("item/date.ftl", paidDate); //$NON-NLS-1$
			row.addCell(dateRenderable);

			String shopFrontName = LangUtils.getString(saleItem.getSale().getStorefront().getName());
			row.addCell(new TextLabel(shopFrontName));

			String transaction = renderTransaction(saleItem).getText();
			row.addCell(renderTransaction(saleItem));

			long price = saleItem.getPrice();
			row.addCell(new MoneyLabel(price, saleItem.getSale().getCurrency()));

			row.setSortData(paidDate, shopFrontName, transaction, price);
		}
		addDefaultBreadcrumbs(context, itemInfo, LABEL_BREADCRUMB);
		displayBackButton(context);

		return view.createResult("item/purchasehistory.ftl", this); //$NON-NLS-1$
	}

	private Label renderTransaction(SaleItem saleItem)
	{
		if( saleItem.getSubscriptionStartDate() != null )
		{
			return LABEL_SUB;
		}
		else if( saleItem.getPrice() == 0 )
		{
			return LABEL_FREE;
		}
		return LABEL_OUTRIGHT;
	}

	protected boolean canView(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).hasPrivilege(PaymentConstants.PRIV_VIEW_SALES_FOR_ITEM);
	}

	public static class HistoryModel
	{
		// nuffin
	}

	public Table getHistoryTable()
	{
		return historyTable;
	}

}
