package com.tle.core.equella.runner;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.logging.LogFactory;

/**
 * Writes Tomcat JDK log messages to log4j log.
 */
@SuppressWarnings("nls")
public class TomcatLogRedirector
{
	static JDKLogHandler activeHandler;

	/**
	 * Activates this feature.
	 */
	public static void activate()
	{
		try
		{
			Logger rootLogger = LogManager.getLogManager().getLogger("");
			// remove old ConsoleHandler
			for( Handler handler : rootLogger.getHandlers() )
			{
				rootLogger.removeHandler(handler);
			}

			activeHandler = new JDKLogHandler();
			activeHandler.setLevel(Level.ALL);
			rootLogger.addHandler(activeHandler);
			rootLogger.setLevel(Level.ALL);
		}
		catch( Exception exc )
		{
			LogFactory.getLog(TomcatLogRedirector.class).error("activation failed", exc);
		}
	}

	public static void deactivate()
	{
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.removeHandler(activeHandler);
		Logger.getLogger(TomcatLogRedirector.class.getName()).info("dactivated");
	}

	protected static class JDKLogHandler extends Handler
	{
		@Override
		public void publish(LogRecord record)
		{
			String message = record.getMessage();
			Throwable exception = record.getThrown();
			Level level = record.getLevel();

			org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger("TomcatLog");

			// if tomcat log is not configured
			if( log4j == null )
			{
				return;
			}

			if( level == Level.SEVERE )
			{
				log4j.error(message, exception);
			}
			else if( level == Level.WARNING )
			{
				log4j.warn(message, exception);
			}
			else if( level == Level.INFO )
			{
				log4j.info(message, exception);
			}
			else if( level == Level.CONFIG )
			{
				log4j.debug(message, exception);
			}
			else
			{
				log4j.trace(message, exception);
			}
		}

		@Override
		public void flush()
		{
			// nothing to do
		}

		@Override
		public void close()
		{
			// nothing to do
		}
	}
}