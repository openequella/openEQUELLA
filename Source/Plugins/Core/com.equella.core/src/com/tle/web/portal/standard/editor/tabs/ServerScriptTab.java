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

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.BootstrapDropDownRenderer;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.standard.CodeMirror;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;

@SuppressWarnings("nls")
public class ServerScriptTab extends AbstractScriptingTab<Object>
{
	private static final NameValue NAME = new BundleNameValue(ResourcesService.getResourceHelper(ServerScriptTab.class)
		.key("editor.freemarker.tab.server.name"), "server"); //$NON-NLS-2$

	@Inject
	private UserScriptsService userScriptService;

	@Component(name = "scpt")
	private CodeMirror scriptEditor;

	@Component(name = "jsl")
	@PlugKey("freemarker.scriptlist.action")
	private SingleSelectionList<UserScript> javaScriptList;

	@Override
	public NameValue getTabToAppearOn()
	{
		return NAME;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		javaScriptList.setDisplayed(context, userScriptService.executableScriptsAvailable());
		return thisView.createResult("edit/tabs/serverscripttab.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		scriptEditor.setEditorType(EditorType.JAVASCRIPT_EDITOR);
		scriptEditor.setAllowFullScreen(true);
		scriptEditor.setShowHelp(true);

		javaScriptList.setListModel(new ScriptListModel(true));
		javaScriptList.addChangeEventHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("loadUserScript"), "server-editor")));
		javaScriptList.setDefaultRenderer(BootstrapDropDownRenderer.RENDER_CONSTANT);

	}

	@Override
	public void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig()) : new PropBagEx();
		scriptEditor.setValue(info, config.getNode("script"));
	}

	@Override
	public void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig()) : new PropBagEx();
		if( scriptEditor.getValue(info) != null )
		{
			config.setNode("script", scriptEditor.getValue(info));
		}

		portlet.setConfig(config.toString());
	}

	@Override
	public void customClear(SectionInfo info)
	{
		scriptEditor.setValue(info, Constants.BLANK);
	}

	@Override
	protected JSExpression getEditor()
	{
		ScriptVariable cm = new ScriptVariable("cm", scriptEditor);
		return cm;
	}

	public CodeMirror getScriptEditor()
	{
		return scriptEditor;
	}

	public SingleSelectionList<UserScript> getJavaScriptList()
	{
		return javaScriptList;
	}

	@EventHandlerMethod(priority = SectionEvent.PRIORITY_HIGH)
	public void loadUserScript(SectionInfo info)
	{
		String script = userScriptService.getByUuid(javaScriptList.getSelectedValueAsString(info)).getScript();
		scriptEditor.setValue(info, script);
	}
}
