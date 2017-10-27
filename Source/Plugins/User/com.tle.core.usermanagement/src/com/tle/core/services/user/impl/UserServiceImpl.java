/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.services.user.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.beans.Institution;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Check;
import com.tle.common.Triple;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.common.usermanagement.user.AnonymousUserState;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.events.UMPChangedEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.UserSessionLoginEvent;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.listeners.GroupChangedListener;
import com.tle.core.events.listeners.UMPChangedListener;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.guice.Bind;
import com.tle.core.institution.events.InstitutionEvent;
import com.tle.core.institution.events.listeners.InstitutionListener;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.events.services.EventService;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AuthenticationException;
import com.tle.exceptions.BadCredentialsException;
import com.tle.exceptions.TokenException;
import com.tle.exceptions.UsernameNotFoundException;
import com.tle.plugins.ump.UserDirectory;
import com.tle.plugins.ump.UserDirectoryChain;
import com.tle.plugins.ump.UserDirectoryChainImpl;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;

@SuppressWarnings("nls")
@Bind(UserService.class)
@Singleton
public class UserServiceImpl
	implements
		UserService,
		InstitutionListener,
		UMPChangedListener,
		UserChangeListener,
		GroupChangedListener,
		UserSessionLogoutListener
{
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class);

	/**
	 * Retrieves the last IP address in a possibly comma-separated list of them.
	 */
	private static final Pattern FORWARD_FOR_PATTERN = Pattern.compile("^(?:.*,)?\\s*([\\.\\d]+)\\s*$");

	// cache of wrapper chains
	private static final Map<Institution, InstitutionState> WRAPPER_CHAINS = Maps.newHashMap();

	@Inject
	private ConfigurationService configurationService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private AuditLogService auditLogService;
	@Inject
	private EventService eventService;
	@Inject
	private UserSessionService userSessionService;

	@Inject
	private PluginTracker<UserDirectory> umpTracker;
	@Inject
	private PluginTracker<UserManagementLogonFilter> logonFilterTracker;

	@Inject(optional = true)
	@Named("userService.useXForwardedFor")
	private boolean useXForwardedFor;

	@Override
	public UserState login(String username, String password, WebAuthenticationDetails details, boolean forceSession)
	{
		UserState us = authenticate(username, password, details);
		login(us, forceSession);
		return us;
	}

	@Override
	public UserState authenticate(String username, String password, WebAuthenticationDetails details)
	{
		ModifiableUserState auth = getCurrentPlugin().authenticateUser(username, password);
		if( auth == null )
		{
			throw new BadCredentialsException("Username or password incorrect");
		}
		return setupUserState(auth, details, true);
	}

	@Override
	public UserState loginWithToken(String token, WebAuthenticationDetails details, boolean forceSession)
	{
		UserState us = CurrentUser.getUserState();
		if( us == null || us.isGuest() || !verifyUserStateForToken(us, token) )
		{
			us = authenticateWithToken(token, details);
			if( us != null )
			{
				login(us, forceSession);
			}
		}
		return us;
	}

	@Override
	public UserState authenticateWithToken(final String token, WebAuthenticationDetails details)
	{
		ModifiableUserState auth = getCurrentPlugin().authenticateToken(token);
		if( auth != null )
		{
			return setupUserState(auth, details, true);
		}
		return null;
	}

	@Override
	public UserState loginAsUser(String username, WebAuthenticationDetails details, boolean forceSession)
	{
		UserState us = authenticateAsUser(username, details);
		login(us, forceSession);
		return us;
	}

	@Override
	public UserState authenticateAsUser(String username, WebAuthenticationDetails details)
	{
		ModifiableUserState auth = getCurrentPlugin().authenticateUserFromUsername(username, null);
		if( auth == null )
		{
			throw new UsernameNotFoundException(username);
		}
		return setupUserState(auth, details, true);
	}

	@Override
	public UserState authenticateAsGuest(WebAuthenticationDetails details)
	{
		return setupUserState(new AnonymousUserState(), details, false);
	}

	@Override
	public UserState authenticateRequest(HttpServletRequest request)
	{
		ModifiableUserState auth = getCurrentPlugin().authenticateRequest(request);
		if( auth != null )
		{
			return setupUserState(auth, getWebAuthenticationDetails(request), true);
		}
		return null;
	}

	private Triple<Collection<Long>, Collection<Long>, Collection<Long>> getCachedExpressions(String key)
	{
		return getCurrentState().expressionCache.getIfPresent(key);
	}

	private void addCachedExpressions(String key,
		Triple<Collection<Long>, Collection<Long>, Collection<Long>> expressions)
	{
		getCurrentState().expressionCache.put(key, expressions);
	}

	@Transactional
	protected ModifiableUserState setupUserState(ModifiableUserState auth, WebAuthenticationDetails details,
		boolean authenticated)
	{
		auth.setSessionID(UUID.randomUUID().toString());
		auth.setInstitution(CurrentInstitution.get());
		auth.setAuthenticated(authenticated);
		auth.setIpAddress(details.getIpAddress());
		auth.setHostAddress(details.getHostAddress());
		auth.setHostReferrer(details.getReferrer());

		try
		{
			final UserDirectoryChain currentPlugin = getCurrentPlugin();
			if( auth.isGuest() )
			{
				currentPlugin.initGuestUserState(auth);
			}
			else if( auth.isSystem() )
			{
				currentPlugin.initSystemUserState(auth);
			}
			else
			{
				currentPlugin.initUserState(auth);
			}

			// Verify that the token is valid. This gives wrappers a second
			// chance to reject a user after we know all of their groups and
			// roles.
			final String token = auth.getToken();
			if( !Check.isEmpty(token) )
			{
				if( !currentPlugin.verifyUserStateForToken(auth, token) )
				{
					throw new TokenException(TokenException.STATUS_NOPERMISSION);
				}
			}
		}
		catch( AuthenticationException e )
		{
			// Can be thrown by SharedSecretWrapper
			LOGGER.error("Error initialising user state.", e);
			throw e;
		}
		catch( Exception e )
		{
			LOGGER.warn("Error initialising user state: " + e.getMessage()); //$NON-NLS-1$
			throw Throwables.propagate(e);
		}

		final AutoLogin settings = configurationService.getProperties(new AutoLogin());
		final boolean ipAndRefererEnabled = settings.isEnableIpReferAcl();

		Triple<Collection<Long>, Collection<Long>, Collection<Long>> expressions;
		if( auth.isGuest() )
		{
			// Cache it
			final String key;
			if( !ipAndRefererEnabled )
			{
				key = "guest";
			}
			else
			{
				key = auth.getIpAddress() + auth.getHostReferrer();
			}
			expressions = getCachedExpressions(key);
			if( expressions == null )
			{
				expressions = aclManager.getAclExpressions(auth, ipAndRefererEnabled);
				addCachedExpressions(key, expressions);
			}
		}
		else
		{
			expressions = aclManager.getAclExpressions(auth, ipAndRefererEnabled);
		}

		auth.setAclExpressions(expressions);

		if( LOGGER.isDebugEnabled() && !auth.isGuest() )
		{
			LOGGER.debug("Authenticated " + convertUserStateToString(auth));
		}

		return auth;
	}

	@Override
	public String convertUserStateToString(UserState state)
	{
		Map<String, Object> s = new HashMap<String, Object>();
		s.put("ipaddress", state.getIpAddress());
		s.put("host", state.getHostAddress());
		s.put("referrer", state.getHostReferrer());

		Map<String, Object> a = new HashMap<String, Object>();
		a.put("common", state.getCommonAclExpressions());
		a.put("owner", state.getOwnerAclExpressions());
		a.put("notowner", state.getNotOwnerAclExpressions());

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", state.getUserBean().getUniqueID());
		m.put("username", state.getUserBean().getUsername());
		m.put("groups", state.getUsersGroups());
		m.put("roles", state.getUsersRoles());
		m.put("source", s);
		m.put("matching_acls", a);

		return new GsonBuilder().create().toJson(m);
	}

	@Override
	public UserBean getInformationForUser(String userid)
	{
		return getCurrentPlugin().getInformationForUser(userid);
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(Collection<String> userids)
	{
		return getCurrentPlugin().getInformationForUsers(userids);
	}

	@Override
	public List<GroupBean> getGroupsContainingUser(String userid)
	{
		return getCurrentPlugin().getGroupsContainingUser(userid);
	}

	@Override
	public List<String> getGroupIdsContainingUser(String userid)
	{
		List<GroupBean> groupIdsContainingUser = getGroupsContainingUser(userid);
		List<String> groupids = new ArrayList<String>();
		for( GroupBean bean : groupIdsContainingUser )
		{
			groupids.add(bean.getUniqueID());
		}
		return groupids;
	}

	@Override
	public List<UserBean> getUsersInGroup(String groupId, boolean recursive)
	{
		return getCurrentPlugin().getUsersInGroup(groupId, recursive);
	}

	@Override
	public List<UserBean> searchUsers(String query)
	{
		return getCurrentPlugin().searchUsers(fixQuery(query));
	}

	@Override
	public List<GroupBean> searchGroups(String query, String parentId)
	{
		return getCurrentPlugin().searchGroups(fixQuery(query), parentId);
	}

	@Override
	public List<UserBean> searchUsers(String query, String parentGroupID, boolean recurse)
	{
		return getCurrentPlugin().searchUsers(fixQuery(query), parentGroupID, recurse);
	}

	@Override
	public GroupBean getInformationForGroup(String uuid)
	{
		return getCurrentPlugin().getInformationForGroup(uuid);
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(Collection<String> uuids)
	{
		return getCurrentPlugin().getInformationForGroups(uuids);
	}

	@Override
	public List<GroupBean> searchGroups(String query)
	{
		return getCurrentPlugin().searchGroups(fixQuery(query));
	}

	@Override
	public GroupBean getParentGroupForGroup(String groupID)
	{
		return getCurrentPlugin().getParentGroupForGroup(groupID);
	}

	@Override
	public RoleBean getInformationForRole(String uuid)
	{
		return getCurrentPlugin().getInformationForRole(uuid);
	}

	@Override
	public Map<String, RoleBean> getInformationForRoles(Collection<String> roleIDs)
	{
		return getCurrentPlugin().getInformationForRoles(roleIDs);
	}

	@Override
	public List<RoleBean> searchRoles(String query)
	{
		return getCurrentPlugin().searchRoles(fixQuery(query));
	}

	private String fixQuery(String query)
	{
		query = Strings.nullToEmpty(query);

		if( !query.startsWith("*") )
		{
			query = "*" + query;
		}

		if( !query.endsWith("*") )
		{
			query += "*";
		}

		return query;
	}

	@Override
	public void keepAlive()
	{
		getCurrentPlugin().keepAlive();
	}

	@Override
	public void logoutToGuest(WebAuthenticationDetails details, boolean forceSession)
	{
		login(authenticateAsGuest(details), forceSession);
	}

	@Override
	public WebAuthenticationDetails getWebAuthenticationDetails(HttpServletRequest request)
	{
		Check.checkNotNull(request);

		// The header is spelt incorrectly on purpose.
		// See -> http://en.wikipedia.org/wiki/Referer
		String referrer = request.getHeader("Referer"); //$NON-NLS-1$

		String ipAddress = null;
		// Get any proxy forwarded addresses
		if( useXForwardedFor )
		{
			String forwardedFor = request.getHeader("X-Forwarded-For"); //$NON-NLS-1$
			if( forwardedFor != null )
			{
				Matcher m = FORWARD_FOR_PATTERN.matcher(forwardedFor);
				if( m.matches() && m.groupCount() == 1 )
				{
					ipAddress = m.group(1);
				}
			}
		}

		// Try the remote address
		if( ipAddress == null || ipAddress.length() == 0 )
		{
			ipAddress = request.getRemoteAddr();
		}

		String remoteHost = request.getRemoteHost();
		if( useXForwardedFor )
		{
			remoteHost = ipAddress;
		}

		return new WebAuthenticationDetails(referrer, ipAddress, remoteHost);
	}

	@Override
	public void useUser(UserState userState)
	{
		if( CurrentUser.getUserState() != null )
		{
			throw new Error("Must not be an existing UserState");
		}
		CurrentUser.setUserState(userState);
	}

	@Override
	public void login(UserState userState, boolean forceSession)
	{
		final UserState oldState = CurrentUser.getUserState();
		if( oldState != null )
		{
			eventService.publishApplicationEvent(new UserSessionLogoutEvent(oldState, false));
		}
		CurrentUser.setUserState(userState);
		if( userState.isAuditable() )
		{
			auditLogService.logUserLoggedIn(userState);
		}
		eventService.publishApplicationEvent(new UserSessionLoginEvent(userState));
		if( forceSession )
		{
			userSessionService.forceSession();
		}

	}

	@Override
	public void userSessionDestroyedEvent(UserSessionLogoutEvent event)
	{
		UserState userState = event.getUserState();
		if( userState.isAuditable() )
		{
			auditLogService.logUserLoggedOut(userState);
		}
		getCurrentPlugin().logout(userState);
	}

	public void setupCurrentSource(boolean sendEvent)
	{
		InstitutionState chain = createInstance();

		synchronized( WRAPPER_CHAINS )
		{
			InstitutionState oldState = WRAPPER_CHAINS.put(CurrentInstitution.get(), chain);
			if( oldState != null )
			{
				try
				{
					oldState.chain.close();
				}
				catch( Exception e )
				{
					LOGGER.error("Error closing UMP: " + oldState, e);
				}
			}
		}

		if( sendEvent )
		{
			eventService.publishApplicationEvent(new UMPChangedEvent());
		}
	}

	@Override
	public boolean verifyUserStateForToken(UserState userState, String token)
	{
		return getCurrentPlugin().verifyUserStateForToken(userState, token);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	public UserManagementSettings getPluginConfig(String settingsClass)
	{
		return getPluginConfigInternal(settingsClass);
	}

	private UserManagementSettings getPluginConfigInternal(String settingsClass)
	{
		PluginTracker<?> tracker = umpTracker;
		Extension extension = umpTracker.getExtension(settingsClass);
		if( extension == null )
		{
			tracker = logonFilterTracker;
			for( Extension ext : logonFilterTracker.getExtensions() )
			{
				Parameter param = ext.getParameter("settingsClass");
				if( param != null && param.valueAsString().equals(settingsClass) )
				{
					extension = ext;
					break;
				}
			}
		}
		if( extension != null )
		{
			UserManagementSettings umpSettings = (UserManagementSettings) tracker.getBeanByParameter(extension,
				"settingsClass"); //$NON-NLS-1$
			return configurationService.getProperties(umpSettings);
		}
		return null;
	}

	@Override
	public void refreshSettings()
	{
		setupCurrentSource(true);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	public void setPluginConfig(UserManagementSettings config)
	{
		configurationService.setProperties(config);
		setupCurrentSource(true);
	}

	private InstitutionState getCurrentState()
	{
		synchronized( WRAPPER_CHAINS )
		{
			Institution institution = CurrentInstitution.get();

			InstitutionState chain = WRAPPER_CHAINS.get(institution);
			if( chain == null )
			{
				setupCurrentSource(false);
				chain = WRAPPER_CHAINS.get(institution);
			}
			return chain;
		}
	}

	private UserDirectoryChain getCurrentPlugin()
	{
		return getCurrentState().chain;
	}

	private InstitutionState createInstance()
	{
		final List<UserDirectory> uds = Lists.newArrayList();

		final UserDirectoryChainImpl chain = new UserDirectoryChainImpl();
		chain.setChain(uds);

		final Map<String, Extension> settingsMap = umpTracker.getExtensionMap();
		for( Map.Entry<String, Extension> entry : settingsMap.entrySet() )
		{
			String settingsClass = entry.getKey();
			try
			{
				UserManagementSettings settings = getPluginConfigInternal(settingsClass);
				if( settings.isEnabled() )
				{
					UserDirectory ud = umpTracker.getBeanByParameter(entry.getValue(), "class");
					uds.add(ud);

					if( ud.initialise(chain, settings) )
					{
						// Save updated settings and notify *other* nodes
						configurationService.setProperties(settings);
						eventService.publishApplicationEvent(new UMPChangedEvent());
					}
				}
			}
			catch( Exception e )
			{
				LOGGER.error("Error creating wrapper: " + settingsClass, e);
			}
		}
		Map<Object, Object> chainAttributes = Maps.newHashMap();

		Collection<UserManagementLogonFilter> filters = logonFilterTracker.getNewBeanList();
		Iterator<UserManagementLogonFilter> iter = filters.iterator();
		while( iter.hasNext() )
		{
			UserManagementLogonFilter filter = iter.next();
			if( !filter.init(chainAttributes) )
			{
				iter.remove();
			}
		}
		InstitutionState state = new InstitutionState();
		state.chain = chain;
		state.filters = filters;
		state.attributes = chainAttributes;
		return state;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(Object key)
	{
		return (T) getCurrentState().attributes.get(key);
	}

	@Override
	public List<RoleBean> getRolesForUser(String userid)
	{
		return getCurrentPlugin().getRolesForUser(userid);
	}

	@Override
	public boolean isWrapperEnabled(String settingsClass)
	{
		return getPluginConfigInternal(settingsClass).isEnabled();
	}

	@Override
	public String getGeneratedToken(String secretId, String username)
	{
		return getCurrentPlugin().getGeneratedToken(secretId, username);
	}

	@Override
	public List<String> getTokenSecretIds()
	{
		return getCurrentPlugin().getTokenSecretIds();
	}

	// // EVENTS LISTENERS ////////////////////////////////////////////////////

	@Override
	public void institutionEvent(InstitutionEvent event)
	{
		WRAPPER_CHAINS.keySet().removeAll(event.getChanges().values());
	}

	@Override
	public void umpChangedEvent(UMPChangedEvent event)
	{
		String purgeId = event.getPurgeIdFromCaches();
		if( purgeId == null )
		{
			WRAPPER_CHAINS.remove(CurrentInstitution.get());
		}
		else if( !event.isGroupPurge() )
		{
			getCurrentPlugin().purgeFromCaches(purgeId);
		}
		else
		{
			getCurrentPlugin().purgeGroupFromCaches(purgeId);
		}
	}

	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		purgeFromCaches(event.getUserID(), false);
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		purgeFromCaches(event.getUserID(), false);
	}

	@Override
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		purgeFromCaches(event.getFromUserId(), false);

		aclManager.userIdChanged(event.getFromUserId(), event.getToUserId());
	}

	@Override
	public void groupDeletedEvent(GroupDeletedEvent event)
	{
		purgeFromCaches(event.getGroupID(), true);
	}

	@Override
	public void groupEditedEvent(GroupEditEvent event)
	{
		purgeFromCaches(event.getGroupID(), true);
		for( String member : event.getNewMembers() )
		{
			purgeFromCaches(member, false);
		}
	}

	@Override
	public void groupIdChangedEvent(GroupIdChangedEvent event)
	{
		purgeFromCaches(event.getFromGroupId(), true);

		aclManager.groupIdChanged(event.getFromGroupId(), event.getToGroupId());
	}

	private void purgeFromCaches(String id, boolean groupPurge)
	{
		if( groupPurge )
		{
			getCurrentPlugin().purgeGroupFromCaches(id);
		}
		else
		{
			getCurrentPlugin().purgeFromCaches(id);
		}
		// Tell other nodes to purge it too
		eventService.publishApplicationEvent(new UMPChangedEvent(id, groupPurge));
	}

	@Override
	public FilterResult runLogonFilters(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Collection<UserManagementLogonFilter> filters = getCurrentState().filters;
		for( UserManagementLogonFilter filter : filters )
		{
			FilterResult result = filter.filter(request, response);
			if( result != null )
			{
				return result;
			}
		}
		return FilterResult.FILTER_CONTINUE;
	}

	@Override
	public Map<String, String[]> getAdditionalLogonState(HttpServletRequest request)
	{
		Map<String, String[]> extraState = Maps.newHashMap();
		Collection<UserManagementLogonFilter> filters = getCurrentState().filters;
		for( UserManagementLogonFilter filter : filters )
		{
			filter.addStateParameters(request, extraState);
		}
		return extraState;
	}

	@Override
	public URI logoutURI(UserState user, URI loggedoutUri)
	{
		Collection<UserManagementLogonFilter> filters = getCurrentState().filters;
		for( UserManagementLogonFilter filter : filters )
		{
			loggedoutUri = filter.logoutURI(user, loggedoutUri);
		}
		return loggedoutUri;
	}

	@Override
	public URI logoutRedirect(URI loggedoutUri)
	{
		Collection<UserManagementLogonFilter> filters = getCurrentState().filters;
		for( UserManagementLogonFilter filter : filters )
		{
			URI redirect = filter.logoutRedirect(loggedoutUri);
			if( redirect != null )
			{
				return redirect;
			}
		}
		return loggedoutUri;
	}

	private static class InstitutionState
	{
		UserDirectoryChain chain;
		Collection<UserManagementLogonFilter> filters;
		Map<?, ?> attributes;
		Cache<String, Triple<Collection<Long>, Collection<Long>, Collection<Long>>> expressionCache = CacheBuilder
			.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
	}

	@Override
	public void clearUserSearchCache()
	{
		getCurrentPlugin().clearUserSearchCache();
	}

	@Override
	public void removeFromCache(String userid)
	{
		getCurrentPlugin().purgeFromCaches(userid);
	}
}
