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

@SuppressWarnings("nls")
public class FreemarkerTab extends AbstractScriptingTab<FreemarkerTab.FreemarkerTabModel>
{
	private static final NameValue NAME = new BundleNameValue(ResourcesService.getResourceHelper(FreemarkerTab.class)
		.key("editor.freemarker.tab.markup.name"), "markup");

	@PlugKey("editor.freemarker.tab.freemarker.link.delete")
	private static Label LABEL_LINK_DELETE;

	@PlugKey("editor.freemarker.tab.freemarker.column.link")
	private static Label LABEL_CSS;
	@PlugKey("editor.freemarker.tab.freemarker.empty")
	private static Label LABEL_EMPTY_LIST;

	@Inject
	private PortletService portletService;
	@Inject
	private UserScriptsService userScriptService;

	@Component
	@PlugKey("editor.freemarker.tab.freemarker.link.add")
	private Link addCssLink;
	@Component(name = "ecss", stateful = false)
	private TextField externalCss;
	@Component(name = "cfl", stateful = false)
	private MutableList<String> cssFileList;
	@Component(name = "ct")
	private SelectionsTable cssTable;
	@Component(name = "fml")
	@PlugKey("freemarker.scriptlist.action")
	private SingleSelectionList<UserScript> freeMarkerList;

	private JSCallable deleteFunction;

	@Component(name = "m", stateful = false)
	private CodeMirror scriptEditor;

	@Override
	public NameValue getTabToAppearOn()
	{
		return NAME;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		freeMarkerList.setDisplayed(context, userScriptService.displayScriptsAvailable());
		return thisView.createResult("edit/tabs/freemarkertab.ftl", context);
	}

	@Override
	public Class<FreemarkerTabModel> getModelClass()
	{
		return FreemarkerTabModel.class;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		scriptEditor.setEditorType(EditorType.FREEMARKER_EDITOR);
		scriptEditor.setAllowFullScreen(true);
		scriptEditor.setShowHelp(true);

		cssFileList.setListModel(new StringListModel());
		cssFileList.setStyle("display: none;");

		addCssLink.setClickHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("addCss"), "selectedCss"),
			externalCss.createGetExpression());

		deleteFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteCss"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "selectedCss");

		cssTable.setColumnHeadings(LABEL_CSS, null);
		cssTable.setColumnSorts(Sort.PRIMARY_ASC);
		cssTable.setSelectionsModel(new CssTableModel());
		cssTable.setNothingSelectedText(LABEL_EMPTY_LIST);

		freeMarkerList.setListModel(new ScriptListModel(false));
		freeMarkerList.addChangeEventHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("loadUserScript"), "fm-editor")));
		freeMarkerList.setDefaultRenderer(BootstrapDropDownRenderer.RENDER_CONSTANT);
	}

	@EventHandlerMethod
	public void addCss(SectionInfo info, String url)
	{
		if( !Check.isEmpty(url) )
		{
			MutableListModel<String> listModel = cssFileList.getListModel();
			if( !listModel.contains(info, url) )
			{
				listModel.add(info, url);
			}
			externalCss.setValue(info, "");
		}
	}

	@EventHandlerMethod
	public void deleteCss(SectionInfo info, String url)
	{
		if( !Check.isEmpty(url) )
		{
			cssFileList.getListModel().remove(info, url);
			PortletEditingBean portlet = portletService.loadSession(freemarkerEditor.getModel(info).getSessionId())
				.getBean();

			PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig())
				: new PropBagEx();
			config.deleteAll("external-css");
			Collection<String> values = cssFileList.getListModel().getValues(info);
			for( String val : values )
			{
				config.createNode("external-css", val);
			}
			portlet.setConfig(config.toString());
		}
	}

	@Override
	public void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig()) : new PropBagEx();
		scriptEditor.setValue(info, config.getNode("markup"));

		List<String> csses = config.getNodeList("external-css");
		MutableListModel<String> listModel = cssFileList.getListModel();
		for( String css : csses )
		{
			if( !listModel.contains(info, css) )
			{
				listModel.add(info, css);
			}
		}
	}

	@Override
	public void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		PropBagEx config = !Check.isEmpty(portlet.getConfig()) ? new PropBagEx(portlet.getConfig()) : new PropBagEx();
		if( scriptEditor.getValue(info) != null )
		{
			config.setNode("markup", scriptEditor.getValue(info));
		}

		config.deleteAll("external-css");
		Collection<String> values = cssFileList.getListModel().getValues(info);
		for( String url : values )
		{
			config.createNode("external-css", url);
		}
		portlet.setConfig(config.toString());
	}

	@Override
	public void customClear(SectionInfo info)
	{
		scriptEditor.setValue(info, Constants.BLANK);
		cssFileList.getListModel().clear(info);
	}

	public CodeMirror getScriptEditor()
	{
		return scriptEditor;
	}

	public Link getAddCssLink()
	{
		return addCssLink;
	}

	public MutableList<String> getCssFileList()
	{
		return cssFileList;
	}

	public TextField getExternalCss()
	{
		return externalCss;
	}

	public SelectionsTable getCssTable()
	{
		return cssTable;
	}

	private class CssTableModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			return cssFileList.getListModel().getValues(info);
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String url,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new TextLabel(url)));
			actions.add(makeRemoveAction(LABEL_LINK_DELETE, new OverrideHandler(deleteFunction, url)));
		}
	}

	public static class FreemarkerTabModel
	{
		// nothing needs to exist in this model ...?
	}

	@EventHandlerMethod(priority = SectionEvent.PRIORITY_HIGH)
	public void loadUserScript(SectionInfo info)
	{
		String script = userScriptService.getByUuid(freeMarkerList.getSelectedValueAsString(info)).getScript();
		scriptEditor.setValue(info, script);
	}

	public SingleSelectionList<UserScript> getFreeMarkerList()
	{
		return freeMarkerList;
	}

	@Override
	protected JSExpression getEditor()
	{
		ScriptVariable cm = new ScriptVariable("cm", scriptEditor);
		return cm;
	}
}
