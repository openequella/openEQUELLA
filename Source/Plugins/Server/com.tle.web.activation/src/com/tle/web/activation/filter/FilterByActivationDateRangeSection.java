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

import java.util.Date;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.searching.DateFilter;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class FilterByActivationDateRangeSection
	extends
		AbstractPrototypeSection<FilterByActivationDateRangeSection.Model>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(name = "de1", parameter = "de1", supported = true)
	private Calendar dateEnd1;

	@Component(name = "de2", parameter = "de2", supported = true)
	private Calendar dateEnd2;

	@Component(name = "ds1", parameter = "ds1", supported = true)
	private Calendar dateStart1;

	@Component(name = "ds2", parameter = "ds2", supported = true)
	private Calendar dateStart2;

	@Component(name = "da1", parameter = "da1", supported = true)
	private Calendar dateActivate1;

	@Component(name = "da2", parameter = "da2", supported = true)
	private Calendar dateActivate2;

	@Component
	private Button clearButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		OverrideHandler handler = new OverrideHandler(searchResults.getResultsUpdater(tree, null,
			"activation-date-clear"));

		dateEnd1.setEventHandler(JSHandler.EVENT_CHANGE, handler);
		dateEnd2.setEventHandler(JSHandler.EVENT_CHANGE, handler);
		dateStart1.setEventHandler(JSHandler.EVENT_CHANGE, handler);
		dateStart2.setEventHandler(JSHandler.EVENT_CHANGE, handler);
		dateActivate1.setEventHandler(JSHandler.EVENT_CHANGE, handler);
		dateActivate2.setEventHandler(JSHandler.EVENT_CHANGE, handler);

		clearButton.setClickHandler(new OverrideHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("clear"), "activation-date-range-filter")));
	}

	@EventHandlerMethod
	public void clear(SectionInfo info)
	{
		dateEnd1.clearDate(info);
		dateEnd2.clearDate(info);

		dateStart1.clearDate(info);
		dateStart2.clearDate(info);

		dateActivate1.clearDate(info);
		dateActivate2.clearDate(info);

		getModel(info).setShowClearLink(false);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/filterbyactivationdate.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		TleDate de1 = dateEnd1.getDate(info);
		TleDate de2 = dateEnd2.getDate(info);
		TleDate ds1 = dateStart1.getDate(info);
		TleDate ds2 = dateStart2.getDate(info);
		TleDate da1 = dateActivate1.getDate(info);
		TleDate da2 = dateActivate2.getDate(info);

		setDates(event, convertToRange(ds1, ds2), convertToRange(de1, de2), convertToRange(da1, da2));

		getModel(info).setShowClearLink(checkAnyDatesSelected(de1, de2, ds1, ds2, da1, da2));
	}

	private boolean checkAnyDatesSelected(TleDate... dates)
	{
		for( TleDate date : dates )
		{
			if( date != null )
			{
				return true;
			}
		}
		return false;
	}

	private TleDate[] convertToRange(TleDate priDate, TleDate secDate)
	{
		TleDate d1 = priDate;
		TleDate d2 = secDate;

		/*
		 * Allows the selection of a start date ahead of the current end date
		 * the same as the wizard control
		 */
		if( d2 != null && (d1 != null && d1.after(d2)) )
		{
			final TleDate temp = d2;
			d2 = d1;
			d1 = temp;
		}

		d2 = (d2 == null ? null : new UtcDate(d2.toLong()).addDays(1));

		if( d1 != null || d2 != null )
		{
			return new TleDate[]{d1, d2};
		}
		return null;
	}

	private void setDates(FreetextSearchEvent event, TleDate[] startDateRange, TleDate[] endDateRange,
		TleDate[] activateddateRange)
	{
		if( startDateRange != null )
		{
			event.addDateFilter(new DateFilter(FreeTextQuery.FIELD_ACTIVATION_FROM, convertDates(startDateRange)));
		}

		if( endDateRange != null )
		{
			event.addDateFilter(new DateFilter(FreeTextQuery.FIELD_ACTIVATION_UNTIL, convertDates(endDateRange)));
		}

		if( activateddateRange != null )
		{
			event.filterByDateRange(convertDates(activateddateRange));
		}
	}

	private Date[] convertDates(TleDate[] dates)
	{
		Date[] rng = new Date[2];
		TleDate from = dates[0];
		TleDate to = dates[1];
		if( from != null )
		{
			rng[0] = UtcDate.convertUtcMidnightToLocalMidnight(from, CurrentTimeZone.get()).toDate();
		}
		if( to != null )
		{
			rng[1] = UtcDate.convertUtcMidnightToLocalMidnight(to, CurrentTimeZone.get()).toDate();
		}
		return rng;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbadr";
	}

	public Calendar getDateEnd1()
	{
		return dateEnd1;
	}

	public Calendar getDateEnd2()
	{
		return dateEnd2;
	}

	public Button getClearButton()
	{
		return clearButton;
	}

	public Calendar getDateStart1()
	{
		return dateStart1;
	}

	public Calendar getDateStart2()
	{
		return dateStart2;
	}

	public Calendar getDateActivate1()
	{
		return dateActivate1;
	}

	public Calendar getDateActivate2()
	{
		return dateActivate2;
	}

	@Override
	public void reset(SectionInfo info)
	{
		clear(info);
	}

	@Override
	public Class<Model> getModelClass()
	{
		return Model.class;
	}

	public static class Model
	{
		private boolean showClearLink;

		public void setShowClearLink(boolean showClearLink)
		{
			this.showClearLink = showClearLink;
		}

		public boolean isShowClearLink()
		{
			return showClearLink;
		}

	}
}
