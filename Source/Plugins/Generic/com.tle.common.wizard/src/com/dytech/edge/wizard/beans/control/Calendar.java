/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

public class Calendar extends WizardControl
{
	public static enum DateFormat
	{
		DMY, MY, Y;
	}

	private static final long serialVersionUID = 1;
	public static final String CLASS = "calendar"; //$NON-NLS-1$

	private boolean range;
	private DateFormat format;


	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public boolean isRange()
	{
		return range;
	}

	public void setRange(boolean range)
	{
		this.range = range;
	}

	public DateFormat getFormat()
	{
		return format;
	}

	public void setFormat(DateFormat format)
	{
		this.format = format;
	}
}
