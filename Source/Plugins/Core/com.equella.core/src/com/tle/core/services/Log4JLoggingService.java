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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.dytech.edge.exceptions.QuietlyLoggable;
import com.tle.common.util.Logger;
import com.tle.core.guice.Bind;
import com.tle.web.DebugSettings;

/**
 * @author aholland
 */
@Bind(LoggingService.class)
@Singleton
public class Log4JLoggingService implements LoggingService
{
	private static final Map<Class<?>, Logger> LOGGERS = new HashMap<Class<?>, Logger>();

	protected final List<String> stackFilter = new ArrayList<String>();

	public Log4JLoggingService()
	{
		stackFilter.add("sun.reflect."); //$NON-NLS-1$
		stackFilter.add("java.lang.reflect."); //$NON-NLS-1$
		stackFilter.add("org.apache.tomcat."); //$NON-NLS-1$
		stackFilter.add("org.apache.catalina."); //$NON-NLS-1$
		stackFilter.add("org.apache.coyote."); //$NON-NLS-1$
		stackFilter.add("org.springframework.aop."); //$NON-NLS-1$
		stackFilter.add("$Proxy"); //$NON-NLS-1$
		stackFilter.add("com.tle.web.helpers.spring.MultiDelegatingFilterProxy"); //$NON-NLS-1$
		stackFilter.add("org.springframework.web.filter.OncePerRequestFilter"); //$NON-NLS-1$
	}

	@Override
	public Logger getLogger(Class<?> clazz)
	{
		if( clazz == null )
		{
			return getLogger(LoggingService.class);
		}

		Logger logger = LOGGERS.get(clazz);
		if( logger == null )
		{
			logger = new Log4JLoggingService.Log4JLogger(org.apache.log4j.Logger.getLogger(clazz), this,
				DebugSettings.isDebuggingMode());
			LOGGERS.put(clazz, logger);
		}
		return logger;
	}

	@Override
	public String getFilteredStacktrace(Throwable ex)
	{
		if( ex == null )
		{
			return null;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw)
		{
			private static final String LINE_START = "\tat "; //$NON-NLS-1$

			@Override
			public void println(String x)
			{
				if( x.startsWith(LINE_START) )
				{
					String t = x.substring(LINE_START.length());
					for( String filter : stackFilter )
					{
						if( t.startsWith(filter) )
						{
							return;
						}
					}
				}
				super.println(x);
			}
		};

		ex.printStackTrace(pw);
		pw.close();

		return sw.toString();
	}

	/**
	 * Get the 'most root' QuietlyLoggable error (if any)
	 * 
	 * @param throwable
	 * @return
	 */
	private QuietlyLoggable getQuietlyLoggable(final Throwable throwable)
	{
		Throwable t = throwable;
		QuietlyLoggable ql = null;
		do
		{
			if( t instanceof QuietlyLoggable )
			{
				ql = (QuietlyLoggable) t;
			}
			t = t.getCause();
		}
		while( t != null );

		return ql;
	}

	/**
	 * @param throwable
	 * @return
	 */
	@Override
	public ErrorInfo getErrorInfo(Throwable throwable)
	{
		boolean silent = false;
		boolean quiet = false;
		boolean warn = false;

		QuietlyLoggable ql = getQuietlyLoggable(throwable);
		if( ql != null )
		{
			silent = ql.isSilent();
			quiet = !ql.isShowStackTrace();
			warn = ql.isWarnOnly();
		}

		return new ErrorInfo(silent, quiet, warn, (ql != null ? (Throwable) ql : throwable));
	}

	public static class Log4JLogger implements Logger
	{
		private final org.apache.log4j.Logger l;
		private final Log4JLoggingService service;
		private final boolean enforcedDebug;

		public Log4JLogger(org.apache.log4j.Logger l, Log4JLoggingService service, boolean enforcedDebug)
		{
			this.l = l;
			this.service = service;
			this.enforcedDebug = enforcedDebug;
		}

		@Override
		public boolean isDebugEnabled()
		{
			return l.isDebugEnabled();
		}

		@Override
		public boolean isTraceEnabled()
		{
			return l.isTraceEnabled();
		}

		@Override
		public void trace(String msg)
		{
			l.trace(msg);
		}

		@Override
		public void error(String msg)
		{
			l.error(msg);
		}

		@Override
		public void error(String msg, Throwable throwable)
		{
			ErrorInfo err = service.getErrorInfo(throwable);

			if( !err.isSilent() && !err.isQuiet() )
			{
				if( err.isWarnOnly() )
				{
					l.warn(msg, err.getException());
				}
				else
				{
					l.error(msg, err.getException());
				}
			}
			else if( err.isQuiet() )
			{
				if( err.isWarnOnly() )
				{
					l.warn(err.getException().getMessage());
				}
				else
				{
					l.error(err.getException().getMessage());
				}
			}
		}

		@Override
		public void error(Throwable throwable)
		{
			error("Error", throwable); //$NON-NLS-1$
		}

		@Override
		public void info(String msg)
		{
			l.info(msg);
		}

		@Override
		public void info(String msg, Throwable throwable)
		{
			ErrorInfo err = service.getErrorInfo(throwable);

			if( !err.isSilent() && !err.isQuiet() )
			{
				l.info(msg, err.getException());
			}
			else if( err.isQuiet() )
			{
				l.info(err.getException().getMessage());
			}
		}

		@Override
		public void debug(String msg)
		{
			if( enforcedDebug && !l.isDebugEnabled() )
			{
				l.info(msg);
			}
			else
			{
				l.debug(msg);
			}
		}

		@Override
		public void debug(String msg, Throwable throwable)
		{
			ErrorInfo err = service.getErrorInfo(throwable);

			if( !err.isSilent() && !err.isQuiet() )
			{
				if( enforcedDebug && !l.isDebugEnabled() )
				{
					l.info(msg, err.getException());
				}
				else
				{
					l.debug(msg, err.getException());
				}
			}
			else if( err.isQuiet() )
			{
				if( enforcedDebug && !l.isDebugEnabled() )
				{
					l.info(err.getException().getMessage());
				}
				else
				{
					l.debug(err.getException().getMessage());
				}
			}
		}

		@Override
		public void warn(String msg)
		{
			l.warn(msg);
		}

		@Override
		public void warn(String msg, Throwable throwable)
		{
			ErrorInfo err = service.getErrorInfo(throwable);

			if( !err.isSilent() && !err.isQuiet() )
			{
				l.warn(msg, err.getException());
			}
			else if( err.isQuiet() )
			{
				l.warn(err.getException().getMessage());
			}
		}
	}
}
