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

package com.tle.web.sections.standard.js.modules;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.model.HtmlValueState;

@SuppressWarnings("nls")
public final class CodeMirrorLibrary
{
	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(TooltipModule.class);

	public enum EditorType
	{
		JAVASCRIPT_EDITOR, FREEMARKER_EDITOR, CSS_EDITOR
	}

	private static final PreRenderable CSS = CssInclude.include(RESOURCES.url("css/codemirror.css")).make();
	private static final PreRenderable CSS_CODEMIRROR = CssInclude
		.include(RESOURCES.url("js/codemirror/lib/codemirror.css")).prerender(CSS).make();
	private static final PreRenderable CSS_ECLIPSE = CssInclude
		.include(RESOURCES.url("js/codemirror/theme/eclipse.css")).prerender(CSS).make();

	private static final PreRenderable PRENDER = new IncludeFile(RESOURCES.url("js/codemirror/lib/codemirror.js"),
		CSS_CODEMIRROR, CSS_ECLIPSE);

	private static final PreRenderable JS_JS = new IncludeFile(
		RESOURCES.url("js/codemirror/mode/javascript/javascript.js"));
	private static final PreRenderable JS_CSS = new IncludeFile(RESOURCES.url("js/codemirror/mode/css/css.js"));
	private static final PreRenderable JS_XML = new IncludeFile(RESOURCES.url("js/codemirror/mode/xml/xml.js"));

	// addon - auto close brackets
	private static final PreRenderable ADDON_ATUOBRACKET = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/edit/closebrackets.js"));

	// addon - fullscreen
	private static final PreRenderable FULLSCREEN_CSS = CssInclude
		.include(RESOURCES.url("js/codemirror/addon/display/fullscreen.css")).prerender(CSS).make();
	private static final PreRenderable ADDON_FULLSCREEN = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/display/fullscreen.js"), FULLSCREEN_CSS);

	// addon - autocomplete
	private static final PreRenderable HINT_CSS = CssInclude
		.include(RESOURCES.url("js/codemirror/addon/hint/show-hint.css")).prerender(CSS).make();
	private static final PreRenderable ADDON_SHOWHINT = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/hint/show-hint.js"));
	private static final PreRenderable ADDON_JAVASCRIPTHINT = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/hint/javascript-hint.js"), HINT_CSS, ADDON_SHOWHINT);
	private static final PreRenderable ADDON_CSSHINT = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/hint/css-hint.js"), HINT_CSS, ADDON_SHOWHINT);

	private static final PreRenderable ADDON_XMLHINT = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/hint/xml-hint.js"));
	private static final PreRenderable ADDON_HTMLHINT = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/hint/html-hint.js"), HINT_CSS, ADDON_SHOWHINT, ADDON_XMLHINT);

	// addon - search/replace
	private static final PreRenderable DIALOG_CSS = CssInclude
		.include(RESOURCES.url("js/codemirror/addon/dialog/dialog.css")).prerender(CSS).make();
	private static final PreRenderable ADDON_DIALOG = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/dialog/dialog.js"));
	private static final PreRenderable ADDON_SEARCHCURSOR = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/search/searchcursor.js"));
	private static final PreRenderable ADDON_SEARCH = new IncludeFile(
		RESOURCES.url("js/codemirror/addon/search/search.js"), DIALOG_CSS, ADDON_DIALOG, ADDON_SEARCHCURSOR);

	// MIXED
	private static final PreRenderable JS_MIXED = new IncludeFile(
		RESOURCES.url("js/codemirror/mode/htmlmixed/htmlmixed.js"), JS_JS, JS_XML, JS_CSS);

	private static final JSCallable APPLY_JAVASCRIPT = new ExternallyDefinedFunction("CodeMirror.fromTextArea", 2,
		PRENDER, JS_JS, ADDON_FULLSCREEN, ADDON_SEARCH, ADDON_JAVASCRIPTHINT, ADDON_ATUOBRACKET);
	private static final JSCallable APPLY_JAVASCRIPT_WITHOUT_FULLSCREEN = new ExternallyDefinedFunction(
		"CodeMirror.fromTextArea", 2, PRENDER, JS_JS, ADDON_SEARCH, ADDON_JAVASCRIPTHINT, ADDON_ATUOBRACKET);

	private static final JSCallable APPLY_FREEMARKER = new ExternallyDefinedFunction("CodeMirror.fromTextArea", 2,
		PRENDER, JS_MIXED, ADDON_FULLSCREEN, ADDON_SEARCH, ADDON_HTMLHINT, ADDON_ATUOBRACKET);
	private static final JSCallable APPLY_FREEMARKER_WITHOUT_FULLSCREEN = new ExternallyDefinedFunction(
		"CodeMirror.fromTextArea", 2, PRENDER, JS_MIXED, ADDON_SEARCH, ADDON_HTMLHINT, ADDON_ATUOBRACKET);

	private static final JSCallable APPLY_CSS = new ExternallyDefinedFunction("CodeMirror.fromTextArea", 2, PRENDER,
		JS_CSS, ADDON_FULLSCREEN, ADDON_SEARCH, ADDON_CSSHINT, ADDON_ATUOBRACKET);
	private static final JSCallable APPLY_CSS_WITHOUT_FULLSCREEN = new ExternallyDefinedFunction(
		"CodeMirror.fromTextArea", 2, PRENDER, JS_CSS, ADDON_SEARCH, ADDON_CSSHINT, ADDON_ATUOBRACKET);

	private static String array[] = {"CodeMirror-linenumbers"};

	private static ObjectExpression FREEMARKER_PARAMS = new ObjectExpression("mode", "text/html", "theme", "eclipse",
		"lineNumbers", true, "tabMode", "shift", "enterMode", "keep", "indentUnit", 4, "gutters", array,
		"autoCloseBrackets", true);
	public static final ObjectExpression JAVASCRIPT_PARAMS = new ObjectExpression("mode", "text/javascript", "theme",
		"eclipse", "lineNumbers", true, "tabMode", "shift", "enterMode", "keep", "indentUnit", 4, "gutters", array,
		"autoCloseBrackets", true);
	public static final ObjectExpression CSS_PARAMS = new ObjectExpression("mode", "text/css", "theme", "eclipse",
		"lineNumbers", true, "tabMode", "shift", "enterMode", "keep", "indentUnit", 4, "gutters", array,
		"autoCloseBrackets", true);

	/**
	 * @param textField
	 * @return The CodeMirror variable for this editor
	 */
	public static ScriptVariable addFreemarkerEditing(HtmlValueState textField, boolean allowFullscreen)
	{
		if( allowFullscreen )
		{
			return initCodeMirror(textField, APPLY_FREEMARKER, FREEMARKER_PARAMS);
		}
		return initCodeMirror(textField, APPLY_FREEMARKER_WITHOUT_FULLSCREEN, FREEMARKER_PARAMS);

	}

	/**
	 * @param textField
	 * @return The CodeMirror variable for this editor
	 */
	public static ScriptVariable addCssEditing(HtmlValueState textField, boolean allowFullscreen)
	{
		if( allowFullscreen )
		{
			return initCodeMirror(textField, APPLY_CSS, CSS_PARAMS);
		}
		return initCodeMirror(textField, APPLY_CSS_WITHOUT_FULLSCREEN, CSS_PARAMS);

	}

	/**
	 * @param textField
	 * @return The CodeMirror variable for this editor
	 */
	public static ScriptVariable addJavascriptEditing(HtmlValueState textField, boolean allowFullscreen)
	{
		if( allowFullscreen )
		{
			return initCodeMirror(textField, APPLY_JAVASCRIPT, JAVASCRIPT_PARAMS);
		}
		return initCodeMirror(textField, APPLY_JAVASCRIPT_WITHOUT_FULLSCREEN, JAVASCRIPT_PARAMS);
	}

	private static ScriptVariable initCodeMirror(HtmlValueState textField, JSCallable initFunc, ObjectExpression params)
	{
		ScriptVariable cm = new ScriptVariable("cm", textField);
		JSStatements as = new AssignStatement(cm, Js.call(initFunc, textField, params));
		textField.addReadyStatements(as);
		return cm;
	}

	private CodeMirrorLibrary()
	{
		throw new Error();
	}
}
