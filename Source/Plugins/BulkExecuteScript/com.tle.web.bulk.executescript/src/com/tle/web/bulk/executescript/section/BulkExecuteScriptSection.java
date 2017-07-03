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

package com.tle.web.bulk.executescript.section;

import javax.inject.Inject;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;

import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.web.bulk.executescript.model.BulkExecuteScriptModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.BootstrapDropDownRenderer;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.CodeMirror;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@Bind
@SuppressWarnings("nls")
public class BulkExecuteScriptSection extends AbstractPrototypeSection<BulkExecuteScriptModel> implements HtmlRenderer
{
	@PlugKey("script.validate")
	private static Label LABEL_VALIDATE;
	@PlugKey("script.errormessage")
	private static String ERROR_MESSAGE;
	@PlugKey("scriptlist.action")
	private static Label SCRIPT_LIST_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@Inject
	private UserScriptsService userScriptService;
	@Inject
	private BundleCache bundleCache;

	@Component(name = "lds")
	private SingleSelectionList<UserScript> scripts;
	@Component(name = "vs")
	private Button validateScriptButton;

	@Component(name = "scpt")
	private CodeMirror scriptEditor;

	@Override
	public SectionResult renderHtml(RenderEventContext info)
	{
		scripts.setDisplayed(info, userScriptService.executableScriptsAvailable());
		validateScriptButton.setLabel(info, LABEL_VALIDATE);
		return viewFactory.createResult("bulkexecute.ftl", info);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		scriptEditor.setEditorType(EditorType.JAVASCRIPT_EDITOR);
		scriptEditor.setAllowFullScreen(false);
		scriptEditor.setShowHelp(true);

		validateScriptButton.setStyleClass("validate");
		validateScriptButton.setClickHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("validate"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING),
			"errordisplay", "bss_bulkDialogfooter")));

		scripts.setLabel(SCRIPT_LIST_LABEL);
		scripts.setListModel(new ScriptListModel());
		scripts.setDefaultRenderer(BootstrapDropDownRenderer.RENDER_CONSTANT);
		scripts.addChangeEventHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("loadUserScript"), "editor")));

	}

	@EventHandlerMethod
	public void loadUserScript(SectionInfo info)
	{
		String script = userScriptService.getByUuid(scripts.getSelectedValueAsString(info)).getScript();
		scriptEditor.setValue(info, script);
	}

	@EventHandlerMethod
	public void validate(SectionInfo info)
	{
		String script = getScript(info);
		BulkExecuteScriptModel model = getModel(info);
		model.setValidationRan(true);
		CompilerEnvirons ce = new CompilerEnvirons();
		ce.initFromContext(ContextFactory.getGlobal().enterContext());
		ErrorReporter er = new ErrorReporter()
		{
			private Label errorMessage;

			@Override
			public void warning(String message, String sourceName, int line, String lineSource, int lineOffset)
			{
				// pfft warnings?
			}

			@Override
			public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
				int lineOffset)
			{
				return new EvaluatorException(errorMessage.getText());
			}

			@Override
			public void error(String message, String sourceName, int line, String lineSource, int lineOffset)
			{
				// only log one error found. Helps with output
				errorMessage = new KeyLabel(ERROR_MESSAGE, message, line + 1, lineOffset, lineSource);
			}

		};
		Parser p = new Parser(ce, er);
		try
		{
			p.parse(script, "bulkExecute", 0);
		}
		catch( EvaluatorException e )
		{
			model.setValidationErrors(true);
			model.setErrorMessage(e.getMessage());

			return;
		}
		model.setValidationErrors(false);

	}

	public class ScriptListModel extends DynamicHtmlListModel<UserScript>
	{
		@Override
		protected Option<UserScript> convertToOption(SectionInfo info, UserScript script)
		{
			return new NameValueOption<UserScript>(
				new BundleNameValue(script.getName(), script.getUuid(), bundleCache), script);
		}

		@Override
		protected Iterable<UserScript> populateModel(SectionInfo info)
		{
			return userScriptService.enumerateForType(ScriptTypes.EXECUTABLE);
		}

	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new BulkExecuteScriptModel();
	}

	@Override
	public Class<BulkExecuteScriptModel> getModelClass()
	{
		return BulkExecuteScriptModel.class;
	}

	public Button getValidateScriptButton()
	{
		return validateScriptButton;
	}

	public String getScript(SectionInfo info)
	{
		return scriptEditor.getValue(info);
	}

	public SingleSelectionList<UserScript> getScripts()
	{
		return scripts;
	}

	public CodeMirror getScriptEditor()
	{
		return scriptEditor;
	}

}
