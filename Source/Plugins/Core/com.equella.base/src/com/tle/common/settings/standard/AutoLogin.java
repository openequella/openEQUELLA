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

package com.tle.common.settings.standard;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyList;

/**
 * @author Nicholas Read
 */
public class AutoLogin implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	private transient HostMatcher hostMatcher;

	@Property(key = "login.auto.userid")
	private String userid;

	@Property(key = "login.auto.username")
	private String username;

	/**
	 * The member previously known only as 'enabled', which is to say; enabled
	 * login via ip address.
	 */
	@Property(key = "login.auto.enabled")
	private boolean enabledViaIp;

	@Property(key = "login.via.ssl")
	private boolean loginViaSSL;

	@Property(key = "login.auto.notautomatic")
	private boolean notAutomatic;

	@Property(key = "login.auto.edit.details.disabled")
	private boolean editDetailsDisallowed;

	@Property(key = "login.auto.transient.drm.acceptances")
	private boolean transientDrmAcceptances;

	@PropertyList(key = "login.auto.addresses")
	private final List<String> addresses = new ArrayList<String>();

	@Property(key = "login.notice")
	private String loginNotice;

	@Property(key = "login.anon.ip.httpref.acl")
	private boolean enableIpReferAcl;

	public List<String> getAddresses()
	{
		return addresses;
	}

	public boolean isEnabledViaIp()
	{
		return enabledViaIp;
	}

	public void setEnabledViaIp(boolean enabledViaIp)
	{
		this.enabledViaIp = enabledViaIp;
	}

	public boolean isLoginViaSSL()
	{
		return loginViaSSL;
	}

	public void setLoginViaSSL(boolean loginViaSSL)
	{
		this.loginViaSSL = loginViaSSL;
	}

	public String getUserid()
	{
		return userid;
	}

	public void setUserid(String userid)
	{
		this.userid = userid;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public boolean isEditDetailsDisallowed()
	{
		return editDetailsDisallowed;
	}

	public void setEditDetailsDisallowed(boolean editDetailsDisallowed)
	{
		this.editDetailsDisallowed = editDetailsDisallowed;
	}

	public boolean isNotAutomatic()
	{
		return notAutomatic;
	}

	public void setNotAutomatic(boolean notAutomatic)
	{
		this.notAutomatic = notAutomatic;
	}

	public HostMatcher getHostMatcher()
	{
		if( hostMatcher == null )
		{
			hostMatcher = new HostMatcher(addresses);
		}
		return hostMatcher;
	}

	public boolean isTransientDrmAcceptances()
	{
		return transientDrmAcceptances;
	}

	public void setTransientDrmAcceptances(boolean transientDrmAcceptances)
	{
		this.transientDrmAcceptances = transientDrmAcceptances;
	}

	public String getLoginNotice()
	{
		return loginNotice;
	}

	public void setLoginNotice(String loginNotice)
	{
		this.loginNotice = loginNotice;
	}

	public boolean isEnableIpReferAcl()
	{
		return enableIpReferAcl;
	}

	public void setEnableIpReferAcl(boolean enableIpReferAcl)
	{
		this.enableIpReferAcl = enableIpReferAcl;
	}
}
