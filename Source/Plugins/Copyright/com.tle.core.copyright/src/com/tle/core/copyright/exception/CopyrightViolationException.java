/*
 * Created on Dec 7, 2004
 */
package com.tle.core.copyright.exception;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class CopyrightViolationException extends WorkflowException
{
	private static final long serialVersionUID = 1L;
	private LanguageBundle i18NMessage;
	private boolean calBookPercentageException;

	public CopyrightViolationException(LanguageBundle message)
	{
		super(CurrentLocale.get(message));
		this.i18NMessage = message;
	}

	public LanguageBundle getI18NMessage()
	{
		return i18NMessage;
	}

	@Override
	public String getLocalizedMessage()
	{
		return CurrentLocale.get(i18NMessage);
	}

	public boolean isCALBookPercentageException()
	{
		return calBookPercentageException;
	}

	public void setCALBookPercentageException(boolean percentageException)
	{
		this.calBookPercentageException = percentageException;
	}
}
