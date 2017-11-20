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

package com.tle.web.portal.standard.editor;

import java.util.List;
import java.util.Map;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.portal.standard.editor.tabs.ScriptingTabInterface;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TabLayout;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class FreemarkerPortletEditorSection
	extends
		AbstractPortletEditorSection<FreemarkerPortletEditorSection.FreemarkerPortletEditorModel>
{
	private static final String TYPE = "freemarker";

	@ViewFactory
	private FreemarkerFactory thisView;

	@Component(stateful = false)
	private TabLayout tabLayout;
	private CollectInterfaceHandler<ScriptingTabInterface> tabSections;

	@Override
	protected SectionRenderable customRender(RenderEventContext context, FreemarkerPortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		return thisView.createResult("edit/editfreemarkerportlet.ftl", context);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "freemarkerEditor";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tabSections = new CollectInterfaceHandler<ScriptingTabInterface>(ScriptingTabInterface.class);
		tree.addRegistrationHandler(tabSections);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		List<ScriptingTabInterface> tabs = tabSections.getAllImplementors(tree);
		JSStatements onShow = new ReturnStatement(true);
		if( CurrentLocale.isRightToLeft() )
		{
			for( int x = (tabs.size() - 1); x >= 0; x-- )
			{
				ScriptingTabInterface tab = tabs.get(x);
				tabLayout.addTabSection(tab);
				onShow = StatementBlock.get(tab.getTabShowStatements(), onShow);
			}
		}
		else
		{
			for( ScriptingTabInterface tab : tabs )
			{
				tabLayout.addTabSection(tab);
				onShow = StatementBlock.get(tab.getTabShowStatements(), onShow);
			}

		}
		tabLayout.addEventStatements("Click", onShow);
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		String config = portlet.getConfig();
		if( !Check.isEmpty(config) )
		{
			try
			{
				List<ScriptingTabInterface> tabs = tabSections.getAllImplementors(info);
				for( ScriptingTabInterface tab : tabs )
				{
					tab.customLoad(info, portlet);
				}
			}
			catch( Exception e )
			{
				// Ignore bad XML
			}
		}
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		List<ScriptingTabInterface> tabs = tabSections.getAllImplementors(info);
		for( ScriptingTabInterface tab : tabs )
		{
			tab.customSave(info, portlet);
		}
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		List<ScriptingTabInterface> tabs = tabSections.getAllImplementors(info);
		for( ScriptingTabInterface tab : tabs )
		{
			tab.customClear(info);
		}
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		List<ScriptingTabInterface> tabs = tabSections.getAllImplementors(info);
		for( ScriptingTabInterface tab : tabs )
		{
			tab.customValidate(info, errors);
		}
	}

	@Override
	public Class<FreemarkerPortletEditorModel> getModelClass()
	{
		return FreemarkerPortletEditorModel.class;
	}

	public static class FreemarkerPortletEditorModel extends AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		// Nothing
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}

	public TabLayout getTabLayout()
	{
		return tabLayout;
	}
}
