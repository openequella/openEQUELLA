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

package com.tle.web.connectors.editor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.formatter.ExpressionFormatter;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.connectors.service.ConnectorEditingSession;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.DebugSettings;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.recipientselector.ExpressionSelectorDialog;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractConnectorEditorSection<M extends AbstractConnectorEditorSection<M>.AbstractConnectorEditorModel>
	extends
		AbstractEntityEditor<ConnectorEditingBean, Connector, M>
{
	private static final Logger LOGGER = Logger.getLogger(AbstractConnectorEditorSection.class);

	private static final IncludeFile INCLUDE = new IncludeFile(
		ResourcesService.getResourceHelper(AbstractConnectorEditorSection.class).url("scripts/editcommon.js"));
	private static final ExternallyDefinedFunction FUNCTION_MODIFY_USERNAME_CHANGED = new ExternallyDefinedFunction(
		"modifyUsernameChanged", 2, INCLUDE);

	@PlugKey("editor.error.url.mandatory")
	private static String KEY_ERROR_URL;
	@PlugKey("editor.error.url.musttest")
	private static String KEY_ERROR_TESTURL;

	@PlugKey("editor.expressionselector.title")
	private static Label EXPRESSION_LABEL;
	@PlugKey("editor.label.modifyusername")
	private static String KEY_MODIFY_USERNAME;

	@PlugKey("editor.label.export.summary")
	private static Label LABEL_EXPORT_SUMMARY;

	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService connectorRepositoryService;
	@Inject
	private UserService userService;
	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Component(name = "u", stateful = false)
	private TextField serverUrl;
	@PlugKey("editor.button.testurl")
	@Component
	private Button testUrlButton;
	@Component(name = "mu", stateful = false)
	private Checkbox modifyUsername;
	@Component(name = "us", stateful = false)
	private TextField usernameScript;
	@Component
	private Div modifyUsernameDiv;
	@Inject
	private ExpressionSelectorDialog exportableSelector;
	@Inject
	private ExpressionSelectorDialog viewableSelector;
	@Component
	private Div exportDiv;
	@Component
	private Div viewDiv;
	@Component
	private Div usernameDiv;
	@Component(name = "es", stateful = false)
	private Checkbox exportSummary;

	protected abstract void customValidate(SectionInfo info, ConnectorEditingBean connector,
		Map<String, Object> errors);

	protected abstract void customLoad(SectionInfo info, ConnectorEditingBean connector);

	protected abstract void customSave(SectionInfo info, ConnectorEditingBean connector);

	protected abstract Connector createNewConnector();

	@Override
	public final SectionResult renderHtml(RenderEventContext context)
	{
		final M model = getModel(context);
		final ConnectorEditingSession session = connectorService.loadSession(model.getSessionId());
		final ConnectorEditingBean connector = session.getBean();

		boolean exportable = connectorRepositoryService.supportsExport(connector.getLmsType());
		boolean viewable = connectorRepositoryService.supportsView(connector.getLmsType());

		String testedUrl = connector.getAttribute(ConnectorConstants.FIELD_TESTED_URL);
		if( !Check.isEmpty(testedUrl) )
		{
			if( testedUrl.endsWith("/") )
			{
				testedUrl = Utils.safeSubstring(testedUrl, 0, -1);
			}
			model.setTestedUrl(testedUrl);
		}
		model.setEntityUuid(session.getBean().getUuid());

		model.setErrors(session.getValidationErrors());
		model.setCustomEditor(renderFields(context, session));
		modifyUsername.setLabel(context, new KeyLabel(KEY_MODIFY_USERNAME, getConnectorLmsName(context, connector)));

		if( !exportable )
		{
			exportableSelector.setExpression(context, null);
		}
		else
		{
			String exportableExpression = exportableSelector.getExpression(context);
			if( Check.isEmpty(exportableExpression) )
			{
				exportableExpression = Recipient.OWNER.getPrefix();
			}
			model.setExportableExpressionPretty(
				new ExpressionFormatter(userService).convertToInfix(exportableExpression));
			exportableSelector.setExpression(context, exportableExpression);
		}

		if( !viewable )
		{
			viewableSelector.setExpression(context, null);
		}
		else
		{
			String viewableExpression = viewableSelector.getExpression(context);
			if( Check.isEmpty(viewableExpression) )
			{
				viewableExpression = Recipient.OWNER.getPrefix();
			}
			model.setViewableExpressionPretty(new ExpressionFormatter(userService).convertToInfix(viewableExpression));
			viewableSelector.setExpression(context, viewableExpression);
		}
		if( !DebugSettings.isAutoTestMode() )
		{
			context.getBody().addEventStatements(JSHandler.EVENT_BEFOREUNLOAD,
				new ReturnStatement(getWarningNavigateAway()));
		}

		return view.createResult("connector-editcommon.ftl", context);
	}

	private Label getConnectorLmsName(SectionInfo info, ConnectorEditingBean connector)
	{
		final Label lmsNameLabel = new KeyLabel(
			connectorService.mapAllAvailableTypes().get(connector.getLmsType()).getNameKey());
		getModel(info).setConnectorLmsName(lmsNameLabel);
		return lmsNameLabel;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		testUrlButton.setClickHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("testUrl"), getAjaxDivId()));

		exportableSelector.setStateful(false);
		exportableSelector.setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("expression"), "exportable"));
		componentFactory.registerComponent(id, "exportableSelector", tree, exportableSelector);
		exportableSelector.setTitle(EXPRESSION_LABEL);

		viewableSelector.setStateful(false);
		viewableSelector
			.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("expression"), "viewable"));
		componentFactory.registerComponent(id, "viewableSelector", tree, viewableSelector);
		viewableSelector.setTitle(EXPRESSION_LABEL);

		modifyUsername.addReadyStatements(
			Js.call_s(FUNCTION_MODIFY_USERNAME_CHANGED, Jq.$(modifyUsername), Jq.$(modifyUsernameDiv), false));
		modifyUsername.addEventStatements("change",
			Js.call_s(FUNCTION_MODIFY_USERNAME_CHANGED, Jq.$(modifyUsername), Jq.$(modifyUsernameDiv), true));

		exportSummary.setLabel(LABEL_EXPORT_SUMMARY);
	}

	@EventHandlerMethod
	public void expression(SectionInfo info, String selectorId, String expression)
	{
		if( selectorId != null )
		{
			final ConnectorEditingSession session = connectorService.loadSession(getModel(info).getSessionId());
			final ConnectorEditingBean bean = session.getBean();

			if( selectorId.equals(exportableSelector.getSectionId()) )
			{
				bean.setContentExportableExpression(expression);
			}
			else if( selectorId.equals(viewableSelector.getSectionId()) )
			{
				bean.setContentViewableExpression(expression);
			}
		}
	}

	@EventHandlerMethod
	public void testUrl(SectionInfo info)
	{
		final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
		final ConnectorEditingBean connector = session.getBean();

		final String url = connector.getServerUrl();
		try( Response resp = httpService.getWebContent(new Request(url), configService.getProxyDetails()) )
		{
			final M model = getModel(info);
			if( !resp.isOk() )
			{
				model.setTestUrlStatus("fail");
				session.getValidationErrors().put("urltest",
					CurrentLocale.get("com.tle.web.connectors.editor.error.url.unreachable",
						getConnectorLmsName(info, connector).getText()));
				onTestUrlFail(info, connector);
			}
			else
			{
				connector.setAttribute(ConnectorConstants.FIELD_TESTED_URL, url);
				model.setTestUrlStatus("ok");
			}
		}
		catch( Exception t )
		{
			LOGGER.error(t);
		}
	}

	protected void onTestUrlFail(SectionInfo info, ConnectorEditingBean connector)
	{
		connector.setAttribute(ConnectorConstants.FIELD_TESTED_URL, null);
	}

	@Override
	protected void validate(SectionInfo info, EntityEditingSession<ConnectorEditingBean, Connector> session)
	{
		final ConnectorEditingBean connector = session.getBean();
		final Map<String, Object> errors = session.getValidationErrors();

		final String url = connector.getServerUrl();
		if( Check.isEmpty(url) )
		{
			errors.put("url", new KeyLabel(KEY_ERROR_URL, getConnectorLmsName(info, connector)).getText());
			onTestUrlFail(info, connector);
		}
		else
		{
			final String testedUrl = connector.getAttribute(ConnectorConstants.FIELD_TESTED_URL);
			if( Check.isEmpty(testedUrl) || !testedUrl.equals(url) )
			{
				errors.put("url", new KeyLabel(KEY_ERROR_TESTURL, getConnectorLmsName(info, connector)).getText());
				onTestUrlFail(info, connector);
			}
		}

		customValidate(info, connector, errors);
	}

	protected abstract String getAjaxDivId();

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<ConnectorEditingBean, Connector> session,
		boolean validate)
	{
		final ConnectorEditingBean connector = session.getBean();

		connector.setUseLoggedInUsername(!modifyUsername.isChecked(info));
		connector.setUsernameScript(usernameScript.getValue(info));
		final String url = serverUrl.getValue(info);
		connector.setServerUrl(url);
		final String testedUrl = connector.getAttribute(ConnectorConstants.FIELD_TESTED_URL);
		if( testedUrl != null && !testedUrl.equals(url) )
		{
			connector.setAttribute(ConnectorConstants.FIELD_TESTED_URL, null);
		}

		final String exportExpression = exportableSelector.getExpression(info);
		final String viewExpression = viewableSelector.getExpression(info);
		if( exportExpression != null || viewExpression != null )
		{
			final TargetList list = new TargetList();
			list.setEntries(new ArrayList<TargetListEntry>());
			if( exportExpression != null )
			{
				list.getEntries().add(
					new TargetListEntry(true, false, ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR, exportExpression));
			}
			if( viewExpression != null )
			{
				list.getEntries().add(new TargetListEntry(true, false,
					ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR, viewExpression));
			}
			session.getPack().setTargetList(list);
		}

		connector.setAttribute(ConnectorConstants.SHOW_SUMMARY_KEY, exportSummary.isChecked(info));

		customSave(info, connector);

		connectorService.saveSession(session);
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<ConnectorEditingBean, Connector> session)
	{
		final ConnectorEditingBean connector = session.getBean();
		final M model = getModel(info);

		final String url = connector.getServerUrl();
		final String testedUrl = connector.getAttribute(ConnectorConstants.FIELD_TESTED_URL);
		serverUrl.setValue(info, url);
		if( Objects.equals(url, testedUrl) )
		{
			model.setTestedUrl(testedUrl);
			// it must have checked out ok in the past...
			model.setTestUrlStatus("ok");
		}

		exportSummary.setChecked(info, connector.getAttribute(ConnectorConstants.SHOW_SUMMARY_KEY, true));

		final String exportableExpression = connector.getContentExportableExpression();
		if( exportableExpression != null )
		{
			exportableSelector.setExpression(info, exportableExpression);
		}
		final String viewableExpression = connector.getContentViewableExpression();
		if( viewableExpression != null )
		{
			viewableSelector.setExpression(info, viewableExpression);
		}

		modifyUsername.setChecked(info, !connector.isUseLoggedInUsername());

		final String username = (connector.getId() == 0 ? "return username;" : connector.getUsernameScript());
		usernameScript.setValue(info, username);

		customLoad(info, connector);
	}

	@Override
	public SectionRenderable renderEditor(RenderContext info)
	{
		return renderSection(info, this);
	}

	@Override
	protected Connector createNewEntity(SectionInfo info)
	{
		return createNewConnector();
	}

	@Override
	protected AbstractEntityService<ConnectorEditingBean, Connector> getEntityService()
	{
		return connectorService;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AbstractConnectorEditorModel();
	}

	public TextField getServerUrl()
	{
		return serverUrl;
	}

	public Button getTestUrlButton()
	{
		return testUrlButton;
	}

	public ExpressionSelectorDialog getExportableSelector()
	{
		return exportableSelector;
	}

	public ExpressionSelectorDialog getViewableSelector()
	{
		return viewableSelector;
	}

	public Checkbox getModifyUsername()
	{
		return modifyUsername;
	}

	public Div getModifyUsernameDiv()
	{
		return modifyUsernameDiv;
	}

	public TextField getUsernameScript()
	{
		return usernameScript;
	}

	public Div getExportDiv()
	{
		return exportDiv;
	}

	public Div getViewDiv()
	{
		return viewDiv;
	}

	public Div getUsernameDiv()
	{
		return usernameDiv;
	}

	public Checkbox getExportSummary()
	{
		return exportSummary;
	}

	public class AbstractConnectorEditorModel
		extends
			AbstractEntityEditor<ConnectorEditingBean, Connector, M>.AbstractEntityEditorModel
	{
		private String testUrlStatus;
		private String testedUrl;
		private String exportableExpressionPretty;
		private String viewableExpressionPretty;
		private Label connectorLmsName;

		public String getTestUrlStatus()
		{
			return testUrlStatus;
		}

		public void setTestUrlStatus(String testUrlStatus)
		{
			this.testUrlStatus = testUrlStatus;
		}

		public String getTestedUrl()
		{
			return testedUrl;
		}

		public void setTestedUrl(String testedUrl)
		{
			this.testedUrl = testedUrl;
		}

		public String getExportableExpressionPretty()
		{
			return exportableExpressionPretty;
		}

		public void setExportableExpressionPretty(String exportableExpressionPretty)
		{
			this.exportableExpressionPretty = exportableExpressionPretty;
		}

		public String getViewableExpressionPretty()
		{
			return viewableExpressionPretty;
		}

		public void setViewableExpressionPretty(String viewableExpressionPretty)
		{
			this.viewableExpressionPretty = viewableExpressionPretty;
		}

		public Label getConnectorLmsName()
		{
			return connectorLmsName;
		}

		public void setConnectorLmsName(Label connectorLmsName)
		{
			this.connectorLmsName = connectorLmsName;
		}
	}
}
