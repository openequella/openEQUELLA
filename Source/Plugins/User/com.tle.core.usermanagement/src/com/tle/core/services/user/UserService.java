package com.tle.core.services.user;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.core.remoting.RemoteUserService;
import com.tle.web.dispatcher.FilterResult;

/**
 * The <code>authenticate*</code> methods do not will only do authentication and
 * setup a UserState object; this must be passed back into the
 * <code>login(UserState)</code> method if you actually want to login a user.
 * Alternatively, you can simply call any of the other <code>login*</code>
 * methods which do both of these steps in one go.
 * 
 * @author Nicholas Read
 */
public interface UserService extends RemoteUserService
{
	UserState login(String username, String password, WebAuthenticationDetails details, boolean forceSession);

	UserState loginWithToken(String token, WebAuthenticationDetails details, boolean forceSession);

	UserState loginAsUser(String username, WebAuthenticationDetails details, boolean forceSession);

	UserState authenticate(String username, String password, WebAuthenticationDetails details);

	UserState authenticateWithToken(String token, WebAuthenticationDetails details);

	UserState authenticateAsUser(String username, WebAuthenticationDetails details);

	UserState authenticateAsGuest(WebAuthenticationDetails details);

	UserState authenticateRequest(HttpServletRequest request);

	boolean verifyUserStateForToken(UserState userState, String token);

	void useUser(UserState userState);

	void login(UserState userState, boolean forceSession);

	void logoutToGuest(WebAuthenticationDetails details, boolean forceSession);

	boolean isWrapperEnabled(String settingsConfig);

	String getGeneratedToken(String secretId, String username);

	WebAuthenticationDetails getWebAuthenticationDetails(HttpServletRequest request);

	FilterResult runLogonFilters(HttpServletRequest request, HttpServletResponse response) throws IOException;

	Map<String, String[]> getAdditionalLogonState(HttpServletRequest request);

	URI logoutURI(UserState userState, URI loggedoutUri);

	URI logoutRedirect(URI loggedoutUri);

	<T> T getAttribute(Object key);

	void clearUserSearchCache();

	// Only for autologin settings
	void refreshSettings();

	/**
	 * This really is just for debugging.
	 */
	String convertUserStateToString(UserState us);
}