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

package com.tle.web.controls.resource;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class ResourceAttachmentBean extends EquellaAttachmentBean
{
	private String itemUuid;
	private int itemVersion;
	private char resourceType;
	private String attachmentUuid;
	private String resourcePath;

	public String getItemUuid()
	{
		return itemUuid;
	}

	public void setItemUuid(String itemUuid)
	{
		this.itemUuid = itemUuid;
	}

	public int getItemVersion()
	{
		return itemVersion;
	}

	public void setItemVersion(int itemVersion)
	{
		this.itemVersion = itemVersion;
	}

	public String getAttachmentUuid()
	{
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid)
	{
		this.attachmentUuid = attachmentUuid;
	}

	public String getResourcePath()
	{
		return resourcePath;
	}

	public void setResourcePath(String resourcePath)
	{
		this.resourcePath = resourcePath;
	}

	public char getResourceType()
	{
		return resourceType;
	}

	public void setResourceType(char resourceType)
	{
		this.resourceType = resourceType;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/resource";
	}

}
