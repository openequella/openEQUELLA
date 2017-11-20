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

package com.tle.web.itemadmin.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.MutableList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.SelectModule;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.sections.standard.model.StringListModel;

@Bind
@SuppressWarnings("nls")
public class ItemAdminWhereSection extends AbstractPrototypeSection<ItemAdminWhereSection.ItemAdminWhereSectionModel>
	implements
		HtmlRenderer,
		BeforeEventsListener
{
	static
	{
		PluginResourceHandler.init(ItemAdminWhereSection.class);
	}

	@PlugURL("js/whereclause.js")
	private static String url;

	private static JSCallable CRITERIA_FUNC = new ExternallyDefinedFunction("criteria", new IncludeFile(url),
		JQueryCore.PRERENDER, SelectModule.INCLUDE);

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	@Component
	private ItemAdminXPathDialog xpathDialog;

	@Component
	private MutableList<String> whereClauses;
	@Component
	private SingleSelectionList<Void> whereStart;
	@Component
	private SingleSelectionList<Void> whereOperator;
	@Component(stateful = false)
	private TextField wherePath;
	@Component(stateful = false)
	private TextField whereValue;
	@Component
	private Div div;

	@PlugKey("whereclause.browse")
	@Component
	private Button browse;

	@PlugKey("whereclause.add")
	@Component
	private Button add;

	@PlugKey("whereclause.search")
	@Component
	private Button search;

	@PlugKey("whereclause.xpath.where")
	private static String WHERE;
	@PlugKey("whereclause.xpath.and")
	private static String AND;
	@PlugKey("whereclause.xpath.or")
	private static String OR;

	@PlugKey("whereclause.xpath.is")
	private static String IS;
	@PlugKey("whereclause.xpath.isnot")
	private static String ISNOT;

	@PlugKey("whereclause.xpath.like")
	private static String LIKE;
	@PlugKey("whereclause.xpath.notlike")
	private static String NOTLIKE;

	@PlugKey("whereclause.xpath.datewarning")
	private static String DATE_WARNING;

	@TreeLookup
	private RootItemAdminSection rootItemAdmin;
	@TreeLookup
	private ItemAdminQuerySection querySection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setupWhereClause();
		search.setClickHandler(events.getNamedHandler("setupCriteria"));

		ObjectExpression strs = new ObjectExpression();
		strs.put("datewarning", CurrentLocale.get(DATE_WARNING));

		div.addReadyStatements(new ScriptStatement(PropertyExpression.create(new JQuerySelector(div),
			new FunctionCallExpression(CRITERIA_FUNC, strs))));
		div.setStyleClass("criteria");

		whereClauses.setListModel(new StringListModel());
		whereClauses.setStyle("display: none;");

		ScriptVariable xpath = new ScriptVariable("xpath");
		JSCallable setFunction = wherePath.createSetFunction();
		FunctionCallStatement body = new FunctionCallStatement(setFunction, xpath);
		xpathDialog.setOkCallback(new SimpleFunction("setXpath" + id, body, xpath));

		whereClauses.setStyleClass("whereclauses");
		whereStart.setStyleClass("wherestart");
		wherePath.setStyleClass("wherepath");
		whereOperator.setStyleClass("whereoperator");
		whereValue.setStyleClass("wherevalue");

		add.setStyleClass("whereadd");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		browse.setClickHandler(context, new OverrideHandler(xpathDialog.getOpenFunction(), getModel(context)
			.getCollectionId()));
		ContentLayout.setLayout(context, ContentLayout.ONE_COLUMN);
		return viewFactory.createResult("whereclause.ftl", this);
	}

	@EventHandlerMethod
	public void setupCriteria(SectionInfo info)
	{
		getModel(info).setEditQuery(false);
		List<String> clauses = whereClauses.getValues(info);
		querySection.getModel(info).setCriteria(clauses);
	}

	public void clearOptions(SectionInfo info)
	{
		List<String> empty = Collections.emptyList();
		whereClauses.setValues(info, empty);
	}

	private void setupWhereClause()
	{
		List<Option<Void>> startOps = new ArrayList<Option<Void>>();
		startOps.add(new VoidKeyOption(WHERE, "WHERE"));
		startOps.add(new VoidKeyOption(AND, "AND"));
		startOps.add(new VoidKeyOption(OR, "OR"));
		whereStart.setListModel(new SimpleHtmlListModel<Void>(startOps));

		List<Option<Void>> exprOps = new ArrayList<Option<Void>>();
		exprOps.add(new VoidKeyOption(IS, "IS"));
		exprOps.add(new VoidKeyOption(ISNOT, "IS NOT"));
		exprOps.add(new VoidKeyOption(LIKE, "LIKE"));
		exprOps.add(new VoidKeyOption(NOTLIKE, "NOT LIKE"));
		exprOps.add(new SimpleOption<Void>("<", "<"));
		exprOps.add(new SimpleOption<Void>("<=", "<="));
		exprOps.add(new SimpleOption<Void>(">", ">"));
		exprOps.add(new SimpleOption<Void>(">=", ">="));
		whereOperator.setListModel(new SimpleHtmlListModel<Void>(exprOps));
	}

	@Override
	public void beforeEvents(SectionInfo info)
	{
		if( getModel(info).isEditQuery() )
		{
			rootItemAdmin.setModalSection(info, this);
		}
	}

	public void setEditQuery(SectionInfo info, boolean edit, String collectionId)
	{
		ItemAdminWhereSectionModel model = getModel(info);
		model.setEditQuery(edit);
		model.setCollectionId(collectionId);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ItemAdminWhereSectionModel();
	}

	public static class ItemAdminWhereSectionModel
	{
		@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
		private boolean editQuery;
		@Bookmarked
		private String collectionId;

		public boolean isEditQuery()
		{
			return editQuery;
		}

		public void setEditQuery(boolean editQuery)
		{
			this.editQuery = editQuery;
		}

		public String getCollectionId()
		{
			return collectionId;
		}

		public void setCollectionId(String collectionId)
		{
			this.collectionId = collectionId;
		}
	}

	public ItemAdminXPathDialog getXpathDialog()
	{
		return xpathDialog;
	}

	public SingleSelectionList<Void> getWhereStart()
	{
		return whereStart;
	}

	public SingleSelectionList<Void> getWhereOperator()
	{
		return whereOperator;
	}

	public TextField getWherePath()
	{
		return wherePath;
	}

	public TextField getWhereValue()
	{
		return whereValue;
	}

	public Button getBrowse()
	{
		return browse;
	}

	public Button getAdd()
	{
		return add;
	}

	public Button getSearch()
	{
		return search;
	}

	public Div getDiv()
	{
		return div;
	}

	public MutableList<String> getWhereClauses()
	{
		return whereClauses;
	}
}
