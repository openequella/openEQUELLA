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

package com.tle.web.api.item.equella.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.item.interfaces.beans.AttachmentBean;

/**
 * An EQUELLA- compatible variety of AttachmentBean.
 * 
 * @author larry
 */
@XmlRootElement
public abstract class EquellaAttachmentBean extends AttachmentBean
{
	private boolean restricted;
	private String thumbnail;

	public boolean isRestricted()
	{
		return restricted;
	}

	public void setRestricted(boolean restricted)
	{
		this.restricted = restricted;
	}

	public String getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail(String thumbnail)
	{
		this.thumbnail = thumbnail;
	}
}
