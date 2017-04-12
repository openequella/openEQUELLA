package com.tle.exceptions;

public class UsernameNotFoundException extends AuthenticationException
{
	private final String username;

	public UsernameNotFoundException(String username)
	{
		super(username);
		this.username = username;
	}

	public String getUsername()
	{
		return username;
	}
}
