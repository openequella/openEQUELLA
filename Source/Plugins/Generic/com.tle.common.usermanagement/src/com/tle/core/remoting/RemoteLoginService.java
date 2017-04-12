package com.tle.core.remoting;

/**
 * @author Nicholas Read
 */
public interface RemoteLoginService
{
	void login(String username, String password);

	void loginWithToken(String token);

	String getLoggedInUserId();

	void logout();

	void keepAlive();
}