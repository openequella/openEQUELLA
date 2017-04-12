package com.tle.web.payment.storefront.section.store;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@TreeIndexed
public class ViewStoreInfoSection extends AbstractPrototypeSection<ViewStoreInfoSection.ViewStoreInfoModel>
	implements
		HtmlRenderer
{
	@PlugKey("store.view")
	private static Label TITLE_LABEL;

	@Inject
	private StoreService storeService;
	@Inject
	private ShopService shopService;

	@TreeLookup
	private AbstractRootEntitySection<?> rootSection;

	@PlugKey("store.view.button.back")
	@Component(name = "back", stateful = false)
	private Button backButton;

	@PlugKey("store.view.name")
	private static Label LABEL_NAME;
	@PlugKey("store.view.url")
	private static Label LABEL_URL;
	@PlugKey("store.view.clientid")
	private static Label LABEL_CLIENT_ID;
	@PlugKey("store.view.allowed")
	private static Label LABEL_TRANS_ALLOWED;
	@PlugKey("store.view.createdate")
	private static Label LABEL_CREATE_DATE;
	@PlugKey("store.view.allowed.free")
	private static Label LABEL_FREE;
	@PlugKey("store.view.allowed.purchase")
	private static Label LABEL_PURCHASE;
	@PlugKey("store.view.allowed.subscription")
	private static Label LABEL_SUB;
	@PlugKey("store.view.enabled")
	private static Label LABEL_ENABLED;
	@PlugKey("store.view.isenabled")
	private static Label LABEL_YES;
	@PlugKey("store.view.disabled")
	private static Label LABEL_NO;
	@PlugKey("store.view.contact.name")
	private static Label LABEL_CONTACT_NAME;
	@PlugKey("store.view.contact.phone")
	private static Label LABEL_CONTACT_PHONE;
	@PlugKey("store.view.contact.email")
	private static Label LABEL_CONTACT_EMAIL;

	@Component
	private Table storeDetails;
	@Component
	private Table contactDetails;

	@EventFactory
	protected EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final ViewStoreInfoModel model = getModel(info);
		if( !Check.isEmpty(model.getUuid()) )
		{
			rootSection.setModalSection(info, this);
		}
	}

	public void viewStoreInfo(SectionInfo info, String uuid)
	{
		final ViewStoreInfoModel model = getModel(info);
		model.setUuid(uuid);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		backButton.setClickHandler(events.getNamedHandler("goback"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.setTitle(context, TITLE_LABEL);
		Breadcrumbs.get(context);

		storeDetails.makePresentation(context);
		contactDetails.makePresentation(context);

		ViewStoreInfoModel model = getModel(context);
		Store store = storeService.getByUuid(model.getUuid());
		StoreBean storeInfo = shopService.getStoreInformation(store, true);
		StorePricingInformationBean pricingInfo = shopService.getPricingInformation(store, true);

		TableState storeDetailsState = storeDetails.getState(context);
		storeDetailsState.addRow(LABEL_NAME, new TextLabel(storeInfo.getName().toString()));
		storeDetailsState.addRow(LABEL_URL, new TextLabel(store.getStoreUrl()));
		storeDetailsState.addRow(LABEL_CLIENT_ID, new TextLabel(store.getClientId()));
		storeDetailsState.addRow(LABEL_CREATE_DATE, store.getDateCreated());

		if( pricingInfo.isAllowFree() )
		{
			storeDetailsState.addRow(LABEL_TRANS_ALLOWED, LABEL_FREE);
		}
		if( pricingInfo.isAllowPurchase() )
		{
			if( pricingInfo.isAllowFree() )
			{
				storeDetailsState.addRow(new TextLabel(""), LABEL_PURCHASE);
			}
			else
			{
				storeDetailsState.addRow(LABEL_TRANS_ALLOWED, LABEL_PURCHASE);
			}
		}
		if( pricingInfo.isAllowSubscription() )
		{
			if( pricingInfo.isAllowFree() || pricingInfo.isAllowPurchase() )
			{
				storeDetailsState.addRow(new TextLabel(""), LABEL_SUB);
			}
			else
			{
				storeDetailsState.addRow(LABEL_TRANS_ALLOWED, LABEL_SUB);
			}
		}
		storeDetailsState.addRow(LABEL_ENABLED, store.isDisabled() ? LABEL_NO : LABEL_YES);

		TableState contactDetailsState = contactDetails.getState(context);

		contactDetailsState.addRow(LABEL_CONTACT_NAME, new TextLabel(storeInfo.getContactName()));
		contactDetailsState.addRow(LABEL_CONTACT_PHONE, new TextLabel(storeInfo.getContactNumber()));
		contactDetailsState.addRow(LABEL_CONTACT_EMAIL, new TextLabel(storeInfo.getContactEmail()));
		return view.createResult("storeinfo.ftl", this);
	}

	public Table getStoreDetails()
	{
		return storeDetails;
	}

	public Table getContactDetails()
	{
		return contactDetails;
	}

	public Button getBackButton()
	{
		return backButton;
	}

	@EventHandlerMethod
	public void goback(SectionInfo info)
	{
		final SectionInfo fwd = info.createForward("/access/store.do");
		info.forward(fwd);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ViewStoreInfoModel();
	}

	public static class ViewStoreInfoModel
	{

		@Bookmarked
		private String uuid;

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getUuid()
		{
			return uuid;
		}

	}

}
