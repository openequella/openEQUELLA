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

package com.tle.beans.search;

@SuppressWarnings("nls")
public class TLESettings extends XmlBasedSearchSettings
{
	private static final String SEARCH_TYPE = "LEdgeSearchEngine";

	private static final String USERNAME = "username";
	private static final String INSTITUTIONURL = "insturl";
	private static final String SHAREDSECRETID = "secretid";
	private static final String SHAREDSECRETVALUE = "secretvalue";
	private static final String USELOGGEDINUSER = "userloggedinuser";

	private String username = "";
	private String institutionUrl = "http://";
	private String sharedSecretId = "";
	private String sharedSecretValue = "";
	private boolean useLoggedInUser = true;

	@Override
	protected String getType()
	{
		return SEARCH_TYPE;
	}

	@Override
	protected void _load()
	{
		super._load();
		username = get(USERNAME, username);
		institutionUrl = get(INSTITUTIONURL, institutionUrl);
		sharedSecretId = get(SHAREDSECRETID, sharedSecretId);
		sharedSecretValue = get(SHAREDSECRETVALUE, sharedSecretValue);
		useLoggedInUser = get(USELOGGEDINUSER, useLoggedInUser);
	}

	@Override
	protected void _save()
	{
		super._save();
		put(USERNAME, username);
		put(INSTITUTIONURL, institutionUrl);
		put(SHAREDSECRETID, sharedSecretId);
		put(SHAREDSECRETVALUE, sharedSecretValue);
		put(USELOGGEDINUSER, useLoggedInUser);
	}

	public String getInstitutionUrl()
	{
		return institutionUrl;
	}

	public void setInstitutionUrl(String institutionUrl)
	{
		this.institutionUrl = institutionUrl;
	}

	public String getSharedSecretId()
	{
		return sharedSecretId;
	}

	public void setSharedSecretId(String sharedSecretId)
	{
		this.sharedSecretId = sharedSecretId;
	}

	public String getSharedSecretValue()
	{
		return sharedSecretValue;
	}

	public void setSharedSecretValue(String sharedSecretValue)
	{
		this.sharedSecretValue = sharedSecretValue;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public boolean isUseLoggedInUser()
	{
		return useLoggedInUser;
	}

	public void setUseLoggedInUser(boolean useLoggedInUser)
	{
		this.useLoggedInUser = useLoggedInUser;
	}
}
