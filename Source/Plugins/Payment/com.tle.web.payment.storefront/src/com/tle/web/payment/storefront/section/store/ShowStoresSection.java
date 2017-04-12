package com.tle.web.payment.storefront.section.store;

import javax.inject.Inject;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.payment.storefront.service.session.StoreEditingBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@SuppressWarnings("nls")
public class ShowStoresSection
	extends
		AbstractShowEntitiesSection<Store, AbstractShowEntitiesSection.AbstractShowEntitiesModel>
{
	@PlugKey("store.showlist.delete.storeinuse")
	private static Label LABEL_STORE_IN_USE_MESSAGE;
	@PlugKey("store.showlist.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("store.showlist.page.title")
	private static Label LABEL_STORES;
	@PlugKey("store.showlist.column.store")
	private static Label LABEL_COLUMN_STORE;
	@PlugKey("store.showlist.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("store.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;

	@TreeLookup
	private ViewStoreInfoSection viewStoreInfoSection;

	@Inject
	private StoreService storeService;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;

	private JSCallable viewStoreInfoFunction;
	private JSCallable editRegistrationFunction;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		viewStoreInfoFunction = events.getSubmitValuesFunction("viewStoreInfo");
		editRegistrationFunction = events.getSubmitValuesFunction("editRegistration");
		toggleEnabledFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("toggleEnabled"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), getAjaxId());
	}

	@Override
	protected JSHandler getNewEntityHandler()
	{
		return events.getNamedHandler("registerNew");
	}

	@Override
	protected AbstractEntityService<StoreEditingBean, Store> getEntityService()
	{
		return storeService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_STORES;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_COLUMN_STORE;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, Store entity)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(StoreFrontConstants.PRIV_CREATE_STORE).isEmpty();
	}

	@Override
	protected boolean canClone(SectionInfo info, Store ent)
	{
		return false;
	}

	@Override
	protected boolean isInUse(SectionInfo info, Store entity)
	{
		return false;
	}

	@Override
	protected SectionRenderable createViewLink(SectionInfo info, Store store)
	{
		final LinkRenderer link = new LinkRenderer(new HtmlLinkState(new OverrideHandler(viewStoreInfoFunction,
			store.getUuid())));
		link.setLabel(new BundleLabel(store.getName(), bundleCache));
		return link;
	}

	@Override
	protected SectionRenderable createEditLink(SectionInfo info, Store store)
	{
		final LinkRenderer link = new LinkRenderer(new HtmlLinkState(new OverrideHandler(editRegistrationFunction,
			store.getUuid())));
		link.setLabel(getEditLabel(info, store));
		return link;
	}

	@EventHandlerMethod
	public final void viewStoreInfo(SectionInfo info, String uuid)
	{
		viewStoreInfoSection.viewStoreInfo(info, uuid);
	}

	@EventHandlerMethod
	public final void editRegistration(SectionInfo info, String uuid)
	{
		final SectionInfo fwd = info.createForward("/access/registerstore.do");
		final StoreRegisterSection reg = fwd.lookupSection(StoreRegisterSection.class);
		reg.edit(fwd, uuid, false);
		info.forward(fwd);
	}

	@EventHandlerMethod
	public void registerNew(SectionInfo info)
	{
		final SectionInfo fwd = info.createForward("/access/registerstore.do");
		final StoreRegisterSection reg = fwd.lookupSection(StoreRegisterSection.class);
		reg.create(fwd);
		info.forward(fwd);
	}

	/**
	 * Only if the Store registration has never been used, can it be deleted.
	 * Check its history and if there is any, block the delete by throwing up a
	 * simple Alert instead of giving the user the superclass default: a
	 * confirm('yes'/'no') option.<br>
	 * Without going into deeper detail, it is enough that an OrderStorePart
	 * exists which references the Store, for a SQL Integrity Constraint
	 * Violation Exception to be thrown should an attempt be made to delete a
	 * Store.
	 */
	@Override
	protected JSValidator getDeleteValidator(SectionInfo info, Store store)
	{
		boolean storeHasHistory = storeService.storeHasHistory(store);
		return storeHasHistory ? new SimpleValidator(Js.alert(LABEL_STORE_IN_USE_MESSAGE.getText())) : super
			.getDeleteValidator(info, store);
	}
}
