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

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.UserBean;

@XmlRootElement
public class ItemStatusBean
{
	private String status;
	private String rejectedMessage;
	private UserBean rejectedBy;
	private ItemNodeStatusBean nodes;

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public ItemNodeStatusBean getNodes()
	{
		return nodes;
	}

	public void setNodes(ItemNodeStatusBean nodes)
	{
		this.nodes = nodes;
	}

	public UserBean getRejectedBy()
	{
		return rejectedBy;
	}

	public void setRejectedBy(UserBean rejectedBy)
	{
		this.rejectedBy = rejectedBy;
	}

	public String getRejectedMessage()
	{
		return rejectedMessage;
	}

	public void setRejectedMessage(String rejectedMessage)
	{
		this.rejectedMessage = rejectedMessage;
	}
}
