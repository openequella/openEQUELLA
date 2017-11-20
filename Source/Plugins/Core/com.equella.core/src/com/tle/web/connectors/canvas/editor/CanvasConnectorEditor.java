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

package com.tle.web.connectors.canvas.editor;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.connectors.canvas.CanvasConnectorConstants;
import com.tle.core.connectors.canvas.service.CanvasConnectorService;
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
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class CanvasConnectorEditor
	extends
		AbstractConnectorEditorSection<CanvasConnectorEditor.CanvasConnectorEditorModel>
{
	@PlugKey("editor.error.badtoken")
	private static Label BAD_TOKEN_ERROR;

	@PlugKey("canvas.testtoken.response.")
	private static String PFX_TOKEN_RESPONSE;

	@Component(stateful = false)
	private TextField manualTokenEntry;
	@Component
	@PlugKey("editor.button.test")
	private Button testTokenButton;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Inject
	private CanvasConnectorService connectorService;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<ConnectorEditingBean, Connector> session)
	{

		return view.createResult("canvasconnector.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		testTokenButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("testAccessToken"), getAjaxDivId()));
	}

	@EventHandlerMethod
	public void testAccessToken(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
		final ConnectorEditingBean connector = session.getBean();

		CanvasConnectorEditorModel model = getModel(info);

		String testResponse = connectorService.testAccessToken(connector.getServerUrl(),
			manualTokenEntry.getValue(info));

		model.setStatusClass(testResponse.equals("unauthorized") ? "fail" : testResponse);
		model.setTestAccessTokenStatus(CurrentLocale.get(PFX_TOKEN_RESPONSE + testResponse));
		if( testResponse.equals("ok") )
		{
			connector.setAttribute(CanvasConnectorConstants.FIELD_TOKEN_OK, true);
		}
	}

	@Override
	protected void customValidate(SectionInfo info, ConnectorEditingBean connector, Map<String, Object> errors)
	{
		CanvasConnectorEditorModel model = getModel(info);
		if( Check.isEmpty(manualTokenEntry.getValue(info)) )
		{
			errors.put("tokenentry", BAD_TOKEN_ERROR.getText());
			return;
		}

		if( !connector.getAttribute(CanvasConnectorConstants.FIELD_TOKEN_OK, false) )
		{
			errors.put("tokentest", BAD_TOKEN_ERROR.getText());
		}

	}

	@Override
	protected void customLoad(SectionInfo info, ConnectorEditingBean connector)
	{
		manualTokenEntry.setValue(info, connector.getAttribute(CanvasConnectorConstants.FIELD_ACCESS_TOKEN));
	}

	@Override
	protected void customSave(SectionInfo info, ConnectorEditingBean connector)
	{
		connector.setAttribute(CanvasConnectorConstants.FIELD_ACCESS_TOKEN, manualTokenEntry.getValue(info));

	}

	@Override
	protected Connector createNewConnector()
	{
		return new Connector(CanvasConnectorConstants.CONNECTOR_TYPE);
	}

	@Override
	protected String getAjaxDivId()
	{
		return "canvassetup";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CanvasConnectorEditorModel();
	}

	@Override
	public Div getUsernameDiv()
	{
		return null;
	}

	public TextField getManualTokenEntry()
	{
		return manualTokenEntry;
	}

	public Button getTestTokenButton()
	{
		return testTokenButton;
	}

	public class CanvasConnectorEditorModel
		extends
			AbstractConnectorEditorSection<CanvasConnectorEditorModel>.AbstractConnectorEditorModel
	{
		private String testAccessTokenStatus;
		private String statusClass;

		public String getStatusClass()
		{
			return statusClass;
		}

		public void setStatusClass(String statusClass)
		{
			this.statusClass = statusClass;
		}

		public String getTestAccessTokenStatus()
		{
			return testAccessTokenStatus;
		}

		public void setTestAccessTokenStatus(String testAccessTokenStatus)
		{
			this.testAccessTokenStatus = testAccessTokenStatus;
		}

	}
}
