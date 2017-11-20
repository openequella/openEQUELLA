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

package com.tle.common.htmleditor.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
// TODO: how do you do this when it's across two tables????
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames =
// {"institution_id", "clientId"})})
public final class HtmlEditorPlugin extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Index(name = "htmleditorPluginIdIndex")
	@Column(nullable = false, length = 100)
	private String pluginId;
	/**
	 * E.g. tinymce
	 */
	@Index(name = "htmleditorPluginType")
	@Column(nullable = false, length = 20)
	private String type;
	@Column(nullable = true, length = 255)
	private String author;
	@Lob
	private String buttons;
	@Lob
	private String config;
	@Lob
	private String extra;
	@Lob
	private String clientJs;
	@Lob
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

	/**
	 * A JSON array of button defintions E.g. <code>
	 * [
	 * {"id":"bacon_example_frybacon",
	 * "image":"images/frybacon.png"},
	 * {"id":"bacon_example_grillbacon",
	 * "image":"images/grillbacon.png"}
	 * ]
	 * </code>
	 * 
	 * @return
	 */
	public String getButtons()
	{
		return buttons;
	}

	public void setButtons(String buttons)
	{
		this.buttons = buttons;
	}

	/**
	 * User-specified config
	 */
	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	/**
	 * Editor type specific config. E.g. tinymce specific properties
	 */
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
