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

import com.dytech.edge.wizard.beans.control.Calendar.DateFormat;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.util.TleDate;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CCalendar;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.standard.controls.Calendar.CalendarWebControlModel;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class Calendar extends AbstractWebControl<CalendarWebControlModel>
{
	@ViewFactory
	private FreemarkerFactory factory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(stateful = false)
	private com.tle.web.sections.standard.Calendar date1;
	@Component(stateful = false)
	private com.tle.web.sections.standard.Calendar date2;
	@Component
	private Link clearLink;

	private CCalendar cal;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		cal = (CCalendar) control;
		super.setWrappedControl(control);
	}

	public boolean isRange()
	{
		return cal.isRange();
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		UpdateDomFunction updateFunction = ajax.getAjaxUpdateDomFunction(getTree(), this,
			events.getEventHandler("showClear"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id + "_clear");

		date1.setConceptual(true);
		date1.setEventHandler(JSHandler.EVENT_CHANGE, new OverrideHandler(updateFunction));
		date1.setConceptual(true);

		if( isRange() )
		{
			date2.setConceptual(true);
			date2.setEventHandler(JSHandler.EVENT_CHANGE, new OverrideHandler(updateFunction));
			date2.setConceptual(true);
		}

		clearLink.setClickHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(getTree(), this,
			events.getEventHandler("clear"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id
				+ "_calendar-control")));
		clearLink.setDisablable(true);
	}

	@EventHandlerMethod
	public void clear(SectionInfo info)
	{
		date1.clearDate(info);
		if( isRange() )
		{
			date2.clearDate(info);
		}
		this.getModel(info).setShowClearLink(false);
	}

	@EventHandlerMethod
	public void showClear(SectionInfo info)
	{
		checkForDates(info);
	}

	private void checkForDates(SectionInfo info)
	{
		CalendarWebControlModel model = getModel(info);
		if( date1.isDateSet(info) || date2.isDateSet(info) )
		{
			model.setShowClearLink(true);
		}
	}

	@Override
	public Class<CalendarWebControlModel> getModelClass()
	{
		return Calendar.CalendarWebControlModel.class;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		setupFormDate(context, date1, cal.getDateFrom());
		addDisabler(context, date1);

		DateFormat df = cal.getDateFormat();
		date1.getState(context).setPickerType(df.name());

		if( isRange() )
		{
			addDisabler(context, date2);
			setupFormDate(context, date2, cal.getDateTo());
			date2.getState(context).setPickerType(df.name());

		}

		checkForDates(context);
		if( getModel(context).isShowClearLink() )
		{
			addDisabler(context, clearLink);
		}

		return factory.createResult("calendar/calendar.ftl", context);
	}

	private void setupFormDate(SectionInfo info, com.tle.web.sections.standard.Calendar formDate,
		@Nullable TleDate value)
	{
		if( value != null )
		{
			formDate.setDate(info, value);
		}
		else
		{
			formDate.clearDate(info);
		}
		if( cal.isReload() )
		{
			formDate.setEventHandler(info, JSHandler.EVENT_CHANGE, new StatementHandler(getReloadFunction(true)));
			clearLink.setClickHandler(info, getReloadFunction(true, events.getEventHandler("clear")));
		}
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		cal.setDates(new TleDate[]{date1.getDate(info), date2.getDate(info)});
	}

	public com.tle.web.sections.standard.Calendar getDate1()
	{
		return date1;
	}

	public com.tle.web.sections.standard.Calendar getDate2()
	{
		return date2;
	}

	public static class CalendarWebControlModel extends WebControlModel
	{
		private boolean showClearLink;

		public boolean isShowClearLink()
		{
			return showClearLink;
		}

		public void setShowClearLink(boolean showClearLink)
		{
			this.showClearLink = showClearLink;
		}
	}

	public Link getClearLink()
	{
		return clearLink;
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return date1;
	}
}
