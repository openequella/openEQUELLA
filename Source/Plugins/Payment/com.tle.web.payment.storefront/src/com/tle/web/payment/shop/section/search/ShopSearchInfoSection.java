package com.tle.web.payment.shop.section.search;

import java.util.List;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.inject.Inject;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.ShopCataloguesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.ImageRenderer;

/**
 * @author dustin
 */
public class ShopSearchInfoSection extends AbstractPrototypeSection<ShopSearchInfoSection.ShopSearchInfoDisplayModel>
	implements
		HtmlRenderer
{
	@PlugKey("shop.search.error.cataloguenotfound")
	private static Label LABEL_CATALOGUE_NOT_FOUND;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ShopService shopService;

	@Component
	@PlugKey("shop.search.link.back")
	private Link returnToStore;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ShopSearchInfoDisplayModel model = getModel(context);

		ShopSearchSectionInfo shopInfo = ShopSearchSectionInfo.getSearchInfo(context);
		Store store = shopInfo.getStore();

		String catalogueId = shopInfo.getCatUuid();
		StoreCatalogueBean storeCatalogueBean = shopService.getCatalogue(store, catalogueId, false);

		if( storeCatalogueBean == null )
		{
			// Forces rebuild of catalogue cache
			shopService.getCatalogues(store, true);
			throw new NotFoundException(LABEL_CATALOGUE_NOT_FOUND.getText());
		}

		model.setTitle(new BundleLabel(store.getName(), bundleCache));
		model.setShowBackLink(true);

		List<StoreCatalogueBean> catalogues = shopService.getCatalogues(store, false);
		if( catalogues.size() > 1 )
		{
			returnToStore.setBookmark(context, new InfoBookmark(browseStore(context)));
		}
		else
		{
			model.setShowBackLink(false);
		}

		model.setCatalogueName(new TextLabel(storeCatalogueBean.getNameStrings().asI18NString(catalogueId).toString()));
		I18NStrings descriptionStrings = storeCatalogueBean.getDescriptionStrings();
		if( descriptionStrings != null )
		{
			model.setCatalogueDescription(new TextLabel(descriptionStrings.toString()));
		}

		StoreBean storebean = shopService.getStoreInformation(store, false);
		String iconpath = storebean.getIcon();
		model.setIcon(new ImageRenderer(iconpath, new BundleLabel(store.getName(), bundleCache)));

		return viewFactory.createResult("shop/shopsearchinfo.ftl", context); //$NON-NLS-1$
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShopSearchInfoDisplayModel();
	}

	@Override
	public Class<ShopSearchInfoDisplayModel> getModelClass()
	{
		return ShopSearchInfoDisplayModel.class;
	}

	private SectionInfo browseStore(SectionInfo info)
	{
		final ShopSearchSectionInfo shopInfo = ShopSearchSectionInfo.getSearchInfo(info);
		final Store store = shopInfo.getStore();

		final SectionInfo fwd = info.createForward(ShopConstants.URL_STORE);
		final ShopCataloguesSection catsSection = fwd.lookupSection(ShopCataloguesSection.class);
		catsSection.viewStore(fwd, store.getUuid());

		return fwd;
	}

	public Link getReturnToStore()
	{
		return returnToStore;
	}

	public static class ShopSearchInfoDisplayModel
	{
		private Label title;
		private Label catalogueName;
		private Label catalogueDescription;
		private ImageRenderer icon;
		private boolean showBackLink;

		public Label getTitle()
		{
			return title;
		}

		public void setIcon(ImageRenderer icon)
		{
			this.icon = icon;
		}

		public ImageRenderer getIcon()
		{
			return icon;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public Label getCatalogueName()
		{
			return catalogueName;
		}

		public void setCatalogueName(Label catalogueName)
		{
			this.catalogueName = catalogueName;
		}

		public Label getCatalogueDescription()
		{
			return catalogueDescription;
		}

		public void setCatalogueDescription(Label catalogueDescription)
		{
			this.catalogueDescription = catalogueDescription;
		}

		public boolean isShowBackLink()
		{
			return showBackLink;
		}

		public void setShowBackLink(boolean showBackLink)
		{
			this.showBackLink = showBackLink;
		}
	}
}
