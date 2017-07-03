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

package com.tle.web.search.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dytech.edge.exceptions.BadRequestException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
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

@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractFilterByDateRangeSection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractPrototypeSection<AbstractFilterByDateRangeSection.Model>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<SE>
{
	private static final String CLEAR_DIV = "clear";

	@ResourceHelper
	private PluginResourceHelper RESOURCES;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(name = "dp", supported = true)
	private Calendar datePrimary;
	@Component(name = "ds", supported = true)
	private Calendar dateSecondary;
	@Component(name = "r", supported = true)
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
		String[] names = getParameterNames();
		datePrimary.setParameterId(names[0]);
		dateSecondary.setParameterId(names[1]);
		range.setParameterId(names[2]);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	protected abstract String[] getParameterNames();

	public abstract Label getTitle();

	public abstract String getAjaxDiv();

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		datePrimary.setEventHandler(JSHandler.EVENT_CHANGE,
			new OverrideHandler(searchResults.getResultsUpdater(tree, events.getEventHandler("showClear"), CLEAR_DIV)));
		dateSecondary.setEventHandler(JSHandler.EVENT_CHANGE,
			new OverrideHandler(searchResults.getResultsUpdater(tree, events.getEventHandler("showClear"), CLEAR_DIV)));

		elementId = new SimpleElementId(id + "between");

		ExternallyDefinedFunction showhide = new ExternallyDefinedFunction("showhide", new IncludeFile(
			RESOURCES.url("js/filterbydate.js"), JQueryUIEffects.BLIND));
		range.addChangeEventHandler(new OverrideHandler(new StatementHandler(showhide, elementId, range
			.createGetExpression()), searchResults.getRestartSearchHandler(tree)));

		clearButton.setClickHandler(new OverrideHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("clear"), getAjaxDiv())));
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
		Model model = getModel(info);
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
		getModel(context).setBetween(new DivRenderer(tagState));
		return viewFactory.createResult("filter/filterbydate.ftl", context);
	}

	@Nullable
	protected Date[] getDateRange(SectionInfo info)
	{
		TleDate priDate = datePrimary.getDate(info);
		TleDate secDate = dateSecondary.getDate(info);

		DateRange selRange;
		try
		{
			selRange = DateRange.valueOf(range.getSelectedValueAsString(info));
		}
		catch( IllegalArgumentException dodge )
		{
			throw new BadRequestException(range.getElementId(info));
		}

		/*
		 * Allows the selection of a start date ahead of the current end date
		 * the same as the wizard control
		 */
		if( secDate != null && (priDate != null && priDate.after(secDate)) && selRange.equals(DateRange.BETWEEN) )
		{
			final TleDate temp = secDate;
			secDate = priDate;
			priDate = temp;
		}

		switch( selRange )
		{
			case AFTER:
				if( priDate != null )
				{
					return new Date[]{toDate(priDate), null};
				}
				break;
			case BEFORE:
				if( priDate != null )
				{
					return new Date[]{null, toDate(priDate)};
				}
				break;
			case BETWEEN:
				if( priDate != null || secDate != null )
				{
					return new Date[]{toDate(priDate), toDate(secDate)};
				}
				break;
			case ON:
				if( priDate != null )
				{
					return new Date[]{toDate(priDate), toDate(priDate.addDays(1))};
				}
				break;
		}
		return null;
	}

	@Nullable
	private Date toDate(@Nullable TleDate date)
	{
		if( date == null )
		{
			return null;
		}
		return UtcDate.convertUtcMidnightToLocalMidnight(date, CurrentTimeZone.get()).toDate();
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
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

	public static class Model
	{
		private boolean showClearLink;
		private DivRenderer between;

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

	@Override
	public void reset(SectionInfo info)
	{
		clear(info);
		range.setSelectedStringValue(info, null);
	}
}
