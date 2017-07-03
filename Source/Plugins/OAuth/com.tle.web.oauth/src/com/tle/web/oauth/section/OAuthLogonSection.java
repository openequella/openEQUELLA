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

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.services.user.UserService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.exceptions.BadCredentialsException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.oauth.OAuthException;
import com.tle.web.oauth.OAuthWebConstants;
import com.tle.web.oauth.service.OAuthWebService;
import com.tle.web.oauth.service.OAuthWebService.AuthorisationDetails;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.template.Decorations;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

@Bind
@SuppressWarnings("nls")
public class OAuthLogonSection extends AbstractPrototypeSection<OAuthLogonSection.OAuthLogonModel>
	implements
		HtmlRenderer,
		AfterParametersListener,
		BookmarkEventListener
{
	//private static final Logger LOGGER = Logger.getLogger(OAuthLogonSection.class);

	@PlugKey("oauth.error.clientnotfound")
	private static String KEY_CLIENT_NOT_FOUND;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private OAuthService oauthService;
	@Inject
	private OAuthWebService oauthWebService;
	@Inject
	private UserService userService;
	@Inject
	private InstitutionService institutionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component(stateful = false, parameter = "username")
	@PlugKey("logon.username")
	private TextField username;
	@Component(stateful = false, parameter = "password")
	@PlugKey("logon.password")
	private TextField password;
	@PlugKey("logon.deny")
	@Component
	private Button denyButton;
	@PlugKey("logon.allow")
	@Component
	private Button allowButton;
	@Component
	@PlugKey("logon.authorise")
	private Button authButton;
	@Component
	@PlugKey("logon.button.logout")
	private Button logoutButton;

	@PlugKey("oauth.error.accessdenied")
	private static Label LABEL_ERROR_DENIED;
	@PlugKey("oauth.error.invalidresponsetype")
	private static String KEY_ERROR_INVALIDRESPONSETYPE;
	@PlugKey("oauth.error.parammandatory")
	private static String KEY_ERROR_MANDATORY;
	@PlugKey("logon.error.badcredentials")
	private static Label LABEL_ERROR_CREDENTIALS;
	@PlugKey("logon.title")
	private static Label LABEL_TITLE;
	@PlugKey("oauth.error.defaultredirectnotapplicable")
	private static Label LABEL_DEFAULTREDIRECT;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations decorations = Decorations.getDecorations(context);
		decorations.clearAllDecorations();
		decorations.setTitle(LABEL_TITLE);
		OAuthLogonModel model = getModel(context);
		context.getBody().addClass("oauth");
		TagState container = new TagState();
		container.setId("oauthdialog");
		container.addClass("oauth" + model.getDisplay());
		model.setContainerDiv(new DivRenderer(container));

		if( !CurrentUser.isGuest() && !CurrentUser.getUserState().wasAutoLoggedIn() )
		{
			model.setAlreadyLoggedIn(true);
			model.setUsername(CurrentUser.getUsername());
			String fixedUserId = model.getFixedUserId();
			model.setCannotUse(fixedUserId != null && !fixedUserId.equals(CurrentUser.getUserID()));
		}
		return viewFactory.createResult("oauthlogon.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		denyButton.setClickHandler(events.getNamedHandler("denyAccess"));
		allowButton.setClickHandler(events.getNamedHandler("allowAccess"));
		authButton.setClickHandler(events.getNamedHandler("authorise"));
		logoutButton.setClickHandler(events.getNamedHandler("logout"));
	}

	@EventHandlerMethod(preventXsrf = false)
	public void denyAccess(SectionInfo info)
	{
		sendError(info, OAuthConstants.ERROR_ACCESS_DENIED, LABEL_ERROR_DENIED);
	}

	@EventHandlerMethod(preventXsrf = false)
	public void allowAccess(SectionInfo info)
	{
		if( CurrentUser.isGuest() )
		{
			throw new AccessDeniedException("No user in session must authorise");
		}
		OAuthLogonModel model = getModel(info);
		OAuthClient oAuthClient = model.getOAuthClient();
		AuthorisationDetails details = oauthWebService.getAuthorisationDetailsByUserState(oAuthClient,
			CurrentUser.getUserState());
		sendSucess(info, oAuthClient, details);
	}

	@EventHandlerMethod(preventXsrf = false)
	public void logout(SectionInfo info)
	{
		URI loggedOutUri = URI.create(info.getPublicBookmark().getHref());
		loggedOutUri = userService.logoutURI(CurrentUser.getUserState(), loggedOutUri);
		HttpServletRequest request = info.getRequest();
		userService.logoutToGuest(userService.getWebAuthenticationDetails(request), false);
		info.forwardToUrl(userService.logoutRedirect(loggedOutUri).toString());
	}

	private void sendError(SectionInfo info, String error, Label errorDescription)
	{
		OAuthLogonModel model = getModel(info);
		model.getOAuthClient();
		String redirectUri = model.getRedirectUri();
		String responseType = model.getResponseType();
		UriBuilder uri = UriBuilder.create(getActualRedirect(responseType, redirectUri));
		QueryBuilder qbuilder = getQueryBuilder(responseType, uri);
		qbuilder.setParam(OAuthWebConstants.PARAM_ERROR, error);
		if( errorDescription != null )
		{
			qbuilder.setParam(OAuthWebConstants.PARAM_ERROR_DESCRIPTION, errorDescription.getText());
		}
		addStateParam(model, qbuilder);
		if( OAuthWebConstants.RESPONSE_TYPE_TOKEN.equals(responseType) )
		{
			uri.setFragment(qbuilder);
		}
		else
		{
			uri.setQuery(qbuilder);
		}
		info.forwardToUrl(uri.toString());
	}

	private QueryBuilder getQueryBuilder(String responseType, UriBuilder uri)
	{
		QueryBuilder qbuilder = QueryBuilder.create();
		if( responseType == null || !responseType.equals(OAuthWebConstants.RESPONSE_TYPE_TOKEN) )
		{
			if( uri.getQuery() != null )
			{
				qbuilder.parse(uri.getQuery());
			}
		}
		return qbuilder;
	}

	private void sendSucess(SectionInfo info, OAuthClient oAuthClient, AuthorisationDetails details)
	{
		OAuthLogonModel model = getModel(info);
		String redirectUri = model.getRedirectUri();
		String responseType = model.getResponseType();
		UriBuilder uri = UriBuilder.create(getActualRedirect(responseType, redirectUri));
		QueryBuilder qbuilder = getQueryBuilder(responseType, uri);
		addStateParam(model, qbuilder);
		if( responseType.equals(OAuthWebConstants.RESPONSE_TYPE_TOKEN) )
		{
			OAuthToken token = oauthService.getOrCreateToken(details.getUserId(), details.getUsername(), oAuthClient,
				null);
			qbuilder.addParam(OAuthWebConstants.PARAM_ACCESS_TOKEN, token.getToken());
			qbuilder.addParam(OAuthWebConstants.PARAM_TOKEN_TYPE, OAuthWebConstants.TOKEN_TYPE_EQUELLA_API);
			uri.setFragment(qbuilder);
		}
		else if( responseType.equals(OAuthWebConstants.RESPONSE_TYPE_CODE) )
		{
			String code = oauthWebService.createCode(oAuthClient, details);
			qbuilder.addParam(OAuthWebConstants.PARAM_CODE, code);
			uri.setQuery(qbuilder);
		}
		info.forwardToUrl(uri.toString());
	}

	private void addStateParam(OAuthLogonModel model, QueryBuilder qbuilder)
	{
		String state = model.getState();
		if( state != null )
		{
			qbuilder.addParam(OAuthWebConstants.PARAM_STATE, state);
		}
	}

	private String getActualRedirect(String responseType, String redirectUri)
	{
		if( redirectUri.equals(OAuthWebConstants.OAUTH_DEFAULT_REDIRECT_URL_NAME) )
		{
			if( responseType == null || !responseType.equals(OAuthWebConstants.RESPONSE_TYPE_TOKEN) )
			{
				throw new OAuthException(400, OAuthConstants.ERROR_INVALID_CLIENT, LABEL_DEFAULTREDIRECT.getText());
			}
			return institutionService.institutionalise(OAuthWebConstants.OAUTH_DEFAULT_REDIRECT_URL);
		}
		return redirectUri;
	}

	@EventHandlerMethod(preventXsrf = false)
	public void authorise(SectionInfo info)
	{
		OAuthLogonModel model = getModel(info);
		try
		{
			String logonUsername = model.getFixedUsername();
			if( logonUsername == null )
			{
				logonUsername = username.getValue(info);
			}
			UserState userState = userService.authenticate(logonUsername, password.getValue(info),
				userService.getWebAuthenticationDetails(info.getRequest()));
			OAuthClient oAuthClient = model.getOAuthClient();
			AuthorisationDetails details = oauthWebService.getAuthorisationDetailsByUserState(oAuthClient, userState);
			sendSucess(info, oAuthClient, details);
		}
		catch( BadCredentialsException bce )
		{
			info.preventGET();
			model.setAuthError(LABEL_ERROR_CREDENTIALS);
		}
	}

	@Override
	public void afterParameters(SectionInfo info, ParametersEvent event)
	{
		OAuthLogonModel model = getModel(info);
		model.getOAuthClient();
		String responseType = model.getResponseType();
		if( responseType == null )
		{
			sendMissingParameter(info, OAuthWebConstants.PARAM_RESPONSE_TYPE);
			return;
		}
		if( !OAuthWebConstants.RESPONSE_TYPES_ALL.contains(responseType) )
		{
			sendError(info, OAuthConstants.ERROR_UNSUPPORTED_RESPONSE_TYPE,
				new KeyLabel(KEY_ERROR_INVALIDRESPONSETYPE, responseType));
		}
	}

	private void sendMissingParameter(SectionInfo info, String param)
	{
		sendError(info, OAuthConstants.ERROR_INVALID_REQUEST, new KeyLabel(KEY_ERROR_MANDATORY, param));
	}

	@Override
	public void bookmark(SectionInfo info, BookmarkEvent event)
	{
		HttpServletRequest request = info.getRequest();
		if( request != null )
		{
			Map<String, String[]> state = userService.getAdditionalLogonState(request);
			for( Entry<String, String[]> entry : state.entrySet() )
			{
				event.setParams(entry.getKey(), entry.getValue());
			}
		}

	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		// nothing
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new OAuthLogonModel();
	}

	public Button getDenyButton()
	{
		return denyButton;
	}

	public Button getAllowButton()
	{
		return allowButton;
	}

	public Button getAuthButton()
	{
		return authButton;
	}

	public Button getLogoutButton()
	{
		return logoutButton;
	}

	public TextField getUsername()
	{
		return username;
	}

	public TextField getPassword()
	{
		return password;
	}

	public class OAuthLogonModel
	{
		@Bookmarked(supported = true, parameter = OAuthWebConstants.PARAM_CLIENT_ID)
		private String clientId;
		@Bookmarked(supported = true, parameter = OAuthWebConstants.PARAM_REDIRECT_URI)
		private String redirectUri;
		@Bookmarked(supported = true, parameter = OAuthWebConstants.PARAM_RESPONSE_TYPE)
		private String responseType;
		@Bookmarked(supported = true, parameter = OAuthWebConstants.PARAM_STATE)
		private String state;
		@Bookmarked(supported = true, parameter = OAuthWebConstants.PARAM_DISPLAY)
		private String display = OAuthWebConstants.DISPLAY_PAGE;

		private DivRenderer containerDiv;
		private boolean alreadyLoggedIn;
		private boolean cannotUse;
		private OAuthClient oauthClient;
		private Label authError;
		private String username;
		private TemplateResult parts;

		public OAuthClient getOAuthClient()
		{
			if( oauthClient == null )
			{
				oauthClient = oauthService.getByClientIdAndRedirectUrl(clientId, redirectUri);
				if( oauthClient == null )
				{
					throw new OAuthException(400, OAuthConstants.ERROR_INVALID_CLIENT,
						new KeyLabel(KEY_CLIENT_NOT_FOUND, clientId, redirectUri).getText(), true);
				}
			}
			return oauthClient;
		}

		public Label getClientName() throws OAuthException
		{
			return new BundleLabel(getOAuthClient().getName(), bundleCache);
		}

		public String getFixedUserId()
		{
			return getOAuthClient().getUserId();
		}

		public String getFixedUsername()
		{
			final String userId = getFixedUserId();
			if( userId != null )
			{
				final UserBean ub = userService.getInformationForUser(userId);
				if( ub != null )
				{
					return ub.getUsername();
				}
			}
			return null;
		}

		public String getClientId()
		{
			return clientId;
		}

		public void setClientId(String clientId)
		{
			this.clientId = clientId;
		}

		public String getRedirectUri()
		{
			return redirectUri;
		}

		public void setRedirectUri(String redirectUri)
		{
			this.redirectUri = redirectUri;
		}

		public String getResponseType()
		{
			return responseType;
		}

		public void setResponseType(String responseType)
		{
			this.responseType = responseType;
		}

		public String getState()
		{
			return state;
		}

		public void setState(String state)
		{
			this.state = state;
		}

		public String getDisplay()
		{
			return display;
		}

		public void setDisplay(String display)
		{
			this.display = display;
		}

		public boolean isAlreadyLoggedIn()
		{
			return alreadyLoggedIn;
		}

		public void setAlreadyLoggedIn(boolean alreadyLoggedIn)
		{
			this.alreadyLoggedIn = alreadyLoggedIn;
		}

		public boolean isCannotUse()
		{
			return cannotUse;
		}

		public void setCannotUse(boolean cannotUse)
		{
			this.cannotUse = cannotUse;
		}

		public DivRenderer getContainerDiv()
		{
			return containerDiv;
		}

		public void setContainerDiv(DivRenderer containerDiv)
		{
			this.containerDiv = containerDiv;
		}

		public Label getAuthError()
		{
			return authError;
		}

		public void setAuthError(Label authError)
		{
			this.authError = authError;
		}

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public TemplateResult getParts()
		{
			return parts;
		}

		public void setParts(TemplateResult parts)
		{
			this.parts = parts;
		}
	}
}
