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

package com.tle.web.lti.consumers.api.beans;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.users.interfaces.beans.GroupBean;
import com.tle.web.api.users.interfaces.beans.RoleBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class LtiConsumerBean extends BaseEntityBean
{
	public static final String ACTION_CREATE_USER = "create";
	public static final String ACTION_GUEST = "ignore";
	public static final String ACTION_ERROR = "deny";

	private String consumerKey;
	private String consumerSecret;
	private String usernamePrefix;
	private String usernamePostfix;
	private String allowedUsersExpression;
	private Set<RoleBean> instructorRoles;
	private Set<RoleBean> otherRoles;
	private String unknownUserAction;
	private Set<GroupBean> unknownUserGroups;
	private Map<String, RoleBean> customRoles;

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret()
	{
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret)
	{
		this.consumerSecret = consumerSecret;
	}

	public String getUsernamePrefix()
	{
		return usernamePrefix;
	}

	public void setUsernamePrefix(String usernamePrefix)
	{
		this.usernamePrefix = usernamePrefix;
	}

	public String getUsernamePostfix()
	{
		return usernamePostfix;
	}

	public void setUsernamePostfix(String usernamePostfix)
	{
		this.usernamePostfix = usernamePostfix;
	}

	public String getAllowedUsersExpression()
	{
		return allowedUsersExpression;
	}

	public void setAllowedUsersExpression(String allowedUsersExpression)
	{
		this.allowedUsersExpression = allowedUsersExpression;
	}

	public Set<RoleBean> getInstructorRoles()
	{
		return instructorRoles;
	}

	public void setInstructorRoles(Set<RoleBean> instructorRoles)
	{
		this.instructorRoles = instructorRoles;
	}

	public Set<RoleBean> getOtherRoles()
	{
		return otherRoles;
	}

	public void setOtherRoles(Set<RoleBean> otherRoles)
	{
		this.otherRoles = otherRoles;
	}

	public String getUnknownUserAction()
	{
		return unknownUserAction;
	}

	public void setUnknownUserAction(String unknownUserAction)
	{
		this.unknownUserAction = unknownUserAction;
	}

	public Set<GroupBean> getUnknownUserGroups()
	{
		return unknownUserGroups;
	}

	public void setUnknownUserGroups(Set<GroupBean> unknownUserGroups)
	{
		this.unknownUserGroups = unknownUserGroups;
	}

	public Map<String, RoleBean> getCustomRoles()
	{
		return customRoles;
	}

	public void setCustomRoles(Map<String, RoleBean> customRoles)
	{
		this.customRoles = customRoles;
	}
}
