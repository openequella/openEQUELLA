/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard;

import java.util.Date;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.standard.model.HtmlCalendarState;

@NonNullByDefault
public class Calendar extends AbstractDisablerComponent<HtmlCalendarState>
	implements
		ParametersEventListener,
		BookmarkEventListener
{
	private boolean conceptual;

	public Calendar()
	{
		super(RendererConstants.CALENDAR);
	}

	@Override
	public Class<HtmlCalendarState> getModelClass()
	{
		return HtmlCalendarState.class;
	}

	/**
	 * @param info
	 * @return
	 */
	@Nullable
	public TleDate getDate(SectionInfo info)
	{
		HtmlCalendarState state = getState(info);
		return state.getDate();
	}

	public boolean isDateSet(SectionInfo info)
	{
		return getState(info).getDate() != null;
	}

	public void clearDate(SectionInfo info)
	{
		getState(info).setDate(null);
	}

	/**
	 * @param info
	 * @param date
	 */
	public void setDate(SectionInfo info, @Nullable TleDate date)
	{
		getState(info).setDate(date);
	}

	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event)
	{
		HtmlCalendarState state = getState(info);
		Date date = event.getDateParameter(getParameterId(), false);
		if( date != null )
		{
			state.setDate(new UtcDate(date).conceptualDate());
		}
	}

	@Override
	public void bookmark(SectionInfo info, BookmarkEvent event)
	{
		if( addToThisBookmark(info, event) )
		{
			final HtmlCalendarState state = getState(info);
			final TleDate date = state.getDate();
			if( date != null )
			{
				final String val;
				if( date.isConceptual() )
				{
					val = Long.toString(date.toLong());
				}
				else
				{
					// Adjust to UTC midnight (see CalendarRender writeMiddle)
					val = Long.toString(UtcDate.convertLocalMidnightToUtcMidnight(date, date.getTimeZone()).toLong());
				}
				event.setParam(getParameterId(), val);
			}
		}
	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		addDocumentedParam(event, getParameterId(), Long.class.getName());
	}

	public boolean isConceptual()
	{
		return conceptual;
	}

	public void setConceptual(boolean conceptual)
	{
		ensureBuildingTree();
		this.conceptual = conceptual;
	}
}
