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

import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.common.NameValue;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.CachedData;
import com.tle.web.viewurl.ViewItemResource;

public class AbstractTreeViewerModel
{
	private String definition;
	private List<NameValue> tabs;
	private final CachedData<ItemNavigationTree> navigationTree = new CachedData<ItemNavigationTree>();
	private ViewItemResource resource;
	private String method;

	@Bookmarked(parameter = "hideNavBar")
	private boolean hideNavBar;
	private boolean hideNavControls;
	private boolean hideTree;

	@Bookmarked
	private String node;

	public String getNode()
	{
		return node;
	}

	public void setNode(String node)
	{
		this.node = node;
	}

	public List<NameValue> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<NameValue> tabs)
	{
		this.tabs = tabs;
	}

	public CachedData<ItemNavigationTree> getNavigationTree()
	{
		return navigationTree;
	}

	public String getDefinition()
	{
		return definition;
	}

	public void setDefinition(String definition)
	{
		this.definition = definition;
	}

	public boolean isHideNavBar()
	{
		return hideNavBar;
	}

	public void setHideNavBar(boolean hideNavBar)
	{
		this.hideNavBar = hideNavBar;
	}

	public boolean isHideNavControls()
	{
		return hideNavControls;
	}

	public void setHideNavControls(boolean hideNavControls)
	{
		this.hideNavControls = hideNavControls;
	}

	public boolean isHideTree()
	{
		return hideTree;
	}

	public void setHideTree(boolean hideTree)
	{
		this.hideTree = hideTree;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public ViewItemResource getResource()
	{
		return resource;
	}

	public void setResource(ViewItemResource resource)
	{
		this.resource = resource;
	}
}