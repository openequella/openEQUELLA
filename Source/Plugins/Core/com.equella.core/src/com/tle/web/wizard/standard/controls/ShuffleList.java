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

package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.DeclarationStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.MutableList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.modules.SelectModule;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.StringListModel;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CMultiCtrl;
import com.tle.web.wizard.controls.WebControlModel;

@Bind
@SuppressWarnings("nls")
public class ShuffleList extends AbstractWebControl<WebControlModel> implements JSDisableable
{
	static
	{
		PluginResourceHandler.init(ShuffleList.class);
	}

	@PlugURL("js/shufflelist.js")
	private static String shufflelist_url;

	protected static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(ShuffleList.class);

	private static JSCallable SHUFFLELIST_SETUP = new ExternallyDefinedFunction("shufflelist", new IncludeFile(
		shufflelist_url), new IncludeFile("scripts/utf8.js"), JQueryCore.PRERENDER, SelectModule.INCLUDE);

	@PlugKey("shuffle.mandatory")
	private static Label MANDATORY_ALERT;
	@PlugKey("shuffle.action.edit")
	private static String EDIT;
	@PlugKey("shuffle.action.delete")
	private static String DELETE;

	@ViewFactory(name="wizardFreemarkerFactory")
	private FreemarkerFactory viewFactory;
	@Component
	private Div div;
	@Component
	private TextField text;
	@Component(stateful = false)
	private MutableList<String> list;

	@PlugKey("shuffle.add")
	@Component
	private Button addButton;

	protected CMultiCtrl multi;
	private JSCallable disableFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		list.setParameterId(getFormName());
		list.setListModel(new ListModel());
		list.setStyle("display: none;");

		ObjectExpression multiObj = new ObjectExpression();
		multiObj.put("alertmsg", MANDATORY_ALERT);
		multiObj.put("list", new ElementByIdExpression(list));
		multiObj.put("addbutton", new ElementByIdExpression(addButton));
		multiObj.put("control", new ElementByIdExpression(text));
		multiObj.put("edit", CurrentLocale.get(EDIT));
		multiObj.put("del", CurrentLocale.get(DELETE));

		div.setStyleClass("shuffle");
		div.addReadyStatements(new ScriptStatement(PropertyExpression.create(new JQuerySelector(div),
			new FunctionCallExpression(SHUFFLELIST_SETUP, multiObj))));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisablers(context, addButton, text, list, this);
		return viewFactory.createResult("shufflelist.ftl", context);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		Collection<String> vals = list.getValues(info);
		setValues(vals.toArray(new String[vals.size()]));
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		multi = (CMultiCtrl) control;
	}

	@Override
	public Class<WebControlModel> getModelClass()
	{
		return WebControlModel.class;
	}

	private class ListModel extends StringListModel
	{
		@Override
		public List<Option<String>> getOptions(SectionInfo info)
		{
			List<Option<String>> opts = new ArrayList<Option<String>>();
			List<NameValue> nvs = multi.getNamesValues();
			for( NameValue nameValue : nvs )
			{
				opts.add(new NameValueOption<String>(nameValue, nameValue.getValue()));
			}
			return opts;
		}
	}

	public TextField getText()
	{
		return text;
	}

	public MutableList<String> getList()
	{
		return list;
	}

	public Button getAddButton()
	{
		return addButton;
	}

	public Div getDiv()
	{
		return div;
	}

	@Override
	public JSCallable createDisableFunction()
	{
		if( disableFunc == null )
		{
			ScriptVariable dis = new ScriptVariable("dis");
			ScriptVariable elem = new ScriptVariable("elem");
			disableFunc = new SimpleFunction("dis", this, StatementBlock.get(new DeclarationStatement(elem, div),
				new ScriptStatement("if(dis){$(elem).addClass('disabled');}else{$(elem).removeClass('disabled');}")),
				dis);
		}

		return disableFunc;
	}

	@Override
	protected ElementId getIdForLabel()
	{

		return text;
	}
}
