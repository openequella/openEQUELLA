package com.tle.core.services;

import com.tle.common.util.Logger;

/*
 * @author aholland
 */
public interface LoggingService
{
	Logger getLogger(Class<?> clazz);

	/**
	 * Removes certain function calls from the printed stack. E.g. from Sun
	 * libraries, method invocations and proxies
	 * 
	 * @param throwable
	 * @return
	 */
	String getFilteredStacktrace(Throwable throwable);

	ErrorInfo getErrorInfo(Throwable throwable);

	class ErrorInfo
	{
		private final boolean silent;
		private final boolean quiet;
		private final boolean warnOnly;
		private final Throwable exception;

		public ErrorInfo(final boolean silent, final boolean quiet, final boolean warnOnly, final Throwable exception)
		{
			this.silent = silent;
			this.quiet = quiet;
			this.warnOnly = warnOnly;
			this.exception = exception;
		}

		public boolean isQuiet()
		{
			return quiet;
		}

		public Throwable getException()
		{
			return exception;
		}

		public boolean isSilent()
		{
			return silent;
		}

		public boolean isWarnOnly()
		{
			return warnOnly;
		}
	}
}
