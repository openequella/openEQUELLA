package com.dytech.devlib;

@SuppressWarnings("nls")
public final class Code
{
	public static String SQL(String szStr)
	{
		return szStr.replaceAll("'", "''");
	}

	private Code()
	{
		throw new Error();
	}
}
