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

package com.tle.core.cloud.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
// Note: I know item is not a base entity, but all the required fields are there
// :)
@XmlRootElement
public class CloudItemBean extends BaseEntityBean
{
	private int version;
	private List<CloudAttachmentBean> attachments;
	private String metadata;
	private CloudNavigationSettingsBean navigation;

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public List<CloudAttachmentBean> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<CloudAttachmentBean> attachments)
	{
		this.attachments = attachments;
	}

	public String getMetadata()
	{
		return metadata;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	public CloudNavigationSettingsBean getNavigation()
	{
		return navigation;
	}

	public void setNavigation(CloudNavigationSettingsBean navigation)
	{
		this.navigation = navigation;
	}
}
