/*
 * Created on Dec 7, 2004
 */
package com.tle.common.quota.exception;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.FileSizeUtils;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class QuotaExceededException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public QuotaExceededException(long current, long max)
	{
		super(CurrentLocale.get("exception.quotaexceeded", //$NON-NLS-1$
			FileSizeUtils.humanReadableFileSize(max), FileSizeUtils.humanReadableFileSize(current)));
	}
}
