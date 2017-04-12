package com.tle.web.payment.shop.section.viewitem;

import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.collect.Lists;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.RootShopSection;
import com.tle.web.payment.shop.section.ShopCataloguesSection;
import com.tle.web.payment.shop.section.search.RootShopSearchSection;
import com.tle.web.payment.shop.section.viewitem.ShopItemSectionInfo.ShopItemSectionInfoFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CombinedTemplateResult;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@TreeIndexed
public class RootShopViewItemSection extends TwoColumnLayout<RootShopViewItemSection.RootShopViewItemModel>
	implements
		ShopItemSectionInfoFactory
{
	@PlugKey("shop.viewitem.title")
	private static Label LABEL_TITLE;
	@PlugKey("shop.viewitem.breadcrumb.title")
	private static Label LABEL_BREADCRUMB_TITLE;
	@PlugKey("shop.search.breadcrumb.stores")
	private static Label LABEL_BACK_TO_STORES;
	@PlugKey("shop.viewitem.error.notfound.remoteserver")
	private static Label LABEL_ITEM_NOT_FOUND;

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(RootShopSection.class);
	private static final CssInclude CSS = CssInclude.include(resources.url("css/shop/shop.css")).make();
	private static final List<CssInclude> CSS_INCLUDES = Lists.newArrayList(CSS);

	@Inject
	private StoreService storeService;
	@Inject
	private ShopService shopService;

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(LABEL_TITLE);
		decorations.setContentBodyClass("shop-item-layout shop-layout");

		RootShopViewItemModel model = getModel(info);
		Store store = storeService.getByUuid(model.getStoreUuid());

		HtmlLinkState storesBreadcrumb = new HtmlLinkState(LABEL_BACK_TO_STORES, new InfoBookmark(
			info.createForward(ShopConstants.URL_SHOPS)));
		crumbs.addToStart(storesBreadcrumb);

		if( shopService.getCatalogues(store, false).size() > 1 )
		{
			final StoreBean storeInfo = shopService.getStoreInformation(store, false);
			final SectionInfo fwd = info.createForward(ShopConstants.URL_STORE);
			final ShopCataloguesSection catsSection = fwd.lookupSection(ShopCataloguesSection.class);
			catsSection.viewStore(fwd, store.getUuid());

			HtmlLinkState shopBreadcrumb = new HtmlLinkState(new TextLabel(storeInfo.getName().toString()));
			shopBreadcrumb.setBookmark(new InfoBookmark(fwd));
			crumbs.add(shopBreadcrumb);
		}

		StoreCatalogueBean catalogue = shopService.getCatalogue(store, model.getCatUuid(), false);
		HtmlLinkState catalogueBreadcrumb = new HtmlLinkState(new TextLabel(catalogue.getName().toString()),
			new InfoBookmark(getCatalogueBookmark(info)));
		catalogueBreadcrumb.setTitle(LABEL_BREADCRUMB_TITLE);
		crumbs.add(catalogueBreadcrumb);

		ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
		if( iinfo.getItem().getName() != null )
		{
			crumbs.setForcedLastCrumb(new WrappedLabel(new TextLabel(iinfo.getItem().getName().toString()), 60, true));
		}
		else
		{
			crumbs.setForcedLastCrumb(new WrappedLabel(new TextLabel(iinfo.getItem().getUuid()), 60, true));
		}
	}

	private SectionInfo getCatalogueBookmark(SectionInfo info)
	{
		final SectionInfo fwd = info.createForward(ShopConstants.URL_SEARCH);
		final RootShopSearchSection search = fwd.lookupSection(RootShopSearchSection.class);
		final RootShopViewItemModel model = getModel(info);
		search.setStore(fwd, model.getStoreUuid());
		search.setCatalogue(fwd, model.getCatUuid());
		return fwd;
	}

	@Override
	protected TemplateResult getTemplateResult(RenderEventContext info)
	{
		CombinedTemplateResult templateResult = new CombinedTemplateResult();
		TwoColumnModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		if( modalSection != null )
		{
			templateResult.addNamedResult(OneColumnLayout.BODY, CombinedRenderer.combineMultipleResults(CSS_INCLUDES));
			templateResult.addResult(OneColumnLayout.BODY, SectionUtils.renderSectionResult(info, modalSection));
			return templateResult;
		}

		SectionRenderable body = null;
		SectionRenderable right = null;
		List<SectionId> children = getChildIds(info);
		for( SectionId childId : children )
		{
			String side = info.getLayout(childId.getSectionId());
			final SectionRenderable renderable = (SectionRenderable) SectionUtils.renderSectionResult(info, childId);
			if( !TwoColumnLayout.RIGHT.equals(side) )
			{
				body = CombinedRenderer.combineMultipleResults(body, renderable);
			}
			else
			{
				right = CombinedRenderer.combineMultipleResults(right, renderable);
			}
		}
		templateResult.addResult(OneColumnLayout.BODY, new DivRenderer("area", body));
		templateResult.addResult(TwoColumnLayout.RIGHT, right);

		templateResult.addNamedResult(OneColumnLayout.BODY, CombinedRenderer.combineMultipleResults(CSS_INCLUDES));

		return templateResult;
	}

	public void setItem(SectionInfo info, String storeUuid, String catUuid, String itemUuid)
	{
		final RootShopViewItemModel model = getModel(info);
		model.setStoreUuid(storeUuid);
		model.setCatUuid(catUuid);
		model.setItemUuid(itemUuid);
	}

	/**
	 * Do not call this. Use ShopItemSectionInfo.getItemInfo instead, otherwise
	 * you will invoke unncessary REST calls
	 */
	@Override
	public ShopItemSectionInfo createShopItemSectionInfo(SectionInfo info)
	{
		final RootShopViewItemModel model = getModel(info);

		final String storeUuid = model.getStoreUuid();
		final Store store = storeService.getByUuid(storeUuid);
		final String catUuid = model.getCatUuid();
		final StoreCatalogueItemBean item = shopService.getCatalogueItem(store, catUuid, model.getItemUuid());
		if( item == null )
		{
			throw new NotFoundException(LABEL_ITEM_NOT_FOUND.getText());
		}
		return new ShopItemSectionInfo(store, catUuid, item);
	}

	protected List<SectionId> getChildIds(RenderContext info)
	{
		return info.getChildIds(this);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new RootShopViewItemModel();
	}

	public static class RootShopViewItemModel extends TwoColumnLayout.TwoColumnModel
	{
		@Bookmarked(name = "s")
		private String storeUuid;
		@Bookmarked(name = "c")
		private String catUuid;
		@Bookmarked(name = "i")
		private String itemUuid;

		public String getStoreUuid()
		{
			return storeUuid;
		}

		public void setStoreUuid(String storeUuid)
		{
			this.storeUuid = storeUuid;
		}

		public String getCatUuid()
		{
			return catUuid;
		}

		public void setCatUuid(String catUuid)
		{
			this.catUuid = catUuid;
		}

		public String getItemUuid()
		{
			return itemUuid;
		}

		public void setItemUuid(String itemUuid)
		{
			this.itemUuid = itemUuid;
		}
	}
}
