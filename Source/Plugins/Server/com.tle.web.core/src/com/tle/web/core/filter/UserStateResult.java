package com.tle.web.core.filter;

import com.tle.core.user.UserState;

public class UserStateResult
{
	//@formatter:off
	public enum Result
	{
		LOGIN_SESSION, 
		LOGIN_NO_SESSION_CREATION, 
		EXISTING, 
		GUEST,
		GUEST_NO_SESSION_CREATION
	}
	//@formatter:on

	private final UserState userState;
	private final Result result;

	public UserStateResult(UserState userState, Result result)
	{
		this.userState = userState;
		this.result = result;
	}

	public UserStateResult(Result result)
	{
		this(null, result);
	}

	public UserStateResult(UserState userState)
	{
		this(userState, Result.LOGIN_SESSION);
	}

	public UserState getUserState()
	{
		return userState;
	}

	public Result getResult()
	{
		return result;
	}
}
