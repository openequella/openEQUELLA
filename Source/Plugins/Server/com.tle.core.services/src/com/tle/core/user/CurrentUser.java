package com.tle.core.user;

import java.util.Set;

import com.dytech.edge.common.valuebean.UserBean;

/**
 * @author Nicholas Read
 */
public final class CurrentUser
{
	private static ThreadLocal<UserState> stateLocal = new ThreadLocal<UserState>();

	private CurrentUser()
	{
		throw new Error();
	}

	public static UserState getUserState()
	{
		return stateLocal.get();
	}

	public static void setUserState(UserState state)
	{
		stateLocal.set(state);
	}

	public static String getSessionID()
	{
		if( getUserState() == null )
		{
			return null;
		}
		return getUserState().getSessionID();
	}

	public static Set<String> getUsersGroups()
	{
		return getUserState().getUsersGroups();
	}

	public static UserBean getDetails()
	{
		return getUserState().getUserBean();
	}

	public static String getUserID()
	{
		return getUserState().getUserBean().getUniqueID();
	}

	public static String getUsername()
	{
		return getUserState().getUserBean().getUsername();
	}

	public static boolean isGuest()
	{
		return getUserState().isGuest();
	}

	public static boolean wasAutoLoggedIn()
	{
		return getUserState().wasAutoLoggedIn();
	}
}
