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

package com.tle.web.userscripts.section;

import java.util.Map;

import javax.inject.Inject;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;

import com.dytech.edge.common.Constants;
import com.google.common.base.Strings;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.core.userscripts.service.session.UserScriptEditingBean;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.CodeMirror;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.userscripts.section.UserScriptEditorSection.UserScriptEditorModel;

@Bind
@SuppressWarnings("nls")
public class UserScriptEditorSection
	extends
		AbstractEntityEditor<UserScriptEditingBean, UserScript, UserScriptEditorModel>
{
	@PlugKey("editor.scripttypes.")
	private static String KEY_SCRIPT_TYPE_PFX;
	@PlugKey("script.errormessage")
	private static String ERROR_MESSAGE;
	@PlugKey("syntax.ok")
	private static Label SYNTAX_PASS;
	@PlugKey("syntax.empty")
	private static Label SYNTAX_EMPTY;

	@PlugKey("script.errormessage.empty")
	private static Label ERROR_MESSAGE_EMPTYSCRIPT;
	@PlugKey("script.errormessage.mandatory")
	private static Label LABEL_ERROR_MANDATORY;
	@PlugKey("script.errormessage.unique")
	private static Label LABEL_ERROR_UNIQUE;

	@Inject
	private UserScriptsService userScriptService;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	protected EventGenerator events;

	@Component(stateful = false)
	private SingleSelectionList<ScriptTypes> scriptTypeList;
	@Component(stateful = false)
	private TextField moduleNameField;
	@Component
	@PlugKey("editor.syntax")
	private Button checkSyntaxButton;

	@Component(name = "javascript")
	private CodeMirror javascriptEditor;
	@Component(name = "freemarker")
	private CodeMirror freemakerEditor;

	@Override
	protected AbstractEntityService<UserScriptEditingBean, UserScript> getEntityService()
	{
		return userScriptService;
	}

	@Override
	protected UserScript createNewEntity(SectionInfo info)
	{
		return new UserScript();
	}

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<UserScriptEditingBean, UserScript> session)
	{
		UserScriptEditorModel model = getModel(context);

		if( model.isJavascript() )
		{
			moduleNameField.setValue(context, model.getModuleName());
			javascriptEditor.setValue(context, model.getJavaScriptContents());
			model.setErrors(session.getValidationErrors());
		}
		else
		{
			freemakerEditor.setValue(context, model.getFreeMarkerContents());
		}
		return view.createResult("editscript.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		javascriptEditor.setEditorType(EditorType.JAVASCRIPT_EDITOR);
		javascriptEditor.setAllowFullScreen(true);
		javascriptEditor.setShowHelp(true);
		freemakerEditor.setEditorType(EditorType.FREEMARKER_EDITOR);
		freemakerEditor.setAllowFullScreen(true);
		freemakerEditor.setShowHelp(true);

		scriptTypeList.setListModel(new EnumListModel<ScriptTypes>(KEY_SCRIPT_TYPE_PFX, true, ScriptTypes.values()));
		StatementHandler listUpdate = new StatementHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("typeChanged"),
				ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), "script-field"));
		scriptTypeList.setEventHandler(JSHandler.EVENT_CHANGE, listUpdate);
		scriptTypeList.setAlwaysSelect(true);

		checkSyntaxButton.setClickHandler(new StatementHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("checkSyntax"), "syntax-div")));
		checkSyntaxButton.setStyleClass("validate-button");
	}

	@EventHandlerMethod
	public void checkSyntax(SectionInfo info)
	{
		UserScriptEditorModel model = getModel(info);
		SpanRenderer message = javaScriptValidation(javascriptEditor.getValue(info));
		model.setSyntaxMessage(message);
	}

	private SpanRenderer javaScriptValidation(String script)
	{
		SpanRenderer msgRenderer;
		if( Check.isEmpty(script) )
		{
			msgRenderer = new SpanRenderer(SYNTAX_EMPTY);
			msgRenderer.addClass("empty");
			return msgRenderer;
		}

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
			p.parse(script, "userscript", 0); //$NON-NLS-1$
		}
		catch( EvaluatorException e )
		{
			msgRenderer = new SpanRenderer(e.getMessage());
			msgRenderer.addClass("fail");
			return msgRenderer;
		}
		msgRenderer = new SpanRenderer(SYNTAX_PASS);
		msgRenderer.addClass("ok");

		return msgRenderer;
	}

	@EventHandlerMethod
	public void typeChanged(SectionInfo info)
	{
		boolean js = scriptTypeList.getSelectedValue(info).equals(ScriptTypes.EXECUTABLE);
		getModel(info).setJavascript(js);

	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<UserScriptEditingBean, UserScript> session)
	{
		final UserScriptEditingBean bean = session.getBean();
		UserScriptEditorModel model = getModel(info);
		if( bean.getName() != null )
		{
			scriptTypeList.setSelectedValue(info, bean.getSelection());
			model.setJavascript(bean.getSelection().equals(ScriptTypes.EXECUTABLE));
			if( model.isJavascript() )
			{
				model.setModuleName(bean.getModuleName());
				model.setJavaScriptContents(bean.getScript());
				model.setFreeMarkerContents(Constants.BLANK);
			}
			else
			{
				model.setJavaScriptContents(Constants.BLANK);
				model.setFreeMarkerContents(bean.getScript());
			}
		}
		else
		{
			// new script -> default to freemarker
			model.setJavascript(false);
			model.setFreeMarkerContents(Constants.BLANK);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new UserScriptEditorModel();
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	protected void validate(SectionInfo info, EntityEditingSession<UserScriptEditingBean, UserScript> session)
	{
		boolean isJavaScript = getModel(info).isJavascript();
		Map<String, Object> validationErrors = session.getValidationErrors();
		validationErrors.clear();

		final LanguageBundleBean bundle = getTitle().getLanguageBundle(info);
		if( LangUtils.isEmpty(bundle) )
		{
			validationErrors.put("title", getTitleMandatoryErrorLabel().getText());
		}

		if( isJavaScript )
		{
			String moduleName = moduleNameField.getValue(info);
			if( !Strings.isNullOrEmpty(moduleName) )
			{
				long id = session.getBean().getId();
				boolean isModuleNameExist = userScriptService.isModuleNameExist(moduleName, id);
				if( isModuleNameExist )
				{
					validationErrors.put("error", LABEL_ERROR_UNIQUE);
				}
			}
			else
			{
				validationErrors.put("error", LABEL_ERROR_MANDATORY);
			}

			if( Strings.isNullOrEmpty(javascriptEditor.getValue(info)) )
			{
				validationErrors.put("errors.noscript", ERROR_MESSAGE_EMPTYSCRIPT);
			}
		}
		else
		{
			if( Strings.isNullOrEmpty(freemakerEditor.getValue(info)) )
			{
				validationErrors.put("errors.noscript", ERROR_MESSAGE_EMPTYSCRIPT);
			}
		}
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<UserScriptEditingBean, UserScript> session,
		boolean validate)
	{
		final UserScriptEditingBean bean = session.getBean();
		UserScriptEditorModel model = getModel(info);
		boolean isJavaScript = model.isJavascript();

		if( isJavaScript )
		{
			bean.setSelection(scriptTypeList.getSelectedValue(info));
			bean.setScript(javascriptEditor.getValue(info));
			bean.setModuleName(moduleNameField.getValue(info));
		}
		else
		{
			bean.setSelection(scriptTypeList.getSelectedValue(info));
			bean.setScript(freemakerEditor.getValue(info));
		}
	}

	public class UserScriptEditorModel
		extends
			AbstractEntityEditor<UserScriptEditingBean, UserScript, UserScriptEditorModel>.AbstractEntityEditorModel
	{
		@Bookmarked
		private boolean javascript;
		private SpanRenderer syntaxMessage;
		private String moduleName;
		private String javaScriptContents = Constants.BLANK;
		private String freeMarkerContents = Constants.BLANK;

		public String getJavaScriptContents()
		{
			return javaScriptContents;
		}

		public void setJavaScriptContents(String javaScriptContents)
		{
			this.javaScriptContents = javaScriptContents;
		}

		public String getFreeMarkerContents()
		{
			return freeMarkerContents;
		}

		public void setFreeMarkerContents(String freeMarkerContents)
		{
			this.freeMarkerContents = freeMarkerContents;
		}

		public SpanRenderer getSyntaxMessage()
		{
			return syntaxMessage;
		}

		public void setSyntaxMessage(SpanRenderer syntaxMessage)
		{
			this.syntaxMessage = syntaxMessage;
		}

		public boolean isJavascript()
		{
			return javascript;
		}

		public void setJavascript(boolean javascript)
		{
			this.javascript = javascript;
		}

		public String getModuleName()
		{
			return moduleName;
		}

		public void setModuleName(String moduleName)
		{
			this.moduleName = moduleName;
		}

	}

	public SingleSelectionList<ScriptTypes> getScriptTypeList()
	{
		return scriptTypeList;
	}

	public TextField getModuleNameField()
	{
		return moduleNameField;
	}

	public Button getCheckSyntaxButton()
	{
		return checkSyntaxButton;
	}

	public CodeMirror getJavascriptEditor()
	{
		return javascriptEditor;
	}

	public CodeMirror getFreemakerEditor()
	{
		return freemakerEditor;
	}
}