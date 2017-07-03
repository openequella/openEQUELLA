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

package com.tle.web.activation.filter;

import java.util.Collections;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.NotEqualsExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.dialog.renderer.ButtonKeys;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class SelectCourseDialog extends AbstractOkayableDialog<SelectCourseDialog.Model>
{
	private static final String DIVID_BUTTONS = "buttons";
	private static final String DIVID_RESULTS = "results";

	@PlugKey("selectcoursedialog.title")
	private static Label LABEL_TITLE;
	@PlugKey("utils.selectuserdialog.validation.enterquery")
	private static Label ENTER_QUERY_LABEL;
	@PlugKey("selectsoursedialog.textfield.hint")
	private static Label TEXTFIELD_HINT;

	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "q", contexts = BookmarkEvent.CONTEXT_SESSION)
	private TextField query;
	@Component(name = "s")
	private Button search;
	@Component(name = "c")
	private SingleSelectionList<CourseInfo> courseList;

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "selectcourse";
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	private boolean validQuery(String query)
	{
		String q = Strings.nullToEmpty(query);
		for( int i = 0; i < q.length(); i++ )
		{
			if( Character.isLetterOrDigit(q.codePointAt(i)) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String getWidth()
	{
		return "600px";
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final Model model = getModel(context);

		final String theQuery = Strings.nullToEmpty(query.getValue(context));
		final boolean invalidQuery = theQuery.length() > 0 && !validQuery(theQuery);
		final boolean hasNoResults = !Check.isEmpty(theQuery)
			&& courseList.getListModel().getOptions(context).size() == 0;

		model.setInvalidQuery(invalidQuery);
		model.setHasNoResults(hasNoResults);

		if( !hasNoResults && !invalidQuery )
		{
			getOk().setClickHandler(
				context,
				new OverrideHandler(new FunctionCallStatement(getOkCallback(), courseList.createGetExpression()),
					new FunctionCallStatement(getCloseFunction())));
		}

		if( courseList.getSelectedValue(context) == null )
		{
			getOk().disable(context);
		}

		return viewFactory.createResult("selectcourse.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);

		CourseListModel listModel = new CourseListModel();
		listModel.setSort(true);
		courseList.setListModel(listModel);

		query.addTagProcessor(new JQueryTextFieldHint(TEXTFIELD_HINT, query));
	}

	@EventHandlerMethod
	public void doSearch(SectionInfo info)
	{
		courseList.setSelectedStringValue(info, null);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		OverrideHandler handler = new OverrideHandler(ajaxEvents.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("doSearch"), ajaxEvents.getEffectFunction(EffectType.REPLACE_IN_PLACE),
			DIVID_RESULTS, DIVID_BUTTONS));
		handler.addValidator(new SimpleValidator(new NotEqualsExpression(query.createGetExpression(),
			new StringExpression(""))).setFailureStatements(Js.alert_s(ENTER_QUERY_LABEL)));
		search.setClickHandler(handler);
		courseList.setEventHandler(JSHandler.EVENT_CHANGE, new OverrideHandler(getOk().createDisableFunction(),
			new NotExpression(courseList.createNotEmptyExpression())));
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return null;
	}

	public SingleSelectionList<CourseInfo> getCourseList()
	{
		return courseList;
	}

	public class CourseListModel extends DynamicHtmlListModel<CourseInfo>
	{
		@Override
		protected Iterable<CourseInfo> populateModel(SectionInfo info)
		{
			String queryText = query.getValue(info);
			if( Check.isEmpty(queryText) )
			{
				return Collections.emptyList();
			}
			return courseInfoService.search(queryText, false, 0, 100);
		}

		@Override
		protected Option<CourseInfo> convertToOption(SectionInfo info, CourseInfo obj)
		{
			return new NameValueOption<CourseInfo>(new BundleNameValue(obj.getName(), obj.getUuid(), bundleCache), obj);
		}
	}

	@Override
	protected Label getOkLabel()
	{
		return new KeyLabel(ButtonKeys.OK);
	}

	public TextField getQuery()
	{
		return query;
	}

	public Button getSearch()
	{
		return search;
	}

	public static class Model extends DialogModel
	{
		private boolean invalidQuery;
		private boolean hasNoResults;

		public boolean isInvalidQuery()
		{
			return invalidQuery;
		}

		public void setInvalidQuery(boolean invalidQuery)
		{
			this.invalidQuery = invalidQuery;
		}

		public boolean isHasNoResults()
		{
			return hasNoResults;
		}

		public void setHasNoResults(boolean hasNoResults)
		{
			this.hasNoResults = hasNoResults;
		}
	}
}
