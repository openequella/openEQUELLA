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
