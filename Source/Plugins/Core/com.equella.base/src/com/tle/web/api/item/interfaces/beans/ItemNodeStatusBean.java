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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.common.interfaces.I18NString;

public class ItemNodeStatusBean
{
	public enum NodeType
	{
		task, serial, parallel, decision
	}

	public enum NodeStatus
	{
		incomplete, complete, waiting
	}

	private final String uuid;
	private I18NString name;
	private NodeType type;
	private NodeStatus status;
	private List<ItemNodeStatusMessageBean> comments;
	private List<ItemNodeStatusBean> children;

	@JsonCreator
	public ItemNodeStatusBean(@JsonProperty("uuid") String uuid)
	{
		this.uuid = uuid;
	}

	public String getUuid()
	{
		return uuid;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

	public List<ItemNodeStatusMessageBean> getComments()
	{
		return comments;
	}

	public void setComments(List<ItemNodeStatusMessageBean> comments)
	{
		this.comments = comments;
	}

	public List<ItemNodeStatusBean> getChildren()
	{
		return children;
	}

	public void setChildren(List<ItemNodeStatusBean> children)
	{
		this.children = children;
	}

	public NodeType getType()
	{
		return type;
	}

	public void setType(NodeType type)
	{
		this.type = type;
	}

	public NodeStatus getStatus()
	{
		return status;
	}

	public void setStatus(NodeStatus status)
	{
		this.status = status;
	}
}
