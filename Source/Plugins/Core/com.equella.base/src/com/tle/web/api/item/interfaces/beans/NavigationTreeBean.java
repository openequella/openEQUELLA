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


@XmlRootElement
public class NavigationTreeBean extends AbstractExtendableBean
{
	private boolean hideUnreferencedAttachments;
	private boolean showSplitOption;
	private List<NavigationNodeBean> nodes;

	@Deprecated
	public void setShowUnreferencedAttachments(boolean showUnreferencedAttachments)
	{
		this.hideUnreferencedAttachments = showUnreferencedAttachments;
	}

	public boolean isShowSplitOption()
	{
		return showSplitOption;
	}

	public void setShowSplitOption(boolean showSplitOption)
	{
		this.showSplitOption = showSplitOption;
	}

	public List<NavigationNodeBean> getNodes()
	{
		return nodes;
	}

	public void setNodes(List<NavigationNodeBean> nodes)
	{
		this.nodes = nodes;
	}

	public boolean isHideUnreferencedAttachments()
	{
		return hideUnreferencedAttachments;
	}

	public void setHideUnreferencedAttachments(boolean hideUnreferencedAttachments)
	{
		this.hideUnreferencedAttachments = hideUnreferencedAttachments;
	}
}
