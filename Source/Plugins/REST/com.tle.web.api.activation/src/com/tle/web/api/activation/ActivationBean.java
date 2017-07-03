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

package com.tle.web.api.activation;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

@XmlRootElement
public class ActivationBean extends AbstractExtendableBean
{
	private String uuid;
	private String type;
	private EquellaItemBean item;
	private String attachment;
	private UserBean user;
	private String status;
	private Date from;
	private Date until;
	private CourseBean course;
	private String citation;
	private String description;
	private String locationId;
	private String locationName;
	private String overrideMessage;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public EquellaItemBean getItem()
	{
		return item;
	}

	public void setItem(EquellaItemBean item)
	{
		this.item = item;
	}

	public String getAttachment()
	{
		return attachment;
	}

	public void setAttachment(String attachment)
	{
		this.attachment = attachment;
	}

	public Date getFrom()
	{
		return from;
	}

	public void setFrom(Date from)
	{
		this.from = from;
	}

	public Date getUntil()
	{
		return until;
	}

	public void setUntil(Date until)
	{
		this.until = until;
	}

	public UserBean getUser()
	{
		return user;
	}

	public void setUser(UserBean user)
	{
		this.user = user;
	}

	public CourseBean getCourse()
	{
		return course;
	}

	public void setCourse(CourseBean course)
	{
		this.course = course;
	}

	public String getCitation()
	{
		return citation;
	}

	public void setCitation(String citation)
	{
		this.citation = citation;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getLocationId()
	{
		return locationId;
	}

	public void setLocationId(String locationId)
	{
		this.locationId = locationId;
	}

	public String getLocationName()
	{
		return locationName;
	}

	public void setLocationName(String locationName)
	{
		this.locationName = locationName;
	}

	public String getOverrideMessage()
	{
		return overrideMessage;
	}

	public void setOverrideMessage(String overrideMessage)
	{
		this.overrideMessage = overrideMessage;
	}
}
