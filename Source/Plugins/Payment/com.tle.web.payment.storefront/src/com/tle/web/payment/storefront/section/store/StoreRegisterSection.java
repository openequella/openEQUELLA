package com.tle.web.payment.storefront.section.store;

import java.net.URL;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.payment.storefront.service.session.StoreEditingBean;
import com.tle.core.payment.storefront.service.session.StoreEditingSession;
import com.tle.core.services.UrlService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.DebugSettings;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.storefront.section.store.StoreRegisterSection.StoreRegisterModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@TreeIndexed
@Bind
public class StoreRegisterSection extends AbstractEntityEditor<StoreEditingBean, Store, StoreRegisterModel>
{
	@Inject
	private StoreService storeService;
	@Inject
	private UrlService urlService;
	@Inject
	private ShopService shopService;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	protected EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@PlugKey("store.register.title.new")
	private static Label LABEL_TITLE_NEW;
	@PlugKey("store.register.title.edit")
	private static Label LABEL_TITLE_EDIT;
	@PlugKey("store.register.button.connect")
	private static Label LABEL_BUTTON_CONNECT;
	@PlugKey("store.register.button.reconnect")
	private static Label LABEL_BUTTON_RECONNECT;
	@PlugKey("store.register.error.storeurl.mandatory")
	private static Label LABEL_ERROR_STOREURL_MANDATORY;
	@PlugKey("store.register.error.storeurl.connected")
	private static Label LABEL_ERROR_STOREURL_CONNECTED;
	@PlugKey("store.register.error.clientid.mandatory")
	private static Label LABEL_ERROR_CLIENTID_MANDATORY;
	@PlugKey("store.register.error.clientid.connected")
	private static Label LABEL_ERROR_CLIENTID_CONNECTED;
	@PlugKey("store.register.error.storeurl.response")
	private static Label LABEL_ERROR_RESPONSE;
	@PlugKey("store.register.error.storeurl.registered")
	private static Label LABEL_ERROR_URL_IS_REGISTERED;

	@Component(stateful = false)
	private TextField storeUrl;
	@Component(stateful = false)
	private TextField clientId;
	@Component(name = "e", stateful = false)
	private Checkbox enabled;
	@Component
	private Button connectButton;

	@PlugKey("store.register.button.save")
	@Component(name = "sv", stateful = false)
	private Button saveButton;
	@PlugKey("store.register.button.cancel")
	@Component(name = "cl", stateful = false)
	private Button cancelButton;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final StoreRegisterModel model = getModel(context);
		final EntityEditingSession<StoreEditingBean, Store> session = getEntityService().loadSession(
			model.getSessionId());

		model.setEntityUuid(session.getBean().getUuid());
		model.setErrors(session.getValidationErrors());

		if( !DebugSettings.isAutoTestMode() )
		{
			context.getBody().addEventStatements(JSHandler.EVENT_BEFOREUNLOAD,
				new ReturnStatement(getWarningNavigateAway()));
		}

		final StoreEditingBean bean = session.getBean();
		if( bean.getToken() != null )
		{
			model.setConnected(true);
			connectButton.setLabel(context, LABEL_BUTTON_RECONNECT);
			Decorations.setTitle(context, LABEL_TITLE_EDIT);
		}
		else
		{
			connectButton.setLabel(context, LABEL_BUTTON_CONNECT);
			Decorations.setTitle(context, LABEL_TITLE_NEW);
		}

		return view.createResult("registerstore.ftl", this);
	}

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<StoreEditingBean, Store> session)
	{
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		connectButton.setClickHandler(events.getNamedHandler("connect"));
		saveButton.setClickHandler(events.getNamedHandler("saveevent"));
		cancelButton.setClickHandler(events.getNamedHandler("cancelevent"));
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_HIGH)
	public void checkForCode(SectionInfo info)
	{
		final StoreRegisterModel model = getModel(info);
		final String code = model.getCode();
		if( code != null )
		{
			// make server side request
			final EntityEditingSession<StoreEditingBean, Store> session = loadFromSession(info);
			final StoreEditingBean bean = session.getBean();

			try
			{

				bean.setToken(shopService.readToken(bean.getStoreUrl(), bean.getClientId(),
					urlService.institutionalise("access/registerstore.do"), code));

				// Now have a token, connect to store API and get details
				final StoreBean storeBean = shopService.getStoreInformation(bean.getStoreUrl(), bean.getToken());
				bean.setIcon(storeBean.getIcon());

				final I18NStrings storeName = storeBean.getNameStrings();
				if( storeName == null )
				{
					LanguageBundle tempLangBundle = LangUtils.createTextTempLangugageBundle(storeBean.getName()
						.toString());
					bean.setName(LangUtils.convertBundleToBean(tempLangBundle));
				}
				else
				{
					bean.setName(lbb(storeName));
				}

				final I18NStrings storeDescription = storeBean.getDescriptionStrings();
				if( storeDescription == null )
				{
					LanguageBundle tempLangBundle = LangUtils.createTextTempLangugageBundle(storeBean.getDescription()
						.toString());
					bean.setDescription(LangUtils.convertBundleToBean(tempLangBundle));
				}
				else
				{
					bean.setDescription(lbb(storeDescription));
				}

				bean.setAttribute(Store.FIELD_CONNECTED_URL, bean.getStoreUrl());
				bean.setAttribute(Store.FIELD_CONNECTED_CLIENT_ID, bean.getClientId());

				getEntityService().saveSession(session);

				// Lose the code!
				final SectionInfo fwd = info.createForward("/access/registerstore.do");
				final StoreRegisterSection srs = fwd.lookupSection(StoreRegisterSection.class);
				srs.setState(fwd, model.getState());
				srs.setStoreUrl(fwd, bean.getStoreUrl());
				srs.setClientId(fwd, bean.getClientId());
				srs.setTestStatus(fwd, "ok");

				info.forward(fwd);
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	// TODO: Ewwww
	private LanguageBundleBean lbb(I18NStrings strings)
	{
		if( strings == null )
		{
			return null;
		}
		final LanguageBundleBean bean = new LanguageBundleBean();

		final Map<String, LanguageStringBean> newStrings = Maps.newHashMap();
		for( Map.Entry<String, String> string : strings.getStrings().entrySet() )
		{
			final LanguageStringBean stringBean = new LanguageStringBean();
			final String locale = string.getKey();
			stringBean.setLocale(locale);
			stringBean.setText(string.getValue());

			newStrings.put(locale, stringBean);
		}
		bean.setStrings(newStrings);
		return bean;
	}

	@EventHandlerMethod
	public void connect(SectionInfo info)
	{
		final StoreEditingSession session = loadFromSession(info);
		final StoreEditingBean bean = session.getBean();
		bean.setToken(null);
		validateReg(info, session, false);

		if( session.getValidationErrors().isEmpty() )
		{
			// redirect to gateway
			final Map<String, String> map = Maps.newHashMap();
			map.put("client_id", bean.getClientId());
			map.put("redirect_uri", urlService.institutionalise("access/registerstore.do"));
			map.put("response_type", "code");
			map.put("state", getModel(info).getSessionId());

			URL oauthUrl = URLUtils.newURL(bean.getStoreUrl(), "oauth/authorise?" + URLUtils.getParameterString(map));

			// http://dev.equella.com/issues/5612
			// we can probably remove this if Chrome sorts out the probl
			info.getResponse().addHeader("X-XSS-Protection", "0");

			info.forwardToUrl(oauthUrl.toString());
		}
	}

	@EventHandlerMethod
	public void saveevent(SectionInfo info)
	{
		final StoreRegisterModel model = getModel(info);
		final EntityEditingSession<StoreEditingBean, Store> session = getEntityService().loadSession(
			model.getSessionId());
		// saveToSessionPrivate(info, session, true);
		// bypass saving of name and description
		saveToSession(info, session, true);
		if( session.getValidationErrors().isEmpty() )
		{
			getEntityService().commitSession(session);
			info.forward(info.createForward("/access/store.do"));
		}
	}

	@EventHandlerMethod
	public void cancelevent(SectionInfo info)
	{
		cancel(info);
		final SectionInfo fwd = info.createForward("/access/store.do");
		info.forward(fwd);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	protected AbstractEntityService<StoreEditingBean, Store> getEntityService()
	{
		return storeService;
	}

	@Override
	protected Store createNewEntity(SectionInfo info)
	{
		return new Store();
	}

	@DirectEvent
	public void loadSession(SectionInfo info)
	{
		loadFromSession(info);
		final StoreRegisterModel model = getModel(info);
		model.setRendered(true);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void saveSession(SectionInfo info)
	{
		final StoreRegisterModel model = getModel(info);
		if( model.isRendered() )
		{
			final EntityEditingSession<StoreEditingBean, Store> session = getEntityService().loadSession(
				getModel(info).getSessionId());
			// bypass saving of name and description
			saveToSession(info, session, false);
			getEntityService().saveSession(session);
		}
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<StoreEditingBean, Store> session)
	{
		final StoreEditingBean bean = session.getBean();
		storeUrl.setValue(info, bean.getStoreUrl());
		clientId.setValue(info, bean.getClientId());
		enabled.setChecked(info, bean.isEnabled());
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<StoreEditingBean, Store> session,
		boolean validate)
	{
		final StoreEditingBean bean = session.getBean();
		bean.setStoreUrl(storeUrl.getValue(info));
		bean.setClientId(clientId.getValue(info));
		bean.setEnabled(enabled.isChecked(info));
		if( validate )
		{
			validateReg(info, session, true);
		}
	}

	protected void validateReg(SectionInfo info, EntityEditingSession<StoreEditingBean, Store> session,
		boolean validateConnect)
	{
		getModel(info).setTestStatus(null);
		final Map<String, Object> validationErrors = session.getValidationErrors();
		validationErrors.clear();

		final StoreEditingBean bean = session.getBean();

		final String url = bean.getStoreUrl();

		if( Strings.isNullOrEmpty(url) || !URLUtils.isAbsoluteUrl(url) )
		{
			validationErrors.put("storeUrl", LABEL_ERROR_STOREURL_MANDATORY);
		}
		else if( validateConnect )
		{
			final String connectedUrl = bean.getAttribute(Store.FIELD_CONNECTED_URL);
			if( Check.isEmpty(connectedUrl) || !connectedUrl.equals(url) )
			{
				validationErrors.put("storeUrl", LABEL_ERROR_STOREURL_CONNECTED);
			}
		}

		if( storeService.isUrlRegistered(url, bean.getId()) )
		{
			validationErrors.put("storeUrl", LABEL_ERROR_URL_IS_REGISTERED);
		}

		if( validationErrors.isEmpty() )
		{
			try
			{
				if( !shopService.testStoreUrl(url) )
				{
					validationErrors.put("storeUrl", LABEL_ERROR_RESPONSE);
				}
			}
			catch( Exception e )
			{
				validationErrors.put("storeUrl", e.getMessage());
			}
		}

		if( Strings.isNullOrEmpty(bean.getClientId()) )
		{
			validationErrors.put("clientId", LABEL_ERROR_CLIENTID_MANDATORY);
		}
		else if( validateConnect )
		{
			final String connectedClientId = bean.getAttribute(Store.FIELD_CONNECTED_CLIENT_ID);
			if( Check.isEmpty(connectedClientId) || !connectedClientId.equals(bean.getClientId()) )
			{
				validationErrors.put("clientId", LABEL_ERROR_CLIENTID_CONNECTED);
			}
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new StoreRegisterModel();
	}

	public void setState(SectionInfo info, String state)
	{
		getModel(info).setState(state);
	}

	public void setStoreUrl(SectionInfo info, String store)
	{
		storeUrl.setValue(info, store);
	}

	public void setTestStatus(SectionInfo info, String status)
	{
		getModel(info).setTestStatus(status);
	}

	public void setClientId(SectionInfo info, String client)
	{
		clientId.setValue(info, client);
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	public TextField getClientId()
	{
		return clientId;
	}

	public TextField getStoreUrl()
	{
		return storeUrl;
	}

	public Button getConnectButton()
	{
		return connectButton;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public class StoreRegisterModel
		extends
			AbstractEntityEditor<StoreEditingBean, Store, StoreRegisterModel>.AbstractEntityEditorModel
	{
		@Bookmarked(name = "code", parameter = "code", supported = true, stateful = false)
		private String code;
		@Bookmarked(name = "state", parameter = "state", supported = true)
		private String state;
		private boolean connected;
		@Bookmarked(stateful = false)
		private boolean rendered;
		@Bookmarked(name = "ts", parameter = "ts", supported = true)
		private String testStatus;

		@Override
		public String getSessionId()
		{
			return state;
		}

		@Override
		public void setSessionId(String sessionId)
		{
			state = sessionId;
		}

		public String getCode()
		{
			return code;
		}

		public void setCode(String code)
		{
			this.code = code;
		}

		public String getState()
		{
			return state;
		}

		public void setState(String state)
		{
			this.state = state;
		}

		public boolean isConnected()
		{
			return connected;
		}

		public void setConnected(boolean connected)
		{
			this.connected = connected;
		}

		public boolean isRendered()
		{
			return rendered;
		}

		public void setRendered(boolean rendered)
		{
			this.rendered = rendered;
		}

		public String getTestStatus()
		{
			return testStatus;
		}

		public void setTestStatus(String testStatus)
		{
			this.testStatus = testStatus;
		}
	}
}
