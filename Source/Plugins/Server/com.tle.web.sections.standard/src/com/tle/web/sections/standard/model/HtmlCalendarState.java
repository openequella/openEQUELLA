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

package com.tle.web.sections.standard.model;

import com.tle.common.util.TleDate;
import com.tle.web.sections.standard.RendererConstants;

/**
 * The State class for calendar {@code Section}s and Renderers.
 * <p>
 * The renderer must return the date in a UTC-millisecond format.
 * 
 * @author jmaginnis
 */
public class HtmlCalendarState extends HtmlComponentState
{
	public enum Format
	{
		SHORT, FULL, ISO, TIME
	}

	private TleDate date;
	private Format displayFormat;
	private String pickerType;

	public String getPickerType()
	{
		return pickerType;
	}

	public void setPickerType(String pickerType)
	{
		this.pickerType = pickerType;
	}

	public HtmlCalendarState()
	{
		super(RendererConstants.CALENDAR);
	}

	public Format getDisplayFormat()
	{
		return displayFormat;
	}

	public void setDisplayFormat(Format displayFormat)
	{
		this.displayFormat = displayFormat;
	}

	public TleDate getDate()
	{
		return date;
	}

	public void setDate(TleDate date)
	{
		this.date = date;
	}

}
