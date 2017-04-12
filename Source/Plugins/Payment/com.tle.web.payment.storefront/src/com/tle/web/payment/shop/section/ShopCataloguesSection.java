package com.tle.web.payment.shop.section;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.ShopCataloguesSection.ShopCataloguesModel;
import com.tle.web.payment.shop.section.search.RootShopSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@TreeIndexed
public class ShopCataloguesSection extends AbstractPrototypeSection<ShopCataloguesModel> implements HtmlRenderer
{
	@PlugKey("shop.browse.banner")
	private static Label LABEL_TITLE;
	@PlugKey("shop.browse.breadcrumb")
	private static Label LABEL_BACK_TO_STORE;
	@PlugKey("shop.browse.error.accessdenied")
	private static String KEY_ERROR_ACCESS_DENIED;

	@Inject
	private StoreService storeService;
	@Inject
	private ShopService shopService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private TLEAclManager aclService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@Component
	@PlugKey("shop.browse.link.back")
	private Link backLink;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ShopCataloguesModel model = getModel(context);
		final Store store = storeService.getByUuid(model.getStoreUuid());

		// ensure BROWSE priv
		if( aclService.filterNonGrantedObjects(Collections.singleton(StoreFrontConstants.PRIV_BROWSE_STORE),
			Collections.singleton(store)).isEmpty() )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_ACCESS_DENIED,
				StoreFrontConstants.PRIV_BROWSE_STORE));
		}

		// only one catalogue exists, the catalogue search results page displays
		// immediately
		final List<StoreCatalogueBean> catalogues = shopService.getCatalogues(store, false);
		if( catalogues.size() == 1 )
		{
			context.forwardAsBookmark(getSearchCatalogueForward(context, catalogues.get(0).getUuid()));
			return null;
		}

		model.setCatalogues(getCatalogueDisplays(context, catalogues));

		HtmlLinkState breadcrumb = new HtmlLinkState(LABEL_BACK_TO_STORE);
		breadcrumb.setClickHandler(events.getNamedHandler("backToStorePage"));
		Breadcrumbs.get(context).add(breadcrumb);

		Decorations.getDecorations(context).setTitle(LABEL_TITLE);

		final StoreBean storeInfo = shopService.getStoreInformation(store, false);
		final Label storeName = new TextLabel(storeInfo.getName().toString());
		model.setStoreName(storeName);
		final ImageRenderer icon = new ImageRenderer(storeInfo.getIcon(), storeName);
		icon.addClass("icon");
		model.setIcon(icon);

		Breadcrumbs.get(context).setForcedLastCrumb(new WrappedLabel(storeName, -1, true));

		return view.createResult("shop/catalogues.ftl", this);
	}

	private List<CatalogueDisplayModel> getCatalogueDisplays(final SectionInfo info, List<StoreCatalogueBean> catalogues)
	{
		final List<CatalogueDisplayModel> displayCatalogues = Lists.newArrayList(Lists.transform(catalogues,
			new Function<StoreCatalogueBean, CatalogueDisplayModel>()
			{
				@Override
				public CatalogueDisplayModel apply(StoreCatalogueBean cat)
				{
					final CatalogueDisplayModel disp = new CatalogueDisplayModel();
					disp.setName(new BundleLabel(LangUtils.createTextTempLangugageBundle(cat.getNameStrings()
						.getStrings()), bundleCache));
					disp.setCount(cat.getAvailable());

					final I18NStrings descriptionStrings = cat.getDescriptionStrings();
					if( descriptionStrings != null )
					{
						disp.setDescription(new BundleLabel(LangUtils.createTextTempLangugageBundle(descriptionStrings
							.getStrings()), bundleCache));
					}
					disp.setLink(new HtmlLinkState(new InfoBookmark(getSearchCatalogueForward(info, cat.getUuid()))));
					return disp;
				}
			}));
		Collections.sort(displayCatalogues, new Comparator<CatalogueDisplayModel>()
		{
			@Override
			public int compare(CatalogueDisplayModel o1, CatalogueDisplayModel o2)
			{
				return o1.getName().getText().compareTo(o2.getName().getText());
			}
		});
		return displayCatalogues;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		backLink.setClickHandler(events.getNamedHandler("backToStorePage"));
	}

	@EventHandlerMethod
	public void backToStorePage(SectionInfo info)
	{
		final SectionInfo fwd = info.createForward(ShopConstants.URL_SHOPS);
		info.forward(fwd);
	}

	private SectionInfo getSearchCatalogueForward(SectionInfo info, String catUuid)
	{
		final SectionInfo fwd = info.createForward(ShopConstants.URL_SEARCH);
		final RootShopSearchSection root = fwd.lookupSection(RootShopSearchSection.class);
		root.setStore(fwd, getModel(info).getStoreUuid());
		root.setCatalogue(fwd, catUuid);
		return fwd;
	}

	public void viewStore(SectionInfo info, String storeUuid)
	{
		final ShopCataloguesModel model = getModel(info);
		model.setStoreUuid(storeUuid);
	}

	@Override
	public ShopCataloguesModel instantiateModel(SectionInfo info)
	{
		return new ShopCataloguesModel();
	}

	public Link getBackLink()
	{
		return backLink;
	}

	public static class CatalogueDisplayModel
	{
		private HtmlLinkState link;
		private Label name;
		private Label description;
		private int count;

		public int getCount()
		{
			return count;
		}

		public void setCount(int count)
		{
			this.count = count;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}

		public void setLink(HtmlLinkState link)
		{
			this.link = link;
		}

		public Label getName()
		{
			return name;
		}

		public void setName(Label name)
		{
			this.name = name;
		}

		public Label getDescription()
		{
			return description;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}
	}

	public static class ShopCataloguesModel
	{
		@Bookmarked(name = "s")
		private String storeUuid;
		private List<CatalogueDisplayModel> catalogues;
		private Label storeName;
		private ImageRenderer icon;

		public Label getStoreName()
		{
			return storeName;
		}

		public void setIcon(ImageRenderer icon)
		{
			this.icon = icon;
		}

		public ImageRenderer getIcon()
		{
			return icon;
		}

		public void setStoreName(Label storeName)
		{
			this.storeName = storeName;
		}

		public String getStoreUuid()
		{
			return storeUuid;
		}

		public void setStoreUuid(String storeUuid)
		{
			this.storeUuid = storeUuid;
		}

		public List<CatalogueDisplayModel> getCatalogues()
		{
			return catalogues;
		}

		public void setCatalogues(List<CatalogueDisplayModel> catalogues)
		{
			this.catalogues = catalogues;
		}
	}
}
