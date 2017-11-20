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

package com.tle.web.connectors.brightspace.editor;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.brightspace.BrightspaceConnectorConstants;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.connectors.service.ConnectorEditingSession;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.connectors.dialog.LMSAuthDialog;
import com.tle.web.connectors.dialog.LMSAuthDialog.LMSAuthUrlCallable;
import com.tle.web.connectors.editor.AbstractConnectorEditorSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class BrightspaceConnectorEditor
	extends
		AbstractConnectorEditorSection<BrightspaceConnectorEditor.BrightspaceConnectorEditorModel>
{
	private static final String POSTFIX_KEY = ".BrightspaceConnectorEditor";

	@PlugKey("editor.testapp.ok")
	private static Label TEST_APP_OK;
	@PlugKey("editor.testapp.fail")
	private static Label TEST_APP_FAIL;
	@PlugKey("editor.admin.ok")
	private static Label ADMIN_OK;
	@PlugKey("editor.admin.fail")
	private static Label ADMIN_FAIL;
	@PlugKey("editor.validation.appid")
	private static Label BAD_APP_ID_ERROR;
	@PlugKey("editor.validation.appkey")
	private static Label BAD_APP_KEY_ERROR;
	@PlugKey("editor.validation.testapp")
	private static Label NOT_TESTED_ERROR;
	@PlugKey("editor.admin.signedinas")
	private static String KEY_SIGNED_IN_AS;

	@Component(stateful = false)
	private TextField appId;
	@Component(stateful = false)
	private TextField appKey;
	@Component
	@PlugKey("editor.button.testapp")
	private Button testAppButton;
	@Inject
	@Component
	private LMSAuthDialog authDialog;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Inject
	private BrightspaceConnectorService brightspaceConnectorService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private UserSessionService userSessionService;

	@PostConstruct
	public void init()
	{
		authDialog.setAuthUrlCallable(new LMSAuthUrlCallable()
		{
			@Override
			public String getAuthorisationUrl(SectionInfo info, String forwardUrl)
			{
				final BrightspaceConnectorEditorModel model = getModel(info);
				final ConnectorEditingSession session = getEntityService().loadSession(model.getSessionId());
				final ConnectorEditingBean connector = session.getBean();

				return brightspaceConnectorService.getAuthorisationUrl(
					connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_ID),
					connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_KEY), connector.getServerUrl(),
					forwardUrl, POSTFIX_KEY);
			}
		});
	}

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<ConnectorEditingBean, Connector> session)
	{
		final BrightspaceConnectorEditorModel model = getModel(context);
		final ConnectorEditingBean connector = session.getBean();

		if( model.getTestedUrl() != null )
		{
			model.setTrustedUrl(institutionService.institutionalise(BrightspaceConnectorConstants.AUTH_URL));

			final String appOkStr = connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_OK);
			if( appOkStr != null )
			{
				final boolean appOk = Boolean.parseBoolean(appOkStr);
				if( appOk )
				{
					model.setLtiConsumersUrl(institutionService.institutionalise("access/lticonsumers.do"));
				}
				model.setTestAppStatusClass(appOk ? "ok" : "fail");
				model.setTestAppStatus(appOk ? TEST_APP_OK.getText() : TEST_APP_FAIL.getText());
				model.setAppOk(appOk);
			}

			final String adminOkStr = connector.getAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_OK);
			if( adminOkStr != null )
			{
				final boolean adminOk = Boolean.parseBoolean(adminOkStr);
				model.setAdminStatusClass(adminOk ? "ok" : "fail");
				final String adminStatusText;
				if( adminOk )
				{
					final String adminUsername = connector
						.getAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_USERNAME);
					adminStatusText = (adminUsername != null ? new KeyLabel(KEY_SIGNED_IN_AS, adminUsername).getText()
						: ADMIN_OK.getText());
				}
				else
				{
					adminStatusText = ADMIN_FAIL.getText();
				}
				model.setAdminStatus(adminStatusText);
				model.setAdminOk(adminOk);
			}
		}
		return view.createResult("brightspaceconnector.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		testAppButton.setClickHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("testApp"), getAjaxDivId()));
		authDialog.setOkCallback(events.getSubmitValuesFunction("adminSignIn"));
	}

	@EventHandlerMethod
	public void testApp(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
		final ConnectorEditingBean connector = session.getBean();

		final String testResponse = brightspaceConnectorService.testApplication(appId.getValue(info),
			appKey.getValue(info), connector.getServerUrl());
		final boolean appOk = "ok".equals(testResponse);
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_APP_OK, appOk);
		getEntityService().saveSession(session);
	}

	@EventHandlerMethod
	public void adminSignIn(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
		final ConnectorEditingBean connector = session.getBean();

		final String userId = userSessionService
			.getAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_ID + POSTFIX_KEY);
		final String userKey = userSessionService
			.getAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_KEY + POSTFIX_KEY);
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_USER_ID, userId);
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_USER_KEY,
			brightspaceConnectorService.encrypt(userKey));
		userSessionService.removeAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_ID + POSTFIX_KEY);
		userSessionService.removeAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_KEY + POSTFIX_KEY);

		final String username = brightspaceConnectorService.whoAmI(
			connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_ID),
			connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_KEY), connector.getServerUrl(), userId,
			userKey);
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_USERNAME, username);

		// TODO: test the admin account for privs? 
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_ADMIN_OK,
			userId != null && userKey != null && username != null);

		getEntityService().saveSession(session);
	}

	@Override
	protected void customValidate(SectionInfo info, ConnectorEditingBean connector, Map<String, Object> errors)
	{
		final BrightspaceConnectorEditorModel model = getModel(info);
		if( Strings.isNullOrEmpty(appId.getValue(info)) )
		{
			errors.put("appid", BAD_APP_ID_ERROR.getText());
			return;
		}

		if( Strings.isNullOrEmpty(appKey.getValue(info)) )
		{
			errors.put("appkey", BAD_APP_KEY_ERROR.getText());
			return;
		}

		if( !connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_OK, false) )
		{
			errors.put("testapp", NOT_TESTED_ERROR.getText());
		}
		model.setErrors(errors);
	}

	@Override
	protected void customLoad(SectionInfo info, ConnectorEditingBean connector)
	{
		appId.setValue(info, connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_ID));
		appKey.setValue(info, connector.getAttribute(BrightspaceConnectorConstants.FIELD_APP_KEY));
	}

	@Override
	protected void customSave(SectionInfo info, ConnectorEditingBean connector)
	{
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_APP_ID, appId.getValue(info));
		connector.setAttribute(BrightspaceConnectorConstants.FIELD_APP_KEY, appKey.getValue(info));
	}

	@Override
	protected Connector createNewConnector()
	{
		return new Connector(BrightspaceConnectorConstants.CONNECTOR_TYPE);
	}

	@Override
	protected String getAjaxDivId()
	{
		return "brightspacesetup";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new BrightspaceConnectorEditorModel();
	}

	@Override
	public Div getUsernameDiv()
	{
		return null;
	}

	public TextField getAppId()
	{
		return appId;
	}

	public TextField getAppKey()
	{
		return appKey;
	}

	public Button getTestAppButton()
	{
		return testAppButton;
	}

	public LMSAuthDialog getAuthDialog()
	{
		return authDialog;
	}

	public class BrightspaceConnectorEditorModel
		extends
			AbstractConnectorEditorSection<BrightspaceConnectorEditorModel>.AbstractConnectorEditorModel
	{
		private boolean appOk;
		private String testAppStatus;
		private String testAppStatusClass;

		private String trustedUrl;
		private String ltiConsumersUrl;

		private boolean adminOk;
		private String adminStatus;
		private String adminStatusClass;

		public boolean isAppOk()
		{
			return appOk;
		}

		public void setAppOk(boolean appOk)
		{
			this.appOk = appOk;
		}

		public String getTestAppStatus()
		{
			return testAppStatus;
		}

		public void setTestAppStatus(String testAppStatus)
		{
			this.testAppStatus = testAppStatus;
		}

		public String getTestAppStatusClass()
		{
			return testAppStatusClass;
		}

		public void setTestAppStatusClass(String testAppStatusClass)
		{
			this.testAppStatusClass = testAppStatusClass;
		}

		public String getTrustedUrl()
		{
			return trustedUrl;
		}

		public void setTrustedUrl(String trustedUrl)
		{
			this.trustedUrl = trustedUrl;
		}

		public String getLtiConsumersUrl()
		{
			return ltiConsumersUrl;
		}

		public void setLtiConsumersUrl(String ltiConsumersUrl)
		{
			this.ltiConsumersUrl = ltiConsumersUrl;
		}

		public boolean isAdminOk()
		{
			return adminOk;
		}

		public void setAdminOk(boolean adminOk)
		{
			this.adminOk = adminOk;
		}

		public String getAdminStatus()
		{
			return adminStatus;
		}

		public void setAdminStatus(String adminStatus)
		{
			this.adminStatus = adminStatus;
		}

		public String getAdminStatusClass()
		{
			return adminStatusClass;
		}

		public void setAdminStatusClass(String adminStatusClass)
		{
			this.adminStatusClass = adminStatusClass;
		}
	}
}
