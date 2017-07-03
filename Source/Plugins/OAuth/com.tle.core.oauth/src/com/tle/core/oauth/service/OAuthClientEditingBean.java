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

package com.tle.core.oauth.service;

import java.util.Set;

import com.tle.beans.security.AccessExpression;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.oauth.OAuthFlowDefinition;

public class OAuthClientEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String clientId;
	private String clientSecret;
	private String redirectUrl;
	private Set<String> permissions;
	private String userId;
	private boolean requiresApproval;
	private AccessExpression usersExpression;
	private OAuthFlowDefinition flowDef;

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getClientSecret()
	{
		return clientSecret;
	}

	public void setClientSecret(String clientSecret)
	{
		this.clientSecret = clientSecret;
	}

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
	}

	public Set<String> getPermissions()
	{
		return permissions;
	}

	public void setPermissions(Set<String> permissions)
	{
		this.permissions = permissions;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public boolean isRequiresApproval()
	{
		return requiresApproval;
	}

	public void setRequiresApproval(boolean requiresApproval)
	{
		this.requiresApproval = requiresApproval;
	}

	public AccessExpression getUsersExpression()
	{
		return usersExpression;
	}

	public void setUsersExpression(AccessExpression usersExpression)
	{
		this.usersExpression = usersExpression;
	}

	public OAuthFlowDefinition getFlowDef()
	{
		return flowDef;
	}

	public void setFlowDef(OAuthFlowDefinition flowDef)
	{
		this.flowDef = flowDef;
	}
}