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

package com.tle.web.viewitem.treeviewer;

import java.util.List;

public class TreeNode
{
	private String uuid;
	private String name;
	private boolean open;
	private String icon;
	private String url;
	private boolean nokids;
	private List<TreeTab> tabs;
	private List<TreeNode> children;

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

	public boolean isOpen()
	{
		return open;
	}

	public void setOpen(boolean open)
	{
		this.open = open;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean isNokids()
	{
		return nokids;
	}

	public void setNokids(boolean nokids)
	{
		this.nokids = nokids;
	}

	public List<TreeTab> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<TreeTab> tabs)
	{
		this.tabs = tabs;
	}

	public List<TreeNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<TreeNode> children)
	{
		this.children = children;
	}
}