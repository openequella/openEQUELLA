package com.tle.web.scripting.impl;

import com.tle.common.scripting.objects.LoggingScriptObject;
import com.tle.common.util.Logger;

/**
 * @author aholland
 */
public class LoggingScriptWrapper extends AbstractScriptWrapper implements LoggingScriptObject
{
	private static final long serialVersionUID = 1L;

	private final Logger logger;

	public LoggingScriptWrapper(Logger logger)
	{
		this.logger = logger;
	}

	@Override
	public void log(String text)
	{
		logger.info(text);
	}
}
