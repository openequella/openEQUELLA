package com.tle.common.userscripts;

@SuppressWarnings("nls")
public final class UserScriptsConstants
{
	private UserScriptsConstants()
	{
		throw new Error();
	}

	public static enum ScriptTypes
	{
		DISPLAY, EXECUTABLE
	}

	public final static String PRIV_EDIT_SCRIPT = "EDIT_USER_SCRIPTS";
	public final static String PRIV_DELETE_SCRIPT = "DELETE_USER_SCRIPTS";
	public final static String PRIV_CREATE_SCRIPT = "CREATE_USER_SCRIPTS";
}
