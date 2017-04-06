package com.tle.web.payment.shop.section;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.interfaces.I18NString;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.payment.shop.ShopConstants;
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
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class ShopStoresSection extends AbstractPrototypeSection<ShopStoresSection.ShopStoresModel>
	implements
		HtmlRenderer
{
	private static final Logger LOGGER = Logger.getLogger(ShopStoresSection.class);

	@PlugKey("shop.stores.title")
	private static Label LABEL_TITLE;
	@PlugKey("shop.stores.error.store")
	private static Label LABEL_ERROR_STORE;
	@PlugKey("shop.browse.error.accessdenied")
	private static String KEY_ERROR_ACCESS_DENIED;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private StoreService storeService;
	@Inject
	private ShopService shopService;
	@Inject
	private OrderService orderService;
	@Inject
	private TLEAclManager aclService;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.setTitle(context, LABEL_TITLE);

		final ShopStoresModel model = getModel(context);

		final List<Store> stores = storeService.enumerateBrowsable();
		final List<StoreDisplayModel> storeDisplays = Lists.newArrayList();

		// ensure BROWSE priv - aclService returns non-empty set, or ...
		boolean canSeeStore = !aclService.filterNonGrantedObjects(
			Collections.singleton(StoreFrontConstants.PRIV_BROWSE_STORE), Collections.singleton(stores)).isEmpty();
		if( !canSeeStore )
		{
			canSeeStore = orderService.isCurrentUserAnApprover() || orderService.isCurrentUserAPayer();
		}
		// ... or otherwise Approver/Payer capacity. Granting canSeeStore here
		// allows the ApproverPayer to see the stores section, but it will not
		// be populated with any stores. The Approver/Payer does get to see
		// the col-2 boxes listing pending approvals payments, thereby giving
		// effect to their permissions and prohibitions.
		if( !canSeeStore )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_ACCESS_DENIED,
				StoreFrontConstants.PRIV_BROWSE_STORE));
		}

		for( Store store : stores )
		{
			final StoreDisplayModel sd = new StoreDisplayModel();
			try
			{
				final StoreBean storeInformation = shopService.getStoreInformation(store, false);

				final Locale locale = CurrentLocale.getLocale();
				final Label title = new TextLabel(storeInformation.getName().toString());
				I18NString description = storeInformation.getDescription();
				if( description != null )
				{
					final Label descriptionLabel = new TextLabel(description.toString());
					sd.setDescription(descriptionLabel);
				}

				sd.setTitle(title);

				sd.setIcon(new ImageRenderer(storeInformation.getImage(), title));

				final SectionInfo fwd = context.createForward(ShopConstants.URL_STORE);
				final ShopCataloguesSection catSection = fwd.lookupSection(ShopCataloguesSection.class);
				catSection.viewStore(fwd, store.getUuid());
				sd.setLink(new HtmlLinkState(new InfoBookmark(fwd)));

				storeDisplays.add(sd);
			}
			catch( AccessDeniedException ade )
			{
				// store is not setup as a store anymore
				LOGGER.error("Store '" + CurrentLocale.get(store.getName()) + "' has denied access", ade);
			}
			catch( NotFoundException nfe )
			{
				// store not reachable
				LOGGER.error("Store '" + CurrentLocale.get(store.getName()) + "' cannot be reached", nfe);
			}
			catch( Exception t )
			{
				LOGGER.error("Error rendering store " + CurrentLocale.get(store.getName()), t);
				sd.setTitle(new BundleLabel(store.getName(), bundleCache));
				sd.setDescription(LABEL_ERROR_STORE);
				sd.setErrored(true);

				storeDisplays.add(sd);
			}
		}
		model.setStores(storeDisplays);

		return view.createResult("shop/stores.ftl", context);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShopStoresModel();
	}

	public static class ShopStoresModel
	{
		private List<StoreDisplayModel> stores;

		public List<StoreDisplayModel> getStores()
		{
			return stores;
		}

		public void setStores(List<StoreDisplayModel> stores)
		{
			this.stores = stores;
		}
	}

	public static class StoreDisplayModel
	{
		private HtmlLinkState link;
		private ImageRenderer icon;
		private Label title;
		private Label description;
		private boolean errored;

		public HtmlLinkState getLink()
		{
			return link;
		}

		public void setLink(HtmlLinkState link)
		{
			this.link = link;
		}

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public Label getDescription()
		{
			return description;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}

		public void setIcon(ImageRenderer icon)
		{
			this.icon = icon;
		}

		public ImageRenderer getIcon()
		{
			return icon;
		}

		public boolean isErrored()
		{
			return errored;
		}

		public void setErrored(boolean errored)
		{
			this.errored = errored;
		}
	}
}
