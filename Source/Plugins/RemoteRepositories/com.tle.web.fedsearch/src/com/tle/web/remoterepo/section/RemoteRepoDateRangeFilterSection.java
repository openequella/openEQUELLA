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

package com.tle.web.remoterepo.section;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.DivRenderer;

@SuppressWarnings("nls")
public abstract class RemoteRepoDateRangeFilterSection<E extends RemoteRepoSearchEvent<E>>
	extends
		AbstractPrototypeSection<RemoteRepoDateRangeFilterSection.RemoteRepoDateRangeFilterModel>
	implements
		HtmlRenderer,
		// ResetFiltersListener,
		SearchEventListener<E>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(RemoteRepoDateRangeFilterSection.class);

	private static final ExternallyDefinedFunction SHOWHIDE_SECONDDATE_FUNCTION = new ExternallyDefinedFunction(
		"showhide", new IncludeFile(resources.url("scripts/daterangefilter.js"), JQueryUIEffects.BLIND));

	// private static final String DATE_RANGE_FILTER = "date-range-filter";
	private static final String CLEAR_DIV = "clear";

	@ResourceHelper
	private PluginResourceHelper RESOURCES;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(name = "dp", parameter = "dp", supported = true)
	private Calendar datePrimary;
	@Component(name = "ds", parameter = "ds", supported = true)
	private Calendar dateSecondary;
	@Component(name = "r", parameter = "dr", supported = true)
	private SingleSelectionList<BundleNameValue> range;
	@Component
	private Button clearButton;

	private ElementId elementId;

	public enum DateRange
	{
		AFTER("filter.bydate.after"), BEFORE("filter.bydate.before"), BETWEEN("filter.bydate.between"), ON(
			"filter.bydate.on");

		private final String name;

		DateRange(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		List<BundleNameValue> rangeOptions = new ArrayList<BundleNameValue>();
		for( DateRange dr : DateRange.values() )
		{
			rangeOptions.add(new BundleNameValue(RESOURCES.key(dr.getName()), dr.name()));
		}

		range.setListModel(new SimpleHtmlListModel<BundleNameValue>(rangeOptions));
		range.setAlwaysSelect(true);
		datePrimary.setConceptual(true);
		dateSecondary.setConceptual(true);

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		final JSHandler updateClear = new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("showClear"), CLEAR_DIV));
		datePrimary.setEventHandler(JSHandler.EVENT_CHANGE, updateClear);
		dateSecondary.setEventHandler(JSHandler.EVENT_CHANGE, updateClear);

		elementId = new SimpleElementId(id + "between");

		range.addChangeEventHandler(new StatementHandler(SHOWHIDE_SECONDDATE_FUNCTION, elementId, range
			.createGetExpression()));
		clearButton.setClickHandler(events.getNamedHandler("clear"));
	}

	@EventHandlerMethod
	public void clear(SectionInfo info)
	{
		datePrimary.clearDate(info);
		dateSecondary.clearDate(info);
		getModel(info).setShowClearLink(false);
	}

	@EventHandlerMethod
	public void showClear(SectionInfo info)
	{
		checkForDates(info);
	}

	private void checkForDates(SectionInfo info)
	{
		RemoteRepoDateRangeFilterModel model = getModel(info);
		if( datePrimary.isDateSet(info) || dateSecondary.isDateSet(info) )
		{
			model.setShowClearLink(true);
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		TagState tagState = new TagState();
		tagState.setElementId(elementId);

		checkForDates(context);

		if( !range.getSelectedValueAsString(context).equals(DateRange.BETWEEN.toString()) )
		{
			tagState.setStyle("display: none");
		}
		final RemoteRepoDateRangeFilterModel model = getModel(context);
		model.setBetween(new DivRenderer(tagState));
		model.setTitle(getTitle(context));
		return viewFactory.createResult("remoterepofilterbydate.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, RemoteRepoSearchEvent event) throws Exception
	{
		TleDate[] dateRange = null;

		TleDate priDate = datePrimary.getDate(info);
		TleDate secDate = dateSecondary.getDate(info);

		/*
		 * Allows the selection of a start date ahead of the current end date
		 * the same as the wizard control
		 */
		if( secDate != null && priDate.after(secDate) )
		{
			secDate = priDate;
		}

		if( priDate != null )
		{
			switch( DateRange.valueOf(range.getSelectedValueAsString(info)) )
			{
				case AFTER:
					dateRange = new TleDate[]{priDate, null};
					break;
				case BEFORE:
					dateRange = new TleDate[]{null, priDate};
					break;
				case BETWEEN:
					dateRange = new TleDate[]{priDate, secDate};
					break;
				case ON:
					dateRange = new TleDate[]{priDate, new UtcDate(priDate.toLong() + TimeUnit.DAYS.toMillis(1))};
					break;
			}
		}

		if( dateRange != null )
		{
			processDateRange(info, event, dateRange);
		}
	}

	protected abstract Label getTitle(SectionInfo info);

	protected abstract void processDateRange(SectionInfo info, RemoteRepoSearchEvent event, TleDate[] dateRange);

	@Override
	public Class<RemoteRepoDateRangeFilterModel> getModelClass()
	{
		return RemoteRepoDateRangeFilterModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbdr";
	}

	public Calendar getDatePrimary()
	{
		return datePrimary;
	}

	public Calendar getDateSecondary()
	{
		return dateSecondary;
	}

	public SingleSelectionList<BundleNameValue> getRange()
	{
		return range;
	}

	public Button getClearButton()
	{
		return clearButton;
	}

	public static class RemoteRepoDateRangeFilterModel
	{
		private boolean showClearLink;
		private DivRenderer between;
		private Label title;

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public DivRenderer getBetween()
		{
			return between;
		}

		public void setBetween(DivRenderer between)
		{
			this.between = between;
		}

		public boolean isShowClearLink()
		{
			return showClearLink;
		}

		public void setShowClearLink(boolean showClearLink)
		{
			this.showClearLink = showClearLink;
		}
	}

	// @Override
	// public void reset(SectionInfo info)
	// {
	// clear(info);
	// range.setSelectedStringValue(info, null);
	// }
}
