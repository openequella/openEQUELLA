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

package com.tle.common.usermanagement.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.tle.beans.Institution;
import com.tle.common.Triple;
import com.tle.common.usermanagement.user.valuebean.UserBean;

/**
 * This class also effectively implements ModifiableUserState, but we don't want
 * to mention this so that user states such as the AnonymousUserState can use
 * this, but not be modifiable.
 * 
 * @author Nicholas Read
 */
public abstract class AbstractUserState implements ModifiableUserState
{
	private static final long serialVersionUID = -7522028980904163568L;

	private Institution institution;
	private UserBean loggedInUser;
	private String sessionID;
	private boolean authenticated;
	private String ipAddress;
	private String hostAddress;
	private String hostReferrer;

	private String sharePassEmail;
	private String token;
	private String tokenSecretId;

	private Set<String> usersGroups;
	private Set<String> usersRoles;

	private Collection<Long> commonAclExpressions;
	private Collection<Long> ownerAclExpressions;
	private Collection<Long> notOwnerAclExpressions;

	private boolean internal;
	private boolean wasAutoLoggedIn;
	private boolean needsSessionUpdate = true;
	private boolean auditable = true;
	private transient Map<Object, Object> cacheAttr;

	@Override
	public UserBean getUserBean()
	{
		return loggedInUser;
	}

	@Override
	public String getSessionID()
	{
		return sessionID;
	}

	@Override
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	@Override
	public Institution getInstitution()
	{
		return institution;
	}

	@Override
	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated)
	{
		authenticated = isAuthenticated;
	}

	@Override
	public boolean isAuthenticated()
	{
		return authenticated;
	}

	@Override
	public Set<String> getUsersRoles()
	{
		if( usersRoles == null )
		{
			usersRoles = new HashSet<String>();
		}
		return usersRoles;
	}

	public Object getDetails()
	{
		return loggedInUser;
	}

	public Object getPrincipal()
	{
		return loggedInUser.getUniqueID();
	}

	public String getName()
	{
		return loggedInUser.getFirstName();
	}

	@Override
	public Set<String> getUsersGroups()
	{
		if( usersGroups == null )
		{
			usersGroups = new HashSet<String>();
		}
		return usersGroups;
	}

	@Override
	public Collection<Long> getCommonAclExpressions()
	{
		return commonAclExpressions;
	}

	@Override
	public Collection<Long> getOwnerAclExpressions()
	{
		return ownerAclExpressions;
	}

	@Override
	public Collection<Long> getNotOwnerAclExpressions()
	{
		return notOwnerAclExpressions;
	}

	@Override
	public String getIpAddress()
	{
		return ipAddress;
	}

	@Override
	public String getHostAddress()
	{
		return hostAddress;
	}

	@Override
	public String getHostReferrer()
	{
		return hostReferrer;
	}

	@Override
	public String getSharePassEmail()
	{
		return sharePassEmail;
	}

	@Override
	public String getToken()
	{
		return token;
	}

	@Override
	public String getTokenSecretId()
	{
		return tokenSecretId;
	}

	@Override
	public void setAclExpressions(Triple<Collection<Long>, Collection<Long>, Collection<Long>> expressions)
	{
		this.commonAclExpressions = expressions.getFirst();
		this.ownerAclExpressions = expressions.getSecond();
		this.notOwnerAclExpressions = expressions.getThird();
	}

	@Override
	public void setLoggedInUser(UserBean user)
	{
		this.loggedInUser = user;
	}

	@Override
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	@Override
	public void setHostAddress(String hostAddress)
	{
		this.hostAddress = hostAddress;
	}

	@Override
	public void setHostReferrer(String hostReferrer)
	{
		this.hostReferrer = hostReferrer;
	}

	@Override
	public void setSharePassEmail(String sharePassEmail)
	{
		this.sharePassEmail = sharePassEmail;
	}

	@Override
	public void setToken(String sharePassToken)
	{
		this.token = sharePassToken;
	}

	@Override
	public void setTokenSecretId(String tokenSecretId)
	{
		this.tokenSecretId = tokenSecretId;
	}

	@Override
	public boolean isSystem()
	{
		return false;
	}

	@Override
	public boolean isGuest()
	{
		return false;
	}

	@Override
	public boolean isInternal()
	{
		return internal;
	}

	public void setInternal(boolean internal)
	{
		this.internal = internal;
	}

	@Override
	public boolean wasAutoLoggedIn()
	{
		return wasAutoLoggedIn;
	}

	@Override
	public void setWasAutoLoggedIn(boolean b)
	{
		wasAutoLoggedIn = b;
	}

	@Override
	public boolean isNeedsSessionUpdate()
	{
		return needsSessionUpdate;
	}

	@Override
	public void updatedInSession()
	{
		needsSessionUpdate = false;
	}

	@Override
	public boolean isAuditable()
	{
		return auditable;
	}

	// Explicit catch of CloneNotSupportedException from super.clone()
	@Override
	public UserState clone() // NOSONAR
	{
		try
		{
			return (UserState) super.clone();
		}
		catch( CloneNotSupportedException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setAuditable(boolean auditable)
	{
		this.auditable = auditable;
	}

	@Override
	public synchronized void removeCachedAttribute(Object key)
	{
		if( cacheAttr != null )
		{
			cacheAttr.remove(key);
		}
	}

	@Override
	public synchronized void setCachedAttribute(Object key, Object value)
	{
		if( cacheAttr == null )
		{
			cacheAttr = Maps.newHashMap();
		}
		cacheAttr.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T getCachedAttribute(Object key)
	{
		if( cacheAttr == null )
		{
			return null;
		}
		return (T) cacheAttr.get(key);
	}
}
