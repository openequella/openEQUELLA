package com.tle.web.sections.standard.renderers.calendar;

import java.io.IOException;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryDatepicker;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlCalendarState;
import com.tle.web.sections.standard.renderers.AbstractInputRenderer;

@SuppressWarnings("nls")
@NonNullByDefault
public class CalendarRenderer extends AbstractInputRenderer implements JSDisableable
{
	private static final String VISIBLE_POSTFIX = "vis";

	private final HtmlCalendarState state;
	private final AppendedElementId visibleElement;
	@Nullable
	private ElementId notBefore;
	@Nullable
	private ElementId notAfter;
	@Nullable
	private SimpleFunction disableFunc;

	public CalendarRenderer(HtmlCalendarState state)
	{
		super(state, "text");
		this.state = state;
		visibleElement = new AppendedElementId(this, VISIBLE_POSTFIX);
	}

	@Override
	public ElementId getVisibleElementId()
	{
		return visibleElement;
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		attrs.put("name", null);
		attrs.put("readonly", "readonly");
	}

	public void setNotAfter(Object notAfter)
	{
		if( notAfter instanceof ElementId )
		{
			this.notAfter = new AppendedElementId((ElementId) notAfter, VISIBLE_POSTFIX);
		}
	}

	public void setNotBefore(Object notBefore)
	{
		if( notBefore instanceof ElementId )
		{
			this.notBefore = new AppendedElementId((ElementId) notBefore, VISIBLE_POSTFIX);
		}
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !event.equals(JSHandler.EVENT_CHANGE) )
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	@Nullable
	private Long toUtcMidnight(@Nullable TleDate date)
	{
		if( date != null )
		{
			if( date.isConceptual() )
			{
				return date.toLong();
			}
			else
			{
				return UtcDate.convertLocalMidnightToUtcMidnight(date, date.getTimeZone()).toLong();
			}
		}
		return null;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		TleDate date = state.getDate();
		// sending dates as anything other than UTC midnight to the picker
		// doesn't make sense.
		// String val = date != null ? Long.toString(new
		// UtcDate(date.toLong()).toMidnight().toLong()) : "";
		Long longDate = toUtcMidnight(date);
		String val = (longDate != null ? Long.toString(longDate) : "");

		String name = getName(writer);
		writer.render(new HiddenInput(this, name, val));
		super.writeMiddle(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		TleDate date = state.getDate();
		info.preRender(JQueryDatepicker.getLangPackInclude(CurrentLocale.getLocale()));

		ElementId otherDate = notBefore != null ? notBefore : notAfter;
		boolean primary = notBefore != null ? false : true;

		JSExpression changeExpr = null;
		JSHandler handler = state.getHandler(JSHandler.EVENT_CHANGE);
		if( handler != null )
		{
			changeExpr = new AnonymousFunction(handler);
		}
		// sending dates as anything other than UTC midnight to the picker
		// doesn't make sense.
		Long time = toUtcMidnight(date);
		// TimeZone tz = CurrentTimeZone.get();
		final Integer tzOff = null; // (date == null || date.isConceptual() ?
									// null
									// : tz.getOffset(date.toLong()));
		final JQuerySelector otherDateSelector = (otherDate == null ? null : Jq.$(otherDate));

		final String pickerFormat = state.getPickerType();

		info.addReadyStatements(JQueryDatepicker.setupPicker(Jq.$(getVisibleElementId()), Jq.$(this), tzOff,
			changeExpr, otherDateSelector, primary, pickerFormat, time,
			CurrentLocale.get("com.tle.web.sections.standard.renderers.calendar.button")));

		if( isDisabled() )
		{
			info.addReadyStatements(Js.call_s(createDisableFunction(), true));
		}
	}

	@Override
	public JSCallable createDisableFunction()
	{
		if( disableFunc == null )
		{
			return new PrependedParameterFunction(JQueryDatepicker.DISABLE_PICKER_FUNCTION, Jq.$(getVisibleElementId()));
		}
		return disableFunc;
	}
}
