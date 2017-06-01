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

import com.tle.web.sections.standard.model.HtmlLinkState;

public class CustomLinkListComponent
{
	private String name;
	private String uuid;
	private String iconUrl;
	private HtmlLinkState edit;
	private HtmlLinkState delete;

	public CustomLinkListComponent(String name, String uuid, HtmlLinkState edit, HtmlLinkState delete, String iconUrl)
	{
		this.name = name;
		this.uuid = uuid;
		this.edit = edit;
		this.delete = delete;
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

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public HtmlLinkState getEdit()
	{
		return edit;
	}

	public void setEdit(HtmlLinkState edit)
	{
		this.edit = edit;
	}

	public HtmlLinkState getDelete()
	{
		return delete;
	}

	public void setDelete(HtmlLinkState delete)
	{
		this.delete = delete;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}
}