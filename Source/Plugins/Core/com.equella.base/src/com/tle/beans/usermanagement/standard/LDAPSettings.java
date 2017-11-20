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

package com.tle.beans.usermanagement.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Check;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyHashMap;
import com.tle.common.settings.annotation.PropertyList;

/**
 * A small structure to hold LDAP settings. Created on Jan 5, 2003
 * 
 * @author Nicholas Read
 */
public class LDAPSettings extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	// The following properties are deprecated in favour of a single field
	// called "url" below.
	@Property(key = "ldap.host")
	private String host;
	@Property(key = "ldap.port")
	private int port;
	@Property(key = "ldap.ssl")
	private boolean ssl;

	// This field replaces all of the above deprecated fields. If URL is empty,
	// then we check host/port/ssl instead.
	@Property(key = "ldap.url")
	private String url;

	@Property(key = "ldap.defaultdomain")
	private String defaultDomain;

	@PropertyList(key = "ldap.bases")
	private List<String> bases = new ArrayList<String>();
	@PropertyHashMap(key = "ldap.attributes")
	private Map<String, String> attributes = new HashMap<String, String>();

	@Property(key = "ldap.admin.username")
	private String adminUsername = ""; //$NON-NLS-1$
	@Property(key = "ldap.admin.password")
	private String adminPassword = ""; //$NON-NLS-1$

	@Property(key = "ldap.searchLimit")
	private int searchLimit = 100;
	@Property(key = "ldap.version")
	private String version = ""; //$NON-NLS-1$
	@Property(key = "ldap.persionObject")
	private String personObject = "person"; //$NON-NLS-1$
	@Property(key = "ldap.groupObject")
	private String groupObject = "group"; //$NON-NLS-1$
	@Property(key = "ldap.forcelowercaseids")
	private boolean forceLowercaseIds = false;
	@Property(key = "ldap.follow")
	private boolean follow = false;
	@Property(key = "ldap.wildcards")
	private boolean wildcards = true;
	@Property(key = "ldap.validateCert")
	private boolean validateCert = false;
	@Property(key = "ldap.enabled")
	private boolean enabled;

	public void setUrl(String url)
	{
		this.url = url;

		// As soon as we set a URL, clear out the host field to make sure we
		// never rebuild the URL based on values in the old, deprecated fields.
		host = null;
	}

	@SuppressWarnings("nls")
	public String getUrl()
	{
		if( Check.isEmpty(url) && !Check.isEmpty(host) )
		{
			StringBuilder sb = new StringBuilder("ldap");
			if( ssl )
			{
				sb.append('s');
			}
			sb.append("://");
			sb.append(host);
			if( (!ssl && port != 389) || (ssl && port != 636) )
			{
				sb.append(':');
				sb.append(port);
			}
			url = sb.toString();
		}
		return url;
	}

	public String getDefaultDomain()
	{
		return defaultDomain;
	}

	public void setDefaultDomain(String defaultDomain)
	{
		this.defaultDomain = defaultDomain;
	}

	public List<String> getBases()
	{
		return bases;
	}

	public void setBases(List<String> bases)
	{
		this.bases = bases;
	}

	public Map<String, String> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Map<String, String> map)
	{
		this.attributes = map;
	}

	public String getAdminPassword()
	{
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}

	public String getAdminUsername()
	{
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername)
	{
		this.adminUsername = adminUsername;
	}

	public String getGroupObject()
	{
		return groupObject;
	}

	public void setGroupObject(String groupObject)
	{
		this.groupObject = groupObject;
	}

	public String getPersonObject()
	{
		return personObject;
	}

	public void setPersonObject(String personObject)
	{
		this.personObject = personObject;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public int getSearchLimit()
	{
		return searchLimit;
	}

	public void setSearchLimit(int searchLimit)
	{
		this.searchLimit = searchLimit;
	}

	public boolean isForceLowercaseIds()
	{
		return forceLowercaseIds;
	}

	public void setForceLowercaseIds(boolean forceLowercaseIds)
	{
		this.forceLowercaseIds = forceLowercaseIds;
	}

	public boolean isFollow()
	{
		return follow;
	}

	public void setFollow(boolean follow)
	{
		this.follow = follow;
	}

	public boolean isWildcards()
	{
		return wildcards;
	}

	public void setWildcards(boolean wildcards)
	{
		this.wildcards = wildcards;
	}

	public boolean isValidateCert()
	{
		return validateCert;
	}

	public void setValidateCert(boolean validateCert)
	{
		this.validateCert = validateCert;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
