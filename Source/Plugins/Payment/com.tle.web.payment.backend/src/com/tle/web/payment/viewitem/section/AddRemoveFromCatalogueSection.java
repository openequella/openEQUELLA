package com.tle.web.payment.viewitem.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.CatalogueService.CatalogueInfo;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.viewitem.section.AddRemoveFromCatalogueSection.AddRemoveFromCatalogueModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@TreeIndexed
@Bind
@SuppressWarnings("nls")
public class AddRemoveFromCatalogueSection extends AbstractContentSection<AddRemoveFromCatalogueModel>
{
	private static final String CATALOGUE_ROW_KEY = "catalogueRow_"; //$NON-NLS-1$

	enum CatalogueEntryType
	{
		BLACKLIST, WHITELIST, DYNAMIC, NONE
	}

	@PlugKey("viewitem.current.catalogue")
	private static Label LABEL_CATALOGUE;
	@PlugKey("viewitem.current.status")
	private static Label LABEL_STATUS;
	@PlugKey("viewitem.current.action")
	private static Label LABEL_ACTION;
	@PlugKey("viewitem.current.delete.manual")
	private static Label LABEL_DELETE_MANUAL;
	@PlugKey("viewitem.current.delete.excluded")
	private static Label LABEL_DELETE_EXCLUDED;
	@PlugKey("viewitem.current.add.blacklist")
	private static Label LABEL_ADD_BLACKLIST;
	@PlugKey("viewitem.current.add.whitelist")
	private static Label LABEL_ADD_WHITELIST;

	@PlugKey("viewitem.current.exclude.confirm")
	private static Confirm CONFIRM_EXCLUDE;
	@PlugKey("viewitem.current.include.confirm")
	private static Confirm CONFIRM_INCLUDE;
	@PlugKey("viewitem.current.unexclude.confirm")
	private static Confirm CONFIRM_UNEXCLUDE;
	@PlugKey("viewitem.current.uninclude.confirm")
	private static Confirm CONFIRM_UNINCLUDE;

	@PlugKey("viewitem.current.type.")
	private static String KEY_TYPE_;
	@PlugURL("images/excluded.png")
	private static String EXCLUDED_URL;
	@PlugURL("images/included.png")
	private static String INCLUDED_URL;

	@PlugKey("viewitem.current.excluded")
	private static Label LABEL_EXCLUDED;
	@PlugKey("viewitem.current.included")
	private static Label LABEL_INCLUDED;

	@Inject
	private CatalogueService catalogueService;
	@Inject
	private PricingTierService pricingService;
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajaxEvents;

	@Component
	private Table currentCatalogues;

	private JSCallable removeFromList;
	private JSCallable addToList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		currentCatalogues.setColumnHeadings("", LABEL_CATALOGUE, LABEL_STATUS, LABEL_ACTION);
		currentCatalogues.setColumnSorts(Sort.NONE, Sort.PRIMARY_ASC, Sort.SORTABLE_ASC);

		removeFromList = ajaxEvents
			.getAjaxUpdateDomFunction(
				tree,
				null,
				events.getEventHandler("removeFromList"), ajaxEvents.getEffectFunction(AjaxGenerator.EffectType.FADEOUTIN), "catalogues"); //$NON-NLS-1$

		addToList = ajaxEvents
			.getAjaxUpdateDomFunction(
				tree,
				null,
				events.getEventHandler("addToList"), ajaxEvents.getEffectFunction(AjaxGenerator.EffectType.FADEOUTIN), "catalogues"); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void removeFromList(SectionInfo info, long catalogueId, boolean black)
	{
		Item item = ParentViewItemSectionUtils.getItemInfo(info).getItem();
		boolean modified = catalogueService.removeItemFromList(catalogueId, item, black);

		if( modified )
		{
			itemService.operation(new ItemIdKey(item), workflowFactory.reindexOnly(true));
		}
	}

	@EventHandlerMethod
	public void addToList(SectionInfo info, long catalogueId, boolean black)
	{
		Item item = ParentViewItemSectionUtils.getItemInfo(info).getItem();
		boolean modified = catalogueService.addItemToList(catalogueId, item, black);

		if( modified )
		{
			itemService.operation(new ItemIdKey(item), workflowFactory.reindexOnly(true));
		}
	}

	public boolean canView(SectionInfo info)
	{
		return !catalogueService.listManageable().isEmpty();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}

		final TableState state = currentCatalogues.getState(context);

		final CatalogueInfo catInfo = getCatalogueEntries(context);
		addRows(state, catInfo.getWhitelist(), CatalogueEntryType.WHITELIST);
		addRows(state, catInfo.getBlacklist(), CatalogueEntryType.BLACKLIST);
		addRows(state, catInfo.getDynamicExWhitelist(), CatalogueEntryType.DYNAMIC);
		List<Catalogue> none = catInfo.getNone();
		none.removeAll(catInfo.getBlacklist());
		addRows(state, none, CatalogueEntryType.NONE);

		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);

		AddRemoveFromCatalogueModel model = getModel(context);
		boolean pricing = false;
		PricingTierAssignment tier = pricingService.getPricingTierAssignmentForItem(itemInfo.getItemId());
		if( tier != null )
		{
			pricing |= tier.getPurchasePricingTier() != null;
			pricing |= tier.getSubscriptionPricingTier() != null;
			pricing |= tier.isFreeItem();

		}
		model.setPricing(pricing);

		addDefaultBreadcrumbs(context, itemInfo, LABEL_CATALOGUE);
		return viewFactory.createResult("currentcatalogues.ftl", context); //$NON-NLS-1$
	}

	private void addRows(TableState state, List<Catalogue> catalogues,
		CatalogueEntryType type)
	{
		for( Catalogue catalogue : catalogues )
		{
			createRow(state, catalogue, type);
		}
	}

	public Table getCurrentCatalogues()
	{
		return currentCatalogues;
	}

	@Override
	public Class<AddRemoveFromCatalogueModel> getModelClass()
	{
		return AddRemoveFromCatalogueModel.class;
	}

	private CatalogueInfo getCatalogueEntries(SectionInfo info)
	{
		Item item = ParentViewItemSectionUtils.getItemInfo(info).getItem();
		return catalogueService.groupCataloguesForItem(item);
	}

	private void createRow(TableState state, Catalogue catalogue, CatalogueEntryType type)
	{
		final TableRow row = state.addRow();
		row.registerUse();
		long catalogueId = catalogue.getId();
		SimpleElementId rowId = new SimpleElementId(CATALOGUE_ROW_KEY + catalogueId);
		row.setElementId(rowId);

		switch( type )
		{
			case WHITELIST:
			case DYNAMIC:
				TableCell includedIcon = new TableCell(new ImageRenderer(INCLUDED_URL, LABEL_INCLUDED));
				includedIcon.addClass("image"); //$NON-NLS-1$
				row.addCell(includedIcon);
				break;
			default:
				TableCell excludedIcon = new TableCell(new ImageRenderer(EXCLUDED_URL, LABEL_EXCLUDED));
				excludedIcon.addClass("image"); //$NON-NLS-1$
				row.addCell(excludedIcon);
		}

		row.addCell(new TableCell(new LabelRenderer(new BundleLabel(catalogue.getName(), bundleCache))));

		row.addCell(new TableCell((new KeyLabel(KEY_TYPE_ + type.toString().toLowerCase()))));

		if( catalogueService.canManage(catalogue) )
		{
			switch( type )
			{
				case BLACKLIST:
					row.addCell(new TableCell(createAction(LABEL_DELETE_EXCLUDED, new OverrideHandler(removeFromList,
						catalogueId, true).addValidator(CONFIRM_UNEXCLUDE))));
					break;

				case WHITELIST:
					row.addCell(new TableCell(createAction(LABEL_DELETE_MANUAL, new OverrideHandler(removeFromList,
						catalogueId, false).addValidator(CONFIRM_UNINCLUDE))));
					break;

				case NONE:
					row.addCell(new TableCell(createAction(LABEL_ADD_WHITELIST, new OverrideHandler(addToList,
						catalogueId, false).addValidator(CONFIRM_INCLUDE))));
					break;

				case DYNAMIC:
					row.addCell(new TableCell(createAction(LABEL_ADD_BLACKLIST, new OverrideHandler(addToList,
						catalogueId, true).addValidator(CONFIRM_EXCLUDE))));
					break;
			}

		}
		else
		{
			row.addCell(new TableCell());
			row.addClass("disabled");
		}
	}

	private LinkRenderer createAction(Label label, JSHandler handler)
	{
		return new LinkRenderer(new HtmlComponentState(label, handler));
	}

	public static class AddRemoveFromCatalogueModel
	{
		private boolean pricing;

		public boolean isPricing()
		{
			return pricing;
		}

		public void setPricing(boolean pricing)
		{
			this.pricing = pricing;
		}
	}
}
