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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.portal.service.PortletService;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.render.BootstrapDropDownRenderer;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.CodeMirror;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MutableList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;
import com.tle.web.sections.standard.model.MutableListModel;
import com.tle.web.sections.standard.model.StringListModel;

@NonNullByDefault
@SuppressWarnings("nls")
public class ClientScriptTab extends AbstractScriptingTab<ClientScriptTab.ClientScriptTabModel>
{
	private static final NameValue NAME = new BundleNameValue(ResourcesService.getResourceHelper(ServerScriptTab.class)
		.key("editor.freemarker.tab.client.name"), "client");

	@PlugKey("editor.freemarker.tab.client.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("editor.freemarker.tab.client.link.delete")
	private static Label LABEL_LINK_DELETE;
	@PlugKey("editor.freemarker.tab.client.column.link")
	private static Label LABEL_JAVASCRIPT;
	@PlugKey("editor.freemarker.tab.client.empty")
	private static Label LABEL_EMPTY_LIST;

	@Inject
	private PortletService portletService;
	@Inject
	private UserScriptsService userScriptService;

	@Component(name = "escpt", stateful = false)
	private TextField externalJs;
	@Component(name = "cscpt")
	private CodeMirror scriptEditor;
	@Component
	private Link addJavascriptLink;
	@Component(name = "jfl", stateful = false)
	private MutableList<String> javascriptFileList;
	@Component(name = "jt")
	private SelectionsTable javascriptTable;
	@Component(name = "jsl")
	@PlugKey("freemarker.scriptlist.action")
	private SingleSelectionList<UserScript> javaScriptList;

	private JSCallable deleteFunction;

	@Override
	public NameValue getTabToAppearOn()
	{
		return NAME;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		javaScriptList.setDisplayed(context, userScriptService.executableScriptsAvailable());
		return thisView.createResult("edit/tabs/clientscripttab.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		javascriptFileList.setListModel(new StringListModel());
		javascriptFileList.setStyle("display: none;");

		scriptEditor.setEditorType(EditorType.JAVASCRIPT_EDITOR);
		scriptEditor.setAllowFullScreen(true);
		scriptEditor.setShowHelp(true);

		addJavascriptLink.setLabel(LABEL_LINK_ADD);

		addJavascriptLink.setClickHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("addJavascript"), "selectedJS"),
			externalJs.createGetExpression());

		deleteFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteJavascript"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "selectedJS");

		javascriptTable.setColumnHeadings(LABEL_JAVASCRIPT, null);
		javascriptTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE);
		javascriptTable.setSelectionsModel(new JavascriptTableModel());
		javascriptTable.setNothingSelectedText(LABEL_EMPTY_LIST);

		javaScriptList.setListModel(new ScriptListModel(true));
		javaScriptList.addChangeEventHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("loadUserScript"), "client-editor")));
		javaScriptList.setDefaultRenderer(BootstrapDropDownRenderer.RENDER_CONSTANT);

	}

	@Override
	public void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig()) : new PropBagEx();
		scriptEditor.setValue(info, config.getNode("clientscript"));
		List<String> javascript = config.getNodeList("external-javascript");
		MutableListModel<String> listModel = javascriptFileList.getListModel();
		for( String js : javascript )
		{
			if( !listModel.contains(info, js) )
			{
				listModel.add(info, js);
			}
		}
	}

	@EventHandlerMethod
	public void deleteJavascript(SectionInfo info, String url)
	{
		if( !Check.isEmpty(url) )
		{
			javascriptFileList.getListModel().remove(info, url);
			PortletEditingBean portlet = portletService.loadSession(freemarkerEditor.getModel(info).getSessionId())
				.getBean();

			PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig())
				: new PropBagEx();
			config.deleteAll("external-javascript");
			Collection<String> values = javascriptFileList.getListModel().getValues(info);
			for( String val : values )
			{
				config.createNode("external-javascript", val);
			}
			portlet.setConfig(config.toString());
		}
	}

	@EventHandlerMethod
	public void addJavascript(SectionInfo info, String url)
	{
		if( !Check.isEmpty(url) )
		{
			MutableListModel<String> listModel = javascriptFileList.getListModel();
			if( !listModel.contains(info, url) )
			{
				listModel.add(info, url);
			}
			externalJs.setValue(info, "");
		}
	}

	@Override
	public void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig()) : new PropBagEx();
		if( scriptEditor.getValue(info) != null )
		{
			config.setNode("clientscript", scriptEditor.getValue(info));
		}

		config.deleteAll("external-javascript");
		Collection<String> values = javascriptFileList.getListModel().getValues(info);
		for( String url : values )
		{
			config.createNode("external-javascript", url);
		}
		portlet.setConfig(config.toString());
	}

	@Override
	public void customClear(SectionInfo info)
	{
		scriptEditor.setValue(info, Constants.BLANK);
		javascriptFileList.getListModel().clear(info);
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

	@Override
	public Class<ClientScriptTabModel> getModelClass()
	{
		return ClientScriptTabModel.class;
	}

	public Link getAddJavascriptLink()
	{
		return addJavascriptLink;
	}

	public TextField getExternalJs()
	{
		return externalJs;
	}

	public MutableList<String> getJavascriptFileList()
	{
		return javascriptFileList;
	}

	public SelectionsTable getJavascriptTable()
	{
		return javascriptTable;
	}

	private class JavascriptTableModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			return javascriptFileList.getListModel().getValues(info);
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String url,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new TextLabel(url)));
			actions.add(makeRemoveAction(LABEL_LINK_DELETE, new OverrideHandler(deleteFunction, url)));
		}
	}

	public static class ClientScriptTabModel
	{
		// nothing needs to exist in this model ...?
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
