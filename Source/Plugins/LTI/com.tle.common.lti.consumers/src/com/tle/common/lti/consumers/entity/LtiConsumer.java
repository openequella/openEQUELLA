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

package com.tle.common.lti.consumers.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class LtiConsumer extends BaseEntity
{
	@Index(name = "consumerKey")
	@Column(length = 255, nullable = false)
	private String consumerKey;
	@Lob
	private String consumerSecret;

	@Column(length = 50)
	private String prefix;
	@Column(length = 50)
	private String postfix;
	@Column(length = 255)
	private String allowedExpression;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> instructorRoles;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> otherRoles;
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "lti_consumer_id", nullable = false)
	private Set<LtiConsumerCustomRole> customRoles = new HashSet<LtiConsumerCustomRole>();
	@Column
	private int unknownUser;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> unknownGroups;

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

	public Set<String> getOtherRoles()
	{
		return otherRoles;
	}

	public void setOtherRoles(Set<String> otherRoles)
	{
		this.otherRoles = otherRoles;
	}

	public String getAllowedExpression()
	{
		return allowedExpression;
	}

	public void setAllowedExpression(String allowedExpression)
	{
		this.allowedExpression = allowedExpression;
	}

	public Set<String> getInstructorRoles()
	{
		return instructorRoles;
	}

	public void setInstructorRoles(Set<String> instructorRoles)
	{
		this.instructorRoles = instructorRoles;
	}

	public Set<String> getUnknownGroups()
	{
		return unknownGroups;
	}

	public void setUnknownGroups(Set<String> unknownGroups)
	{
		this.unknownGroups = unknownGroups;
	}

	public Set<LtiConsumerCustomRole> getCustomRoles()
	{
		return customRoles;
	}

	public void setCustomRoles(Set<LtiConsumerCustomRole> customRoles)
	{
		this.customRoles = customRoles;
	}

}
