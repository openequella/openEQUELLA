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

package com.tle.core.htmleditor.service;

import com.tle.core.entity.EntityEditingBean;

/**
 * @author Aaron
 */
public class HtmlEditorPluginEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String pluginId;
	private String author;
	private String type;
	private String buttons;
	private String config;
	private String extra;
	private String clientJs;
	private String serverJs;

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getButtons()
	{
		return buttons;
	}

	public void setButtons(String buttons)
	{
		this.buttons = buttons;
	}

	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	public String getExtra()
	{
		return extra;
	}

	public void setExtra(String extra)
	{
		this.extra = extra;
	}

	public String getClientJs()
	{
		return clientJs;
	}

	public void setClientJs(String clientJs)
	{
		this.clientJs = clientJs;
	}

	public String getServerJs()
	{
		return serverJs;
	}

	public void setServerJs(String serverJs)
	{
		this.serverJs = serverJs;
	}
}
