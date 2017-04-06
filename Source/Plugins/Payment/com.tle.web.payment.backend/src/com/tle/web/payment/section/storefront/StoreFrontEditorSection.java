package com.tle.web.payment.section.storefront;

import java.util.Locale;

import javax.inject.Inject;

import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.service.OAuthClientEditingBean;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.payment.service.TaxService;
import com.tle.core.payment.service.session.StoreFrontEditingBean;
import com.tle.core.payment.service.session.StoreFrontEditingSession;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.payment.model.CountryListModel;
import com.tle.web.payment.section.storefront.StoreFrontEditorSection.StoreFrontEditorModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
@Bind
public class StoreFrontEditorSection
	extends
		AbstractEntityEditor<StoreFrontEditingBean, StoreFront, StoreFrontEditorModel>
{
	@PlugKey("storefront.edit.label.registrationname")
	private static Label LABEL_REGNAME;
	@PlugKey("storefront.edit.label.notes")
	private static Label LABEL_NOTES;
	@PlugKey("storefront.edit.button.selectuser")
	private static Label LABEL_SELECT_USER;
	@PlugKey("storefront.edit.button.changeuser")
	private static Label LABEL_CHANGE_USER;
	@PlugKey("storefront.edit.label.minone.paymenttype")
	private static Label LABEL_MIN_ONE_PAYMENT_TYPE;

	@PlugKey("storefront.edit.error.mandatory.registrationname")
	private static Label LABEL_ERROR_REGNAME;

	@PlugKey("storefront.edit.taxtype.none")
	private static String TAXTYPE_NONE;

	@PlugKey("storefront.edit.allowfree")
	@Component(name = "af", stateful = false)
	private Checkbox allowFree;
	@PlugKey("storefront.edit.allowpurchase")
	@Component(name = "ap", stateful = false)
	private Checkbox allowPurchase;
	@PlugKey("storefront.edit.allowsubscription")
	@Component(name = "as", stateful = false)
	private Checkbox allowSubscription;
	@Component(name = "tt", stateful = false)
	private SingleSelectionList<TaxType> taxType;

	@Component(name = "pn", stateful = false)
	private TextField productName;
	@Component(name = "pv", stateful = false)
	private TextField productVersion;
	@Component(name = "cl", stateful = false)
	private SingleSelectionList<Locale> country;
	@Component(name = "ci", stateful = false)
	private TextField clientId;
	@Component(name = "ru", stateful = false)
	private TextField redirectUrl;
	@Inject
	@Component(name = "sud", stateful = false)
	private SelectUserDialog selectUserDialog;
	@Component(name = "sub")
	private Button selectUserButton;
	@Component(name = "cm", stateful = false)
	private TextField contactNumber;
	@Component(name = "e", stateful = false)
	private Checkbox enabled;

	@Inject
	private StoreFrontService storeFrontService;
	@Inject
	private UserLinkService userLinkService;
	@Inject
	private TaxService taxService;
	@Inject
	private BundleCache bundleCache;
	private UserLinkSection userLinkSection;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<StoreFrontEditingBean, StoreFront> session)
	{
		final StoreFrontEditorModel model = getModel(context);
		final StoreFrontEditingBean sfb = session.getBean();

		final String userId = sfb.getClient().getUserId();
		if( userId != null )
		{
			model.setUser(userLinkSection.createLink(context, userId));
			selectUserButton.setLabel(context, LABEL_CHANGE_USER);
		}
		else
		{
			selectUserButton.setLabel(context, LABEL_SELECT_USER);
		}

		return view.createResult("storefront/editstorefront.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		country.setListModel(new CountryListModel());

		selectUserDialog.setAjax(true);
		selectUserDialog.setMultipleUsers(false);
		selectUserButton.setClickHandler(selectUserDialog.getOpenFunction());

		JSCallable inplace = ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);
		selectUserDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("selectUser"),
			inplace, "userAjaxDiv"));

		taxType.setListModel(new TaxTypeListModel());
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@EventHandlerMethod
	public void selectUser(SectionInfo info, String userJson)
	{
		final StoreFrontEditingSession session = loadFromSession(info);
		final StoreFrontEditingBean bean = session.getBean();
		final SelectedUser user = SelectUserDialog.userFromJsonString(userJson);
		// SelectUserDialog allows 'select this user' operation without any
		// actual selection
		if( user != null )
		{
			bean.getClient().setUserId(user.getUuid());
		}
	}

	@EventHandlerMethod
	public void clearUser(SectionInfo info)
	{
		final StoreFrontEditingSession session = loadFromSession(info);
		final StoreFrontEditingBean bean = session.getBean();
		bean.getClient().setUserId(null);
	}

	@Override
	protected AbstractEntityService<StoreFrontEditingBean, StoreFront> getEntityService()
	{
		return storeFrontService;
	}

	@Override
	protected StoreFront createNewEntity(SectionInfo info)
	{
		return new StoreFront();
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<StoreFrontEditingBean, StoreFront> session)
	{
		final StoreFrontEditingBean sfb = session.getBean();
		productName.setValue(info, sfb.getProduct());
		productVersion.setValue(info, sfb.getProductVersion());
		final OAuthClientEditingBean client = sfb.getClient();
		clientId.setValue(info, client.getClientId());
		redirectUrl.setValue(info, client.getRedirectUrl());

		country.setSelectedStringValue(info, sfb.getCountry());

		allowFree.setChecked(info, sfb.isAllowFree());
		allowPurchase.setChecked(info, sfb.isAllowPurchase());
		allowSubscription.setChecked(info, sfb.isAllowSubscription());
		taxType.setSelectedValue(info, sfb.getTaxType());

		contactNumber.setValue(info, sfb.getContactPhone());

		enabled.setChecked(info, sfb.isEnabled());
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<StoreFrontEditingBean, StoreFront> session,
		boolean validate)
	{
		final StoreFrontEditingBean sfb = session.getBean();

		sfb.setProduct(productName.getValue(info));
		sfb.setProductVersion(productVersion.getValue(info));
		final OAuthClientEditingBean client = sfb.getClient();
		client.setClientId(clientId.getValue(info));
		client.setRedirectUrl(redirectUrl.getValue(info));

		sfb.setCountry(country.getSelectedValueAsString(info));

		sfb.setAllowFree(allowFree.isChecked(info));
		sfb.setAllowPurchase(allowPurchase.isChecked(info));
		sfb.setAllowSubscription(allowSubscription.isChecked(info));
		sfb.setTaxType(taxType.getSelectedValue(info));

		sfb.setContactPhone(contactNumber.getValue(info));

		sfb.setEnabled(enabled.isChecked(info));
	}

	/**
	 * There must be one at least of the payment types selected.<br>
	 * Note that we decline the option of providing for saving 3 blank payment
	 * types when enabled is deselected, because it is possible to enable a
	 * disabled store front without loading the editor.
	 */
	@Override
	protected void validate(SectionInfo info, EntityEditingSession<StoreFrontEditingBean, StoreFront> session)
	{
		super.validate(info, session);
		if( !(allowFree.isChecked(info) || allowPurchase.isChecked(info) || allowSubscription.isChecked(info)) )
		{
			session.getValidationErrors().put("minOnePaymentType", LABEL_MIN_ONE_PAYMENT_TYPE);
		}
	}

	@Override
	protected Label getTitleMandatoryErrorLabel()
	{
		return LABEL_ERROR_REGNAME;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new StoreFrontEditorModel();
	}

	public Checkbox getAllowFree()
	{
		return allowFree;
	}

	public Checkbox getAllowPurchase()
	{
		return allowPurchase;
	}

	public Checkbox getAllowSubscription()
	{
		return allowSubscription;
	}

	public TextField getContactNumber()
	{
		return contactNumber;
	}

	@Override
	public Label getTitleLabel()
	{
		return LABEL_REGNAME;
	}

	@Override
	public Label getDescriptionLabel()
	{
		return LABEL_NOTES;
	}

	public TextField getProductName()
	{
		return productName;
	}

	public TextField getProductVersion()
	{
		return productVersion;
	}

	public SingleSelectionList<Locale> getCountry()
	{
		return country;
	}

	public TextField getClientId()
	{
		return clientId;
	}

	public TextField getRedirectUrl()
	{
		return redirectUrl;
	}

	public SelectUserDialog getSelectUserDialog()
	{
		return selectUserDialog;
	}

	public Button getSelectUserButton()
	{
		return selectUserButton;
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	public SingleSelectionList<TaxType> getTaxType()
	{
		return taxType;
	}

	public class StoreFrontEditorModel
		extends
			AbstractEntityEditor<StoreFrontEditingBean, StoreFront, StoreFrontEditorModel>.AbstractEntityEditorModel
	{
		private HtmlLinkState user;

		public HtmlLinkState getUser()
		{
			return user;
		}

		public void setUser(HtmlLinkState user)
		{
			this.user = user;
		}
	}

	public class TaxTypeListModel extends DynamicHtmlListModel<TaxType>
	{
		public TaxTypeListModel()
		{
			setSort(true);
		}

		@Override
		protected Iterable<TaxType> populateModel(SectionInfo info)
		{
			return taxService.enumerateEnabled();
		}

		@Override
		protected Option<TaxType> getTopOption()
		{
			return new KeyOption<TaxType>(TAXTYPE_NONE, "", null);
		}

		@Override
		protected Option<TaxType> convertToOption(SectionInfo info, TaxType obj)
		{
			return new NameValueOption<TaxType>(new BundleNameValue(obj.getName(), "" + obj.getId(), bundleCache), obj);
		}
	}
}
