/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.oauth.section;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.institution.InstitutionService;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.OAuthFlowDefinition;
import com.tle.core.oauth.OAuthFlowDefinitions;
import com.tle.core.oauth.service.OAuthClientEditingBean;
import com.tle.core.oauth.service.OAuthClientEditingSession;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.DebugSettings;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.oauth.OAuthWebConstants;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland, Dustin
 */
@SuppressWarnings("nls")
@TreeIndexed
public class OAuthClientEditorSection extends AbstractPrototypeSection<OAuthClientEditorSection.OAuthClientEditorModel>
	implements
		HtmlRenderer,
		ModalOAuthSection
{
	private static final Logger LOGGER = Logger.getLogger(OAuthClientEditorSection.class);

	@PlugKey("client.editor.label.pagetitle.new")
	private static Label LABEL_CREATE_PAGETITLE;
	@PlugKey("client.editor.label.pagetitle.edit")
	private static Label LABEL_EDIT_PAGETITLE;
	@PlugKey("client.editor.warning.navigateaway")
	private static Label LABEL_WARNING_NAVIGATEAWAY;
	@PlugKey("client.editor.button.selectuser")
	private static Label LABEL_SELECT_USER;
	@PlugKey("client.editor.button.changeuser")
	private static Label LABEL_CHANGE_USER;
	@PlugKey("client.editor.confirm.save")
	private static Confirm CONFIRM_SAVE;
	@PlugKey("client.editor.error.accessdenied")
	private static String KEY_ERROR_ACCESS_DENIED;
	@PlugKey("client.editor.error.title.mandatory")
	private static String KEY_ERROR_NAME;
	@PlugKey("client.editor.error.clientid.mandatory")
	private static String KEY_ERROR_CLIENTID;
	@PlugKey("client.editor.error.clientid.unique")
	private static String KEY_ERROR_CLIENTID_UNIQUE;
	@PlugKey("client.editor.error.redirecturl.mandatory")
	private static String KEY_ERROR_REDIRECTURL;
	@PlugKey("client.editor.error.redirecturl.invalid")
	private static String KEY_ERROR_INVALID_REDIRECTURL;
	@PlugKey("client.editor.error.flowtype.mandatory")
	private static String KEY_ERROR_FLOWTYPE;
	@PlugKey("client.editor.error.user.mandatory")
	private static String KEY_ERROR_USER;
	@PlugKey("client.editor.flow.choosetype")
	private static String CHOOSE_TYPE_KEY;
	@PlugKey("client.editor.confirm.regen")
	private static Confirm DELETE_CONFIRM;

	@PlugKey("client.editor.chooseurl.false")
	private static String CHOOSE_URL_FALSE;
	@PlugKey("client.editor.chooseurl.true")
	private static String CHOOSE_URL_TRUE;

	@Inject
	@Component(name = "n", stateful = false)
	private MultiEditBox nameField;
	@Component(name = "i", stateful = false)
	private TextField clientIdField;
	@PlugKey("client.editor.button.reset")
	@Component(name = "rs", stateful = false)
	private Button resetSecretButton;
	@Component(name = "r", stateful = false)
	private TextField redirectUrlField;
	@Component(name = "rd", stateful = false)
	private Div redirectUrlDiv;
	@Inject
	@Component(name = "sud", stateful = false)
	private SelectUserDialog selectUserDialog;
	@Component(name = "su", stateful = false)
	private Button selectUserButton;
	@PlugKey("client.editor.button.clearuser")
	@Component(name = "cu", stateful = false)
	private Button clearUserButton;

	@Component(name = "sf", stateful = false)
	private SingleSelectionList<OAuthFlowDefinition> selectFlow;
	@Component(name = "cu", stateful = false)
	private SingleSelectionList<NameValue> chooseUrl;
	@Component(name = "du", stateful = false)
	private Checkbox defaultUrl;

	@PlugKey("client.editor.button.save")
	@Component(name = "sv", stateful = false)
	private Button saveButton;
	@PlugKey("client.editor.button.cancel")
	@Component(name = "cl", stateful = false)
	private Button cancelButton;

	// would be better if we can not do this bollocks
	@TreeLookup
	private OneColumnLayout<?> rootSection;

	@Inject
	private OAuthService oauthService;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@ResourceHelper
	private PluginResourceHelper resources;

	private JSHandler saveHandler;

	@Override
	public final SectionResult renderHtml(RenderEventContext context)
	{
		final OAuthClientEditorModel model = getModel(context);
		final OAuthClientEditingSession session = oauthService.loadSession(model.getSessionId());
		final OAuthClient oauth = session.getEntity();

		// check edit priv
		if( oauth.getId() == 0 )
		{
			ensureCreatePriv();
		}
		else if( !oauthService.canEdit(oauth) )
		{
			throw new AccessDeniedException(
				CurrentLocale.get(KEY_ERROR_ACCESS_DENIED, OAuthConstants.PRIV_EDIT_OAUTH_CLIENT));
		}

		model.setEntityUuid(session.getBean().getUuid());

		if( oauth.getUserId() != null )
		{
			model.setFixedUser(userLinkSection.createLink(context, oauth.getUserId()));
			selectUserButton.setLabel(context, LABEL_CHANGE_USER);
		}
		else
		{
			selectUserButton.setLabel(context, LABEL_SELECT_USER);
		}

		model.setDefaultRedirectUrl(institutionService.institutionalise(OAuthWebConstants.OAUTH_DEFAULT_REDIRECT_URL));
		model.setErrors(session.getValidationErrors());

		OverrideHandler editHandler = new OverrideHandler(saveHandler);

		if( isEditExisting(context) )
		{
			editHandler.addValidator(CONFIRM_SAVE);
		}
		saveButton.setClickHandler(context, editHandler);

		if( !DebugSettings.isAutoTestMode() )
		{
			context.getBody().addEventStatements(JSHandler.EVENT_BEFOREUNLOAD,
				new ReturnStatement(LABEL_WARNING_NAVIGATEAWAY));
		}

		return view.createResult("oauthclient.ftl", context);
	}

	private boolean isEditExisting(SectionInfo info)
	{
		return getClient(info).getId() != 0;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		selectUserDialog.setAjax(true);
		// selectUserDialog.setOkLabel(OK_LABEL);
		selectUserDialog.setMultipleUsers(false);
		selectUserButton.setClickHandler(selectUserDialog.getOpenFunction());
		clearUserButton.setClickHandler(
			ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("clearUser"), "userAjaxDiv"));
		UpdateDomFunction regenSecret = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("resetSecret"),
			"clientSecretDiv");
		JSHandler regen = new OverrideHandler(regenSecret).addValidator(DELETE_CONFIRM);
		resetSecretButton.setClickHandler(regen);

		selectFlow.setListModel(new DynamicHtmlListModel<OAuthFlowDefinition>()
		{
			@Override
			protected Iterable<OAuthFlowDefinition> populateModel(SectionInfo info)
			{
				return OAuthFlowDefinitions.getAll();
			}

			@Override
			protected Option<OAuthFlowDefinition> getTopOption()
			{
				return new KeyOption<OAuthFlowDefinition>(CHOOSE_TYPE_KEY, "", null);
			}

			@Override
			protected Option<OAuthFlowDefinition> convertToOption(SectionInfo info, OAuthFlowDefinition obj)
			{
				return new NameValueOption<OAuthFlowDefinition>(
					new NameValue(resources.getString(obj.getNameKey()), obj.getId()), obj);
			}
		});
		chooseUrl.setListModel(
			new SimpleHtmlListModel<NameValue>(new NameValue(CurrentLocale.get(CHOOSE_URL_FALSE), "false"),
				new NameValue(CurrentLocale.get(CHOOSE_URL_TRUE), "true")));

		JSCallable inplace = ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);
		selectUserDialog.setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("selectUser"), inplace, "userAjaxDiv"));

		selectFlow.addChangeEventHandler(
			ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("typeSelected"), inplace, "flowAjaxDiv"));
		chooseUrl.addChangeEventHandler(ajax.getAjaxUpdateDomFunction(tree, null,
			events.getEventHandler("chooseUrlSelected"), inplace, "flowAjaxDiv"));

		saveHandler = events.getNamedHandler("save");
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
	}

	private void ensureCreatePriv()
	{
		if( aclService.filterNonGrantedPrivileges(OAuthConstants.PRIV_CREATE_OAUTH_CLIENT).isEmpty() )
		{
			throw new AccessDeniedException(
				CurrentLocale.get(KEY_ERROR_ACCESS_DENIED, OAuthConstants.PRIV_CREATE_OAUTH_CLIENT));
		}
	}

	/**
	 * Srsly, this should be in the service
	 * 
	 * @param info
	 * @param oauth
	 * @param errors
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean validate(SectionInfo info, OAuthClientEditingBean oauth, Map<String, Object> errors)
	{
		errors.clear();

		final LanguageBundleBean bundle = nameField.getLanguageBundle(info);
		if( LangUtils.isEmpty(bundle) )
		{
			errors.put("name", CurrentLocale.get(KEY_ERROR_NAME));
		}
		final String clientId = clientIdField.getValue(info);
		if( Check.isEmpty(clientId) )
		{
			errors.put("clientid", CurrentLocale.get(KEY_ERROR_CLIENTID));
		}
		else
		{
			// check to see if client id exists
			final OAuthClient old = oauthService.getByClientIdOnly(clientId);
			if( old != null && old.getId() != oauth.getId() )
			{
				errors.put("clientid", CurrentLocale.get(KEY_ERROR_CLIENTID_UNIQUE));
			}
		}

		if( selectFlow.getSelectedValue(info) == null )
		{
			errors.put("flowType", CurrentLocale.get(KEY_ERROR_FLOWTYPE));
			return false;
		}

		final OAuthClientEditorModel model = getModel(info);
		final OAuthClientEditingSession session = oauthService.loadSession(model.getSessionId());

		if( selectFlow.getSelectedValue(info).isSetUser() && session.getEntity().getUserId() == null )
		{
			errors.put("selectUser", CurrentLocale.get(KEY_ERROR_USER));
		}

		final String redirectUrl = redirectUrlField.getValue(info);

		if( Check.isEmpty(redirectUrl) && selectFlow.getSelectedValue(info).isSetUrl()
			&& (!selectFlow.getSelectedValue(info).isUseInbuiltUrl()
				|| "true".equals(chooseUrl.getSelectedValue(info).getValue())) )
		{
			errors.put("redirecturl", CurrentLocale.get(KEY_ERROR_REDIRECTURL));
		}
		if( redirectUrl != null && !"default".equals(redirectUrl) )
		{
			try
			{
				new URL(redirectUrl);
			}
			catch( MalformedURLException m )
			{
				errors.put("redirecturl", CurrentLocale.get(KEY_ERROR_INVALID_REDIRECTURL));
			}
		}

		return errors.isEmpty();
	}

	private OAuthClientEditingSession saveToSession(SectionInfo info, OAuthClientEditingSession session,
		boolean validate)
	{
		final OAuthClientEditingBean oauth = session.getBean();
		if( validate )
		{
			session.setValid(validate(info, oauth, session.getValidationErrors()));
		}
		else
		{
			session.getValidationErrors().clear();
		}

		// Save fields even if invalid, it wont be committed until the session
		// is valid
		oauth.setName(nameField.getLanguageBundle(info));
		oauth.setClientId(clientIdField.getValue(info));
		oauth.setFlowDef(selectFlow.getSelectedValue(info));

		if( selectFlow.getSelectedValue(info) != null && selectFlow.getSelectedValue(info).isUseInbuiltUrl() )
		{
			if( selectFlow.getSelectedValue(info).isSetUrl()
				&& "true".equals(chooseUrl.getSelectedValue(info).getValue()) )
			{
				oauth.setRedirectUrl(redirectUrlField.getValue(info));
			}
			else
			{
				oauth.setRedirectUrl(selectFlow.getSelectedValue(info).getRedirectUrl());
			}
		}
		else
		{
			oauth.setRedirectUrl(redirectUrlField.getValue(info));
		}

		if( selectFlow.getSelectedValue(info) != null && selectFlow.getSelectedValue(info).isSetUser() )
		{
			// preserve the 'Fixed user' from the user selector
			String enteredUserUUid = session.getEntity().getUserId();
			oauth.setUserId(enteredUserUUid);
		}
		else
		{
			oauth.setUserId(null);
		}

		oauthService.saveSession(session);
		return session;
	}

	private OAuthClientEditingBean getClient(SectionInfo info)
	{
		return getModel(info).getClient();
	}

	private void startSession(SectionInfo info, OAuthClientEditingSession session)
	{
		getModel(info).setEditing(true);
		final OAuthClientEditingBean oauth = session.getBean();
		oauth.setFlowDef(getFlow(oauth));
		loadInternal(info, session);
		getModel(info).setSessionId(session.getSessionId());
	}

	private void loadInternal(SectionInfo info, OAuthClientEditingSession session)
	{
		final OAuthClientEditingBean oauth = session.getBean();
		final OAuthClientEditorModel model = getModel(info);
		model.setClient(oauth);

		final LanguageBundleBean name = oauth.getName();
		if( name != null )
		{
			nameField.setLanguageBundle(info, name);
		}
		clientIdField.setValue(info, oauth.getClientId());
		model.setClientSecret(oauth.getClientSecret());
		selectFlow.setSelectedValue(info, oauth.getFlowDef());
		model.setFlow(oauth.getFlowDef());

		String redirect = oauth.getRedirectUrl() == null ? "" : oauth.getRedirectUrl();

		if( !"default".equals(redirect) )
		{
			redirectUrlField.setValue(info, redirect);
		}
		if( chooseUrl.getSelectedValue(info) == null )
		{
			chooseUrl.setSelectedStringValue(info, Boolean.toString(!"default".equals(redirect)));
			model.setShowSetUrl(!"default".equals(redirect));
		}
		else
		{
			model.setShowSetUrl(Boolean.parseBoolean(chooseUrl.getSelectedValue(info).getValue()));
		}
	}

	private OAuthFlowDefinition getFlow(OAuthClientEditingBean oauth)
	{
		if( oauth.getName() == null && oauth.getFlowDef() == null )
		{
			// Starting a new one
			return null;
		}
		if( oauth.getFlowDef() != null )
		{
			return oauth.getFlowDef();
		}
		// else
		// boolean urlIsSet = false;
		boolean userSet = !Check.isEmpty(oauth.getUserId());
		boolean isDefaultUrl = false;

		if( !Check.isEmpty(oauth.getRedirectUrl()) )
		{
			// urlIsSet = true;
			isDefaultUrl = "default".equals(oauth.getRedirectUrl());
		}

		if( userSet )
		{
			return OAuthFlowDefinitions.CLIENT_CREDENTIALS_GRANT;
		}
		if( isDefaultUrl )
		{
			return OAuthFlowDefinitions.IMPLICIT_GRANT;
		}
		// if( urlIsSet )
		// {
		return OAuthFlowDefinitions.AUTHORISATION_CODE_GRANT;
		// }
		// return OAuthFlowDefinitions.OAUTH_ONE_GRANT;
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		final OAuthClientEditingSession session = oauthService.loadSession(model.getSessionId());
		saveToSession(info, session, true);
		if( session.isValid() )
		{
			try
			{
				oauthService.commitSession(session);
			}
			catch( InvalidDataException ide )
			{
				LOGGER.error("context", ide);
				session.setValid(false);
				model.setErrors(session.getValidationErrors());
				return;
			}
			model.setSessionId(null);
			returnFromEdit(info);
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		oauthService.cancelSessionId(model.getSessionId());
		model.setSessionId(null);
		returnFromEdit(info);
	}

	@EventHandlerMethod
	public void selectUser(SectionInfo info, String usersJson) throws Exception
	{
		final OAuthClientEditorModel model = getModel(info);
		final OAuthClientEditingSession session = oauthService.loadSession(model.getSessionId());
		final List<SelectedUser> selectedUsers = SelectUserDialog.usersFromJsonString(usersJson);

		if( !Check.isEmpty(selectedUsers) )
		{
			final SelectedUser selectedUser = selectedUsers.get(0);
			session.getEntity().setUserId(selectedUser.getUuid());
		}
		else
		{
			session.getEntity().setUserId(null);
		}
	}

	@EventHandlerMethod
	public void clearUser(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		final OAuthClientEditingSession session = oauthService.loadSession(model.getSessionId());
		session.getEntity().setUserId(null);
	}

	@EventHandlerMethod
	public void resetSecret(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		final OAuthClientEditingSession session = oauthService.loadSession(model.getSessionId());
		final OAuthClientEditingBean bean = session.getBean();
		bean.setClientSecret(UUID.randomUUID().toString());
		model.setClientSecret(bean.getClientSecret());
	}

	@EventHandlerMethod
	public void typeSelected(SectionInfo info)
	{
		OAuthFlowDefinition flow = selectFlow.getSelectedValue(info);
		OAuthClientEditorModel model = getModel(info);
		model.setFlow(flow);
	}

	@EventHandlerMethod
	public void chooseUrlSelected(SectionInfo info)
	{
		OAuthClientEditorModel model = getModel(info);
		if( "true".equals(chooseUrl.getSelectedValue(info).getValue()) )
		{
			model.setShowSetUrl(true);
			return;
		}
		model.setShowSetUrl(false);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		if( model.isEditing() )
		{
			rootSection.setModalSection(info, this);
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void includeHandler(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		if( model.isRendered() )
		{
			final String sessionId = getModel(info).getSessionId();
			if( sessionId != null )
			{
				final OAuthClientEditingSession session = oauthService.loadSession(sessionId);
				saveToSession(info, session, false);
			}
		}
	}

	@DirectEvent
	public void loadFromSession(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		model.setRendered(true);

		final String sessionId = getModel(info).getSessionId();
		if( sessionId != null )
		{
			final OAuthClientEditingSession session = oauthService.loadSession(sessionId);
			loadInternal(info, session);
		}
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		ContentLayout.setLayout(info, ContentLayout.ONE_COLUMN);

		final OAuthClientEditorModel model = getModel(info);
		final Label pageTitle = getPageTitle(info);

		decorations.setContentBodyClass("oauthedit");
		crumbs.setForcedLastCrumb(pageTitle);

		model.setPageTitle(pageTitle);
		decorations.setTitle(pageTitle);
	}

	private Label getPageTitle(SectionInfo info)
	{
		return (isEditExisting(info) ? LABEL_EDIT_PAGETITLE : LABEL_CREATE_PAGETITLE);
	}

	/**
	 * Called from ShowOAuthSection
	 * 
	 * @param info
	 * @param type
	 */
	public void createNew(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		model.setEditing(true);

		final OAuthClientEditingSession session = oauthService.startNewSession(new OAuthClient());
		startSession(info, session);
	}

	/**
	 * Called from ShowOAuthSection
	 * 
	 * @param info
	 * @param oauthUuid
	 * @param type
	 */
	public void startEdit(SectionInfo info, String oauthUuid)
	{
		final OAuthClientEditingSession session = oauthService.startEditingSession(oauthUuid);
		startSession(info, session);
	}

	private void returnFromEdit(SectionInfo info)
	{
		final OAuthClientEditorModel model = getModel(info);
		model.setEditing(false);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new OAuthClientEditorModel();
	}

	public MultiEditBox getNameField()
	{
		return nameField;
	}

	public TextField getClientIdField()
	{
		return clientIdField;
	}

	public Div getRedirectUrlDiv()
	{
		return redirectUrlDiv;
	}

	public TextField getRedirectUrlField()
	{
		return redirectUrlField;
	}

	public SelectUserDialog getSelectUserDialog()
	{
		return selectUserDialog;
	}

	public Button getSelectUserButton()
	{
		return selectUserButton;
	}

	public Button getClearUserButton()
	{
		return clearUserButton;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public SingleSelectionList<OAuthFlowDefinition> getSelectFlow()
	{
		return selectFlow;
	}

	public Button getResetSecretButton()
	{
		return resetSecretButton;
	}

	public SingleSelectionList<NameValue> getChooseUrl()
	{
		return chooseUrl;
	}

	public Checkbox getDefaultUrl()
	{
		return defaultUrl;
	}

	public static class OAuthClientEditorModel
	{
		@Bookmarked(name = "s")
		private String sessionId;
		@Bookmarked(name = "ed")
		private boolean editing;
		@Bookmarked(stateful = false)
		private boolean rendered;
		private String clientSecret;
		// Caching
		private OAuthClientEditingBean client;

		private HtmlLinkState fixedUser;

		private Label pageTitle;
		private Map<String, Object> errors = Maps.newHashMap();
		private String defaultRedirectUrl;
		private OAuthFlowDefinition flow;

		private boolean setUrl;
		private boolean showSetUrl;
		private boolean defaultOption;
		private boolean useInbuiltRedirect;
		private boolean selectUser;

		private String descriptionKey;
		private String entityUuid;

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public boolean isEditing()
		{
			return editing;
		}

		public void setEditing(boolean editing)
		{
			this.editing = editing;
		}

		public boolean isRendered()
		{
			return rendered;
		}

		public void setRendered(boolean rendered)
		{
			this.rendered = rendered;
		}

		public OAuthClientEditingBean getClient()
		{
			return client;
		}

		public void setClient(OAuthClientEditingBean client)
		{
			this.client = client;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public Map<String, Object> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, Object> errors)
		{
			this.errors = errors;
		}

		public String getDefaultRedirectUrl()
		{
			return defaultRedirectUrl;
		}

		public void setDefaultRedirectUrl(String defaultRedirectUrl)
		{
			this.defaultRedirectUrl = defaultRedirectUrl;
		}

		public HtmlLinkState getFixedUser()
		{
			return fixedUser;
		}

		public void setFixedUser(HtmlLinkState fixedUser)
		{
			this.fixedUser = fixedUser;
		}

		public OAuthFlowDefinition getFlow()
		{
			return flow;
		}

		public void setFlow(OAuthFlowDefinition flow)
		{
			this.flow = flow;
			if( flow == null )
			{
				return;
			}
			setUrl = flow.isSetUrl();
			selectUser = flow.isSetUser();
			useInbuiltRedirect = flow.isUseInbuiltUrl();
			descriptionKey = flow.getDescriptionKey();
			defaultOption = flow.isUseInbuiltUrl();
		}

		public boolean isSetUrl()
		{
			return setUrl;
		}

		public boolean isUseInbuiltRedirect()
		{
			return useInbuiltRedirect;
		}

		public boolean isSelectUser()
		{
			return selectUser;
		}

		public String getDescriptionKey()
		{
			return descriptionKey == null ? "client.editor.flow.help" : descriptionKey;
		}

		public String getClientSecret()
		{
			return clientSecret;
		}

		public void setClientSecret(String id)
		{
			this.clientSecret = id;
		}

		public boolean isShowSetUrl()
		{
			return showSetUrl;
		}

		public void setShowSetUrl(boolean showSetUrl)
		{
			this.showSetUrl = showSetUrl;
		}

		public boolean isDefaultOption()
		{
			return defaultOption;
		}

		public String getEntityUuid()
		{
			return entityUuid;
		}

		public void setEntityUuid(String entityUuid)
		{
			this.entityUuid = entityUuid;
		}
	}
}
