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

package com.tle.core.oauth;

import java.io.Serializable;

public class OAuthFlowDefinition implements Serializable
{
	private final boolean setUrl;
	private final boolean useInbuiltUrl;
	private final boolean setUser;
	private final String nameKey;
	private final String descriptionKey;
	private final String redirectUrl;
	private final String id;

	public OAuthFlowDefinition(boolean setUrl, boolean useInbuiltUrl, boolean setUser, String nameKey,
		String descriptionKey, String redirectUrl, String id)
	{
		this.setUrl = setUrl;
		this.useInbuiltUrl = useInbuiltUrl;
		this.setUser = setUser;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
		this.redirectUrl = redirectUrl;
		this.id = id;
	}

	public boolean isSetUrl()
	{
		return setUrl;
	}

	public boolean isUseInbuiltUrl()
	{
		return useInbuiltUrl;
	}

	public boolean isSetUser()
	{
		return setUser;
	}

	public String getNameKey()
	{
		return nameKey;
	}

	public String getDescriptionKey()
	{
		return descriptionKey;
	}

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public String getId()
	{
		return id;
	}
}
