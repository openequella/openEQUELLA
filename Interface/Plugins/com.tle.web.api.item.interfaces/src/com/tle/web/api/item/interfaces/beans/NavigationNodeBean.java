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

package com.tle.web.api.item.interfaces.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;


/**
 * @author Aaron
 */
@XmlRootElement
public class NavigationNodeBean extends AbstractExtendableBean
{
	private String uuid;
	private String name;
	private String icon;
	private String imsId;
	private List<NavigationTabBean> tabs;
	private List<NavigationNodeBean> nodes;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getImsId()
	{
		return imsId;
	}

	public void setImsId(String imsId)
	{
		this.imsId = imsId;
	}

	public List<NavigationTabBean> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<NavigationTabBean> tabs)
	{
		this.tabs = tabs;
	}

	public List<NavigationNodeBean> getNodes()
	{
		return nodes;
	}

	public void setNodes(List<NavigationNodeBean> nodes)
	{
		this.nodes = nodes;
	}
}
