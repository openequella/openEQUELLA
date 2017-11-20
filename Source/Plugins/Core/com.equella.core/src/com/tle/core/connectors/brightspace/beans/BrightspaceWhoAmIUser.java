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

package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class BrightspaceWhoAmIUser
{
	@JsonProperty("Identifier")
	private String identifier;
	@JsonProperty("FirstName")
	private String firstName;
	@JsonProperty("LastName")
	private String lastName;
	@JsonProperty("UniqueName")
	private String uniqueName;
	@JsonProperty("ProfileIdentifier")
	private String profileIdentifier;

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getUniqueName()
	{
		return uniqueName;
	}

	public void setUniqueName(String uniqueName)
	{
		this.uniqueName = uniqueName;
	}

	public String getProfileIdentifier()
	{
		return profileIdentifier;
	}

	public void setProfileIdentifier(String profileIdentifier)
	{
		this.profileIdentifier = profileIdentifier;
	}
}
