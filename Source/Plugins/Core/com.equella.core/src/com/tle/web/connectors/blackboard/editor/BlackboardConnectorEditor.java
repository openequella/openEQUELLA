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

package com.tle.web.connectors.blackboard.editor;

import static com.tle.core.connectors.blackboard.BlackboardConnectorConstants.CONNECTOR_TYPE;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.blackboard.BlackboardConnectorConstants;
import com.tle.core.connectors.blackboard.service.BlackboardConnectorService;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.guice.Bind;
import com.tle.web.connectors.editor.AbstractConnectorEditorSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class BlackboardConnectorEditor
	extends
		AbstractConnectorEditorSection<BlackboardConnectorEditor.BlackboardConnectorEditorModel>
{
	@PlugKey("bb.editor.error.testwebservice.mandatory")
	private static Label LABEL_TEST_WEBSERVICE_MANDATORY;
	@PlugKey("editor.error.testwebservice.enteruser")
	private static Label LABEL_TEST_WEBSERVICE_ENTERUSER;

	@Inject
	private BlackboardConnectorService blackboardService;

	@Component(name = "pts", stateful = false)
	private TextField proxyToolSecret;
	@Component(name = "pp", stateful = false)
	private TextField proxyToolPass; // this is the global BB password to

	@PlugKey("editor.button.register")
	@Component
	private Button registerButton;
	@Component(name = "twsu", stateful = false)
	private TextField testWebServiceUsername;
	@PlugKey("editor.button.testwebservice")
	@Component
	private Button testWebServiceButton;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<ConnectorEditingBean, Connector> session)
	{
		return view.createResult("blackboardconnector.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		registerButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("registerTool"), "registerdiv"));

		testWebServiceButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("testWebService"), "testdiv"));
	}

	@Override
	protected String getAjaxDivId()
	{
		return "blackboardsetup";
	}

	@Override
	protected void onTestUrlFail(SectionInfo info, ConnectorEditingBean connector)
	{
		super.onTestUrlFail(info, connector);
		getModel(info).setTestWebServiceStatus(null);
		connector.setAttribute(BlackboardConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
	}

	@EventHandlerMethod
	public void registerTool(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
		final ConnectorEditingBean connector = session.getBean();

		final String result = blackboardService.registerProxyTool(connector.getServerUrl(),
			proxyToolPass.getValue(info));
		final BlackboardConnectorEditorModel model = getModel(info);
		if( result == null )
		{
			model.setRegisterToolStatus("ok");
			model.setRegisterToolOk(true);
		}
		else if( result.equals(BlackboardConnectorService.REGISTER_PROXY_TOOL_RESULT_ALREADY_REGISTERED) )
		{
			model.setRegisterToolStatus("okalreadyregistered");
			model.setRegisterToolOk(true);
		}
		else
		{
			model.setRegisterToolStatus("fail");
			model.setRegisterToolOk(false);
			session.getValidationErrors().put("registertool", result);
		}
	}

	@EventHandlerMethod
	public void testWebService(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);

		final String wsUsername = testWebServiceUsername.getValue(info);
		if( Check.isEmpty(wsUsername) )
		{
			session.getValidationErrors().put("systemusername", LABEL_TEST_WEBSERVICE_ENTERUSER.getText());
			return;
		}

		final ConnectorEditingBean connector = session.getBean();

		// change the secret if necessary
		final String secret = proxyToolSecret.getValue(info);
		if( !Check.isEmpty(secret) )
		{
			blackboardService.setSecret(connector.getServerUrl(), secret);
		}

		final String result = blackboardService.testConnection(connector.getServerUrl(), wsUsername);
		if( result == null )
		{
			connector.setAttribute(BlackboardConnectorConstants.FIELD_TESTED_WEBSERVICE, true);
			getModel(info).setTestWebServiceStatus("ok");
		}
		else
		{
			connector.setAttribute(BlackboardConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
			getModel(info).setTestWebServiceStatus("fail");
			session.getValidationErrors().put("blackboardwebservice", result);
		}
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}

	@Override
	protected Connector createNewConnector()
	{
		return new Connector(CONNECTOR_TYPE);
	}

	@Override
	protected void customValidate(SectionInfo info, ConnectorEditingBean connector, Map<String, Object> errors)
	{
		if( !connector.getAttribute(BlackboardConnectorConstants.FIELD_TESTED_WEBSERVICE, false) )
		{
			errors.put("blackboardwebservice", LABEL_TEST_WEBSERVICE_MANDATORY.getText());
		}

		if( Check.isEmpty(connector.getAttribute(BlackboardConnectorConstants.SYSTEM_USERNAME)) )
		{
			errors.put("systemusername", LABEL_TEST_WEBSERVICE_ENTERUSER.getText());
		}
	}

	@Override
	protected void customLoad(SectionInfo info, ConnectorEditingBean connector)
	{
		proxyToolSecret.setValue(info, blackboardService.getSecret(connector.getServerUrl()));
		testWebServiceUsername.setValue(info, connector.getAttribute(BlackboardConnectorConstants.SYSTEM_USERNAME));
		final boolean testedWebservice = connector.getAttribute(BlackboardConnectorConstants.FIELD_TESTED_WEBSERVICE,
			false);
		if( testedWebservice )
		{
			final BlackboardConnectorEditorModel model = getModel(info);
			model.setTestWebServiceStatus("ok");
		}
	}

	@Override
	protected void customSave(SectionInfo info, ConnectorEditingBean connector)
	{
		connector.setAttribute(BlackboardConnectorConstants.SYSTEM_USERNAME, testWebServiceUsername.getValue(info));
		// No need to set the secret. Register proxy tool MUST be invoked to
		// save this
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new BlackboardConnectorEditorModel();
	}

	public Button getRegisterButton()
	{
		return registerButton;
	}

	public TextField getProxyToolPass()
	{
		return proxyToolPass;
	}

	public TextField getProxyToolSecret()
	{
		return proxyToolSecret;
	}

	public TextField getTestWebServiceUsername()
	{
		return testWebServiceUsername;
	}

	public Button getTestWebServiceButton()
	{
		return testWebServiceButton;
	}

	public class BlackboardConnectorEditorModel
		extends
			AbstractConnectorEditorSection<BlackboardConnectorEditorModel>.AbstractConnectorEditorModel
	{
		private String registerToolStatus;
		private boolean registerToolOk;
		private String testWebServiceStatus;
		private String proxyToolRegistration;

		public String getRegisterToolStatus()
		{
			return registerToolStatus;
		}

		public void setRegisterToolStatus(String registerToolStatus)
		{
			this.registerToolStatus = registerToolStatus;
		}

		public boolean isRegisterToolOk()
		{
			return registerToolOk;
		}

		public void setRegisterToolOk(boolean registerToolOk)
		{
			this.registerToolOk = registerToolOk;
		}

		public String getTestWebServiceStatus()
		{
			return testWebServiceStatus;
		}

		public void setTestWebServiceStatus(String testWebServiceStatus)
		{
			this.testWebServiceStatus = testWebServiceStatus;
		}

		public void setProxyToolRegistration(String proxyToolRegistration)
		{
			this.proxyToolRegistration = proxyToolRegistration;
		}

		public String getProxyToolRegistration()
		{
			return proxyToolRegistration;
		}
	}
}
