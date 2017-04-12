package com.tle.web.payment.shop.section.search;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.navigation.BreadcrumbService;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.ShopCataloguesSection;
import com.tle.web.payment.shop.section.search.ShopSearchSectionInfo.ShopSearchSectionInfoFactory;
import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;

/**
 * @author Aaron
 */
@TreeIndexed
public class RootShopSearchSection extends AbstractRootSearchSection<RootShopSearchSection.RootShopSearchModel>
	implements
		ShopSearchSectionInfoFactory
{
	@PlugURL("css/shop/shop.css")
	private static String URL_CSS;

	@PlugKey("shop.search.title")
	private static Label TITLE_LABEL;
	@PlugKey("shop.search.breadcrumb.stores")
	private static Label LABEL_BACK_TO_STORES;
	@PlugKey("shop.search.error.accessdenied")
	private static String KEY_ERROR_ACCESS_DENIED;

	@Inject
	protected BreadcrumbService breadcrumbService;
	@Inject
	private StoreService storeService;
	@Inject
	private ShopService shopService;
	@Inject
	private TLEAclManager aclService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Store store = storeService.getByUuid(getModel(context).getStore());

		// ensure BROWSE priv
		if( aclService.filterNonGrantedObjects(Collections.singleton(StoreFrontConstants.PRIV_BROWSE_STORE),
			Collections.singleton(store)).isEmpty() )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_ACCESS_DENIED,
				StoreFrontConstants.PRIV_BROWSE_STORE));
		}

		StoreBean storeInfo = shopService.getStoreInformation(store, false);

		List<StoreCatalogueBean> catalogues = shopService.getCatalogues(store, false);
		StoreCatalogueBean catalogue = shopService.getCatalogue(store, getModel(context).getCatalogue(), false);

		final SectionInfo fwd = context.createForward(ShopConstants.URL_SHOPS);
		HtmlLinkState breadcrumb = new HtmlLinkState(LABEL_BACK_TO_STORES, new InfoBookmark(fwd));
		Breadcrumbs.get(context).add(breadcrumb);

		if( catalogues.size() > 1 )
		{
			final SectionInfo fwd2 = context.createForward(ShopConstants.URL_STORE);
			ShopCataloguesSection shopCataloguesSection = fwd2.lookupSection(ShopCataloguesSection.class);
			shopCataloguesSection.viewStore(fwd2, getModel(context).getStore());
			HtmlLinkState breadcrumb2 = new HtmlLinkState(new TextLabel(storeInfo.getName().toString()),
				new InfoBookmark(fwd2));
			Breadcrumbs.get(context).add(breadcrumb2);
		}

		Breadcrumbs.get(context).setForcedLastCrumb(
			new WrappedLabel(new TextLabel(catalogue.getName().toString()), -1, true));

		return super.renderHtml(context);
	}

	@Override
	@SuppressWarnings("nls")
	protected String getContentBodyClasses()
	{
		return "search-layout shop-layout";
	}

	@Override
	protected void createCssIncludes(List<CssInclude> includes)
	{
		super.createCssIncludes(includes);
		includes.add(CssInclude.include(URL_CSS)/* .hasRtl() */.make());
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	// @Override
	// public SectionResult renderHtml(RenderEventContext context) throws
	// Exception
	// {
	// return view.createResult("shop/search.ftl", context);
	// }

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new RootShopSearchModel();
	}

	@Override
	public ShopSearchSectionInfo getShopSearchSectionInfo(SectionInfo info)
	{
		final RootShopSearchModel model = getModel(info);
		final ShopSearchSectionInfo sinfo = new ShopSearchSectionInfo(storeService.getByUuid(model.getStore()),
			model.getCatalogue());
		return sinfo;
	}

	public void setStore(SectionInfo info, String storeUuid)
	{
		getModel(info).setStore(storeUuid);
	}

	public void setCatalogue(SectionInfo info, String catUuid)
	{
		getModel(info).setCatalogue(catUuid);
	}

	public static class RootShopSearchModel extends AbstractRootSearchSection.Model
	{
		@Bookmarked(name = "s")
		private String store;
		@Bookmarked(name = "c")
		private String catalogue;

		public String getStore()
		{
			return store;
		}

		public void setStore(String store)
		{
			this.store = store;
		}

		public String getCatalogue()
		{
			return catalogue;
		}

		public void setCatalogue(String catalogue)
		{
			this.catalogue = catalogue;
		}
	}
}
