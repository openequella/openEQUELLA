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

package com.tle.beans.activation;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.item.Item;
import com.tle.beans.item.cal.request.CourseInfo;

@Entity
@AccessType("field")
public class ActivateRequest implements Cloneable
{
	public static final int TYPE_ACTIVE = 0;
	public static final int TYPE_INACTIVE = 1;
	public static final int TYPE_PENDING = 2;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	private String uuid;

	@Column(length = 8, nullable = false)
	private String type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Index(name = "activateRequestItem")
	private Item item;

	@Column(length = 45, nullable = false)
	private String attachment;

	@Column(length = 256)
	private String user;
	private int status;
	private Date time;

	private Date from;
	private Date until;

	@ManyToOne
	@JoinColumn(name = "course_info_id", nullable = false)
	@Index(name = "activateRequestCourse")
	private CourseInfo course;

	@Column(length = 128)
	private String citation;
	@Column(length = 256)
	private String locationName;
	@Column(length = 256)
	private String locationId;

	@Lob
	private String overrideReason;

	@Lob
	private String description;

	public void setStatus(int status)
	{
		this.status = status;
	}

	public ActivateRequest()
	{
		// nothing
	}

	public ActivateRequest(ActivateRequest request)
	{
		id = request.id;
		attachment = request.attachment;
		user = request.user;
		course = request.course;
		status = request.status;
		time = request.time;
	}

	public CourseInfo getCourse()
	{
		return course;
	}

	public Date getTime()
	{
		return time;
	}

	public void setTime(Date date)
	{
		this.time = date;
	}

	public String getAttachment()
	{
		return attachment;
	}

	public void setAttachment(String attachment)
	{
		this.attachment = attachment;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public int getStatus()
	{
		return status;
	}

	public void setCourse(CourseInfo course)
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

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

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

	public String getOverrideReason()
	{
		return overrideReason;
	}

	public void setOverrideReason(String overrideReason)
	{
		this.overrideReason = overrideReason;
	}
}