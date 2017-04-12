package com.tle.web;

@SuppressWarnings("nls")
public final class DebugSettings
{
	private static boolean autoTestMode;
	private static boolean debuggingMode;
	private static boolean debugAaron;

	static
	{
		autoTestMode = (System.getProperty("equella.autotest") != null);
		debuggingMode = (System.getProperty("equella.debug") != null);
		debugAaron = (System.getProperty("equella.debugaaron") != null);
	}

	public static boolean isAutoTestMode()
	{
		return autoTestMode;
	}

	public static boolean isDebuggingMode()
	{
		return debuggingMode || debugAaron;
	}

	public static boolean isDebugAaron()
	{
		return debugAaron;
	}

	private DebugSettings()
	{
		throw new Error();
	}
}
