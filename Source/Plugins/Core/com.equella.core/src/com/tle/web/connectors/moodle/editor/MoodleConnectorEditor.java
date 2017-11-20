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

package com.tle.web.connectors.moodle.editor;

import static com.tle.core.connectors.moodle.MoodleConnectorConstants.CONNECTOR_TYPE;
import static com.tle.core.connectors.moodle.MoodleConnectorConstants.FIELD_WEBSERVICE_TOKEN;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.moodle.MoodleConnectorConstants;
import com.tle.core.connectors.moodle.service.MoodleConnectorService;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
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
public class MoodleConnectorEditor
	extends
		AbstractConnectorEditorSection<MoodleConnectorEditor.MoodleConnectorEditorModel>
{
	@PlugKey("editor.error.webservicetoken.mandatory")
	private static Label LABEL_ERROR_WEBSERVICETOKEN;
	@PlugKey("editor.error.testwebservice.mandatory")
	private static Label LABEL_TEST_WEBSERVICE_MANDATORY;

	@Inject
	private MoodleConnectorService moodleService;

	@Component(name = "wt", stateful = false)
	private TextField webServiceToken;

	@PlugKey("editor.button.testservice")
	@Component
	private Button testServiceButton;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<ConnectorEditingBean, Connector> session)
	{
		return view.createResult("moodleconnector.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		testServiceButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("testMoodle"), "testdiv"));
	}

	@Override
	protected String getAjaxDivId()
	{
		return "moodlesetup";
	}

	@Override
	protected void onTestUrlFail(SectionInfo info, ConnectorEditingBean connector)
	{
		super.onTestUrlFail(info, connector);
		getModel(info).setTestMoodleStatus(null);
		connector.setAttribute(MoodleConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
	}

	@EventHandlerMethod
	public void testMoodle(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
		final ConnectorEditingBean connector = session.getBean();

		final String result = moodleService.testConnection(connector.getServerUrl(),
			connector.getAttribute(FIELD_WEBSERVICE_TOKEN), CurrentUser.getUsername());
		if( result == null )
		{
			connector.setAttribute(MoodleConnectorConstants.FIELD_TESTED_WEBSERVICE, true);
			getModel(info).setTestMoodleStatus("ok");
		}
		else
		{
			connector.setAttribute(MoodleConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
			getModel(info).setTestMoodleStatus("fail");
			session.getValidationErrors().put("moodlewebservice", result);
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
		if( Check.isEmpty(connector.getAttribute(FIELD_WEBSERVICE_TOKEN)) )
		{
			errors.put("token", LABEL_ERROR_WEBSERVICETOKEN.getText());
		}

		if( !connector.getAttribute(MoodleConnectorConstants.FIELD_TESTED_WEBSERVICE, false) )
		{
			errors.put("moodlewebservice", LABEL_TEST_WEBSERVICE_MANDATORY.getText());
		}
	}

	@Override
	protected void customLoad(SectionInfo info, ConnectorEditingBean connector)
	{
		webServiceToken.setValue(info, connector.getAttribute(FIELD_WEBSERVICE_TOKEN));

		final boolean testedWebservice = connector
			.getAttribute(MoodleConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
		if( testedWebservice )
		{
			final MoodleConnectorEditorModel model = getModel(info);
			model.setTestMoodleStatus("ok");
		}
	}

	@Override
	protected void customSave(SectionInfo info, ConnectorEditingBean connector)
	{
		connector.setAttribute(FIELD_WEBSERVICE_TOKEN, webServiceToken.getValue(info));
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MoodleConnectorEditorModel();
	}

	// @Override
	// protected void customClear(SectionInfo info)
	// {
	//
	// webServiceToken.setValue(info, null);
	// }

	public TextField getWebServiceToken()
	{
		return webServiceToken;
	}

	public Button getTestServiceButton()
	{
		return testServiceButton;
	}

	public class MoodleConnectorEditorModel
		extends
			AbstractConnectorEditorSection<MoodleConnectorEditorModel>.AbstractConnectorEditorModel
	{
		private String testMoodleStatus;

		public String getTestMoodleStatus()
		{
			return testMoodleStatus;
		}

		public void setTestMoodleStatus(String testMoodleStatus)
		{
			this.testMoodleStatus = testMoodleStatus;
		}
	}
}
