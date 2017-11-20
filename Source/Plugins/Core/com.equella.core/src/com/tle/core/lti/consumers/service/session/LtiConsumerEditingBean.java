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

package com.tle.core.lti.consumers.service.session;

import java.util.Set;

import com.tle.common.Pair;
import com.tle.core.entity.EntityEditingBean;

public class LtiConsumerEditingBean extends EntityEditingBean
{
	private String consumerKey;
	private String consumerSecret;
	private String prefix;
	private String postfix;
	private String allowedExpression;
	private Set<String> instructorRoles;
	private Set<String> otherRoles;
	private int unknownUser;
	private Set<String> unknownGroups;
	private Set<Pair<String, String>> customRoles;

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

	public int getUnknownUser()
	{
		return unknownUser;
	}

	public void setUnknownUser(int unknownUser)
	{
		this.unknownUser = unknownUser;
	}

	public Set<String> getInstructorRoles()
	{
		return instructorRoles;
	}

	public void setInstructorRoles(Set<String> instructorRoles)
	{
		this.instructorRoles = instructorRoles;
	}

	public Set<String> getOtherRoles()
	{
		return otherRoles;
	}

	public void setOtherRoles(Set<String> otherRoles)
	{
		this.otherRoles = otherRoles;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public String getPostfix()
	{
		return postfix;
	}

	public void setPostfix(String postfix)
	{
		this.postfix = postfix;
	}

	public String getAllowedExpression()
	{
		return allowedExpression;
	}

	public void setAllowedExpression(String allowedExpression)
	{
		this.allowedExpression = allowedExpression;
	}

	public Set<String> getUnknownGroups()
	{
		return unknownGroups;
	}

	public void setUnknownGroups(Set<String> unknownGroups)
	{
		this.unknownGroups = unknownGroups;
	}

	public Set<Pair<String, String>> getCustomRoles()
	{
		return customRoles;
	}

	public void setCustomRoles(Set<Pair<String, String>> customRoles)
	{
		this.customRoles = customRoles;
	}

}
