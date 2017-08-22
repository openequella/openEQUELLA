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

package com.tle.web.wizard.controls;

import java.text.ParseException;
import java.util.TimeZone;

import com.tle.web.sections.result.util.KeyLabel;
import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.Calendar;
import com.dytech.edge.wizard.beans.control.Calendar.DateFormat;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.LangUtils;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.freetext.queries.DateRangeQuery;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides the model for a calendar control.
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class CCalendar extends EditableCtrl
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(CCalendar.class);

	private final DateFormat format;
	private final boolean range;
	@Nullable
	protected TleDate dateFrom;
	@Nullable
	protected TleDate dateTo;

	public CCalendar(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		final Calendar calendar = (Calendar) controlBean;
		range = calendar.isRange() || isExpertSearch();
		final DateFormat calFormat = calendar.getFormat();
		format = calFormat == null ? DateFormat.DMY : calFormat;
	}

	@Override
	public void setValues(@Nullable String... vals)
	{
		throw new Error("Should use setDates()");
	}

	@Override
	public void resetToDefaults()
	{
		final TleDate[] vals = new TleDate[2];
		vals[0] = parseDate(controlBean.getItemValue(0));
		vals[1] = parseDate(controlBean.getItemValue(1));
		setDates(vals);
	}

	/**
	 * @param dates The date(s) for the range must represent midnight *our* time
	 *            zone.
	 */
	public void setDates(TleDate[] dates)
	{
		dateFrom = dates[0];
		dateTo = dates[1];
		clearValues();

		if( dateFrom != null )
		{
			addValue(dateFrom.isConceptual() ? dateFrom.format(Dates.ISO_DATE_ONLY) : new UtcDate(dateFrom.toDate())
				.format(Dates.ISO));
		}
		else
		{
			addValue("");
		}
		if( range )
		{
			if( dateTo != null )
			{
				addValue(dateTo.isConceptual() ? dateTo.format(Dates.ISO_DATE_ONLY) : new UtcDate(dateTo.toDate())
					.format(Dates.ISO));
			}
			else
			{
				addValue("");
			}
		}

		if( dateTo != null && dateFrom != null && dateFrom.compareTo(dateTo) > 0 )
		{
			setInvalid(true, new KeyLabel("wizard.controls.calendar.after"));
		}
	}

	@Override
	public boolean isEmpty()
	{
		return (dateFrom == null || (isRange() && dateTo == null));
	}

	@Override
	public void loadFromDocument(PropBagEx docPropBag)
	{
		super.loadFromDocument(docPropBag);
		try
		{
			final String strFrom = getValueWithIndex(0);
			if( strFrom != null )
			{
				if( strFrom.length() > 0 )
				{
					dateFrom = parseDate(strFrom);
				}
				else
				{
					dateFrom = null;
				}
			}
			if( range )
			{
				final String strTo = getValueWithIndex(1);
				if( strTo != null )
				{
					if( strTo.length() > 0 )
					{
						dateTo = parseDate(strTo);
					}
					else
					{
						dateTo = null;
					}
				}
			}
		}
		catch( Exception e )
		{
			setInvalid(true, new KeyLabel("wizard.controls.calendar.parsingerror"));
		}
	}

	@Nullable
	protected TleDate parseDate(@Nullable String date)
	{
		TleDate rdate = null;
		if( date == null || date.trim().equals("") )
		{
			return null;
		}
		final TimeZone tz = CurrentTimeZone.get();
		try
		{
			// Zero is used as today's default date
			int value = Integer.parseInt(date);
			java.util.Calendar cal = java.util.Calendar.getInstance(tz);
			cal.add(java.util.Calendar.DAY_OF_YEAR, value);
			// Make it midnight *our* time
			cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
			cal.set(java.util.Calendar.MINUTE, 0);
			cal.set(java.util.Calendar.SECOND, 0);
			cal.set(java.util.Calendar.MILLISECOND, 0);
			rdate = new LocalDate(cal.getTime(), tz);
		}
		catch( Exception e )
		{
			// Probably a date then
		}

		if( rdate == null )
		{
			try
			{
				rdate = new LocalDate(date, Dates.ISO_WITH_TIMEZONE, tz);
			}
			catch( ParseException e1 )
			{
				// Timeless dates are midnight in *our* timezone.
				try
				{
					rdate = new LocalDate(date, Dates.ISO_MIDNIGHT, tz);
				}
				catch( ParseException e2 )
				{
					// Use UTC so that the day doesn't change. Date-only is
					// only for Wizards
					// This is a 'conceptual' date.
					try
					{
						rdate = UtcDate.conceptualDate(date);
					}
					catch( ParseException e3 )
					{
						LOGGER.warn("Unparseable date " + date);
					}
				}
			}
		}
		return rdate;
	}

	@Nullable
	@Override
	public BaseQuery getPowerSearchQuery()
	{
		TleDate start = getDateFrom();
		TleDate end = getDateTo();

		if( start == null && end == null )
		{
			return null;
		}

		return new DateRangeQuery(getFirstTarget().getFreetextField(), start, end, CurrentLocale.get(
			controlBean.getPowerSearchFriendlyName(), null));
	}

	public boolean isRange()
	{
		return range;
	}

	public DateFormat getDateFormat()
	{
		return format;
	}

	@Nullable
	public TleDate getDateFrom()
	{
		return dateFrom;
	}

	@Nullable
	public TleDate getDateTo()
	{
		return dateTo;
	}
}
