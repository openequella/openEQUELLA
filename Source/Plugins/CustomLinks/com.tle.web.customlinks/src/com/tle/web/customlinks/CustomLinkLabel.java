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

package com.tle.web.customlinks;

import java.io.Serializable;

public class CustomLinkLabel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	private String url;
	private boolean newWindow;
	private String iconUrl;

	public CustomLinkLabel(String name, String url, boolean newWindow, String iconUrl)
	{
		this.name = name;
		this.url = url;
		this.newWindow = newWindow;
		this.iconUrl = iconUrl;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean isNewWindow()
	{
		return newWindow;
	}

	public void setNewWindow(boolean newWindow)
	{
		this.newWindow = newWindow;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

}