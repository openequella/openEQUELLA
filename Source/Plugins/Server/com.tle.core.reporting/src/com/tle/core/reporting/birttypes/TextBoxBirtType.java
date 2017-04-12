package com.tle.core.reporting.birttypes;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.ReportParameterConverter;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.Calendar;
import com.dytech.edge.wizard.beans.control.EditBox;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.ibm.icu.util.ULocale;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.LangUtils;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;

public class TextBoxBirtType extends AbstractBirtType
{
	private boolean useCalendar;
	private ReportParameterConverter paramConverter;
	private ReportParameterConverter displayConverter;

	public TextBoxBirtType(IScalarParameterDefn def, int paramNum)
	{
		super(def, paramNum);
		int dataType = def.getDataType();
		useCalendar = dataType == IParameterDefn.TYPE_DATE_TIME || dataType == IParameterDefn.TYPE_DATE;
		displayConverter = getConverter(scalarDef.getDisplayFormat());
		paramConverter = getConverter(getParameterFormat());
	}

	@Override
	protected WizardControl createControl(IGetParameterDefinitionTask paramTask)
	{
		WizardControl control;
		Object defValue = paramTask.getDefaultValue(scalarDef);
		String defaultValue = null;
		if( useCalendar )
		{
			control = new Calendar();
			if( defValue != null )
			{
				defaultValue = new LocalDate((Date) defValue, TimeZone.getDefault()).format(Dates.ISO_MIDNIGHT);
			}
		}
		else
		{
			control = new EditBox();
			defaultValue = displayConverter.format(defValue);
		}
		List<WizardControlItem> items = control.getItems();
		if( defaultValue != null )
		{
			items.add(new WizardControlItem(LangUtils.createTextTempLangugageBundle(defaultValue), defaultValue));
		}
		return control;
	}

	@Override
	public void convertToParams(PropBagEx docXml, Map<String, String[]> params)
	{
		if( docXml.nodeExists(targetNode) )
		{
			String xmlValue = docXml.getNode(targetNode);

			Object parsed = null;
			if( useCalendar )
			{
				try
				{
					parsed = new LocalDate(xmlValue, Dates.ISO_DATE_ONLY, CurrentTimeZone.get()).toDate();
				}
				catch( ParseException e )
				{
					// nothing
				}
			}
			else
			{
				parsed = displayConverter.parse(xmlValue, scalarDef.getDataType());
			}
			if( parsed != null )
			{
				params.put(scalarDef.getName(), new String[]{paramConverter.format(parsed)});
			}
			else
			{
				params.remove(scalarDef.getName());
			}
		}
	}

	private ReportParameterConverter getConverter(String format)
	{
		com.ibm.icu.util.TimeZone icuTimeZone = com.ibm.icu.util.TimeZone.getTimeZone(CurrentTimeZone.get().getID());
		return new ReportParameterConverter(format, ULocale.forLocale(CurrentLocale.getLocale()), icuTimeZone);
	}

	@Override
	public void convertToXml(PropBagEx docXml, Map<String, String[]> params)
	{
		String firstValue = getFirstValue(params);
		if( firstValue != null )
		{
			String xmlValue;
			Object parsed = paramConverter.parse(firstValue, scalarDef.getDataType());
			if( parsed != null )
			{
				if( useCalendar )
				{
					xmlValue = new LocalDate((Date) parsed, TimeZone.getDefault()).format(Dates.ISO_DATE_ONLY);
				}
				else
				{
					xmlValue = displayConverter.format(parsed);
				}
				if( xmlValue != null )
				{
					docXml.setNode(targetNode, xmlValue);
				}
			}
		}

	}
}