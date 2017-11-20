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

package com.tle.web.portal.standard.editor.tabs;

import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.standard.editor.FreemarkerPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractScriptingTab<T> extends AbstractPrototypeSection<T>
	implements
		HtmlRenderer,
		ScriptingTabInterface
{
	@TreeLookup
	protected FreemarkerPortletEditorSection freemarkerEditor;

	@ViewFactory
	protected FreemarkerFactory thisView;
	@EventFactory
	protected EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Inject
	private UserScriptsService userScriptService;
	@Inject
	private BundleCache bundleCache;

	@Override
	public JSStatements getTabShowStatements()
	{
		return Js.iff(getEditor(), Js.statement(Js.methodCall(getEditor(), Js.function("refresh"))));
	}

	protected abstract JSExpression getEditor();

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public void customValidate(SectionInfo info, Map<String, Object> errors)
	{
		// Nothing here
	}

	public class ScriptListModel extends DynamicHtmlListModel<UserScript>
	{
		boolean javascript;

		public ScriptListModel(boolean javascript)
		{
			this.javascript = javascript;
		}

		@Override
		protected Option<UserScript> convertToOption(SectionInfo info, UserScript script)
		{
			return new NameValueOption<UserScript>(
				new BundleNameValue(script.getName(), script.getUuid(), bundleCache), script);
		}

		@Override
		protected Iterable<UserScript> populateModel(SectionInfo info)
		{
			if( javascript )
			{
				return userScriptService.enumerateForType(ScriptTypes.EXECUTABLE);
			}
			else
			{
				return userScriptService.enumerateForType(ScriptTypes.DISPLAY);
			}
		}

	}
}
