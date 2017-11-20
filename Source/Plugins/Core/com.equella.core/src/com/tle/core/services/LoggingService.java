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
