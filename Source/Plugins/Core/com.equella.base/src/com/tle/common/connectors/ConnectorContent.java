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

package com.tle.common.connectors;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ConnectorContent
{
	public static final String KEY_INSTRUCTOR = "instructor";
	public static final String KEY_ENROLLMENTS = "enrollments";
	public static final String KEY_DATE_ACCESSED = "dateAccessed";

	private String id;
	private String courseId;
	private String course;
	private String courseCode;
	private String courseUrl;
	private String folderId;
	private String folder;
	private String folderUrl;
	private Date dateAdded;
	private Date dateModified;
	private String attachmentUuid;
	private String attachmentUrl;
	private String uuid;
	private int version;
	private String externalTitle;
	private String externalUrl;
	private String externalDescription;
	private boolean available;

	private final Map<String, ConnectorContentAttribute> attributes = Maps.newHashMap();

	public ConnectorContent(String id)
	{
		this.id = id;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getFolderId()
	{
		return folderId;
	}

	public void setFolderId(String folderId)
	{
		this.folderId = folderId;
	}

	public String getCourse()
	{
		return course;
	}

	public void setCourse(String course)
	{
		this.course = course;
	}

	public String getCourseCode()
	{
		return courseCode;
	}

	public void setCourseCode(String courseCode)
	{
		this.courseCode = courseCode;
	}

	public String getCourseUrl()
	{
		return courseUrl;
	}

	public void setCourseUrl(String courseUrl)
	{
		this.courseUrl = courseUrl;
	}

	public String getFolder()
	{
		return folder;
	}

	public void setFolder(String folder)
	{
		this.folder = folder;
	}

	public Date getDateAdded()
	{
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded)
	{
		this.dateAdded = dateAdded;
	}

	public Date getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Date dateModified)
	{
		this.dateModified = dateModified;
	}

	public String getAttachmentUuid()
	{
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid)
	{
		this.attachmentUuid = attachmentUuid;
	}

	public String getAttachmentUrl()
	{
		return attachmentUrl;
	}

	public void setAttachmentUrl(String attachmentUrl)
	{
		this.attachmentUrl = attachmentUrl;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public ConnectorContentAttribute getAttribute(String key)
	{
		return attributes.get(key);
	}

	public List<ConnectorContentAttribute> getAttributeList()
	{
		return Lists.newArrayList(attributes.values());
	}

	public void setAttribute(String key, String labelKey, Object value)
	{
		setAttribute(key, labelKey, value, false);
	}

	public void setAttribute(String key, String labelKey, Object value, boolean hide)
	{
		attributes.put(key, new ConnectorContentAttribute(labelKey, value, hide));
	}

	public String getFolderUrl()
	{
		return folderUrl;
	}

	public void setFolderUrl(String folderUrl)
	{
		this.folderUrl = folderUrl;
	}

	public String getExternalTitle()
	{
		return externalTitle;
	}

	public void setExternalTitle(String externalTitle)
	{
		// sanity check
		if( externalTitle == null )
		{
			throw new RuntimeException("External title cannot be null");
		}
		this.externalTitle = externalTitle;
	}

	public String getExternalUrl()
	{
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl)
	{
		this.externalUrl = externalUrl;
	}

	public String getExternalDescription()
	{
		return externalDescription;
	}

	public void setExternalDescription(String externalDescription)
	{
		this.externalDescription = externalDescription;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public static class ConnectorContentAttribute
	{
		private final String labelKey;
		private final Object value;
		private final boolean hide;

		public ConnectorContentAttribute(String labelKey, Object value, boolean hide)
		{
			this.labelKey = labelKey;
			this.value = value;
			this.hide = hide;
		}

		public ConnectorContentAttribute(String labelKey, Object value)
		{
			this(labelKey, value, false);
		}

		public String getLabelKey()
		{
			return labelKey;
		}

		public Object getValue()
		{
			return value;
		}

		public boolean isHide()
		{
			return hide;
		}
	}
}
