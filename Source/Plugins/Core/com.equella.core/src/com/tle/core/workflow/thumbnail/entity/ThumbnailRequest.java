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

package com.tle.core.workflow.thumbnail.entity;

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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;
import com.tle.common.PathUtils;

/**
 * @author Aaron
 *
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"institution_id", "uuid"}),
		@UniqueConstraint(columnNames = {"institution_id", "itemUuid", "itemVersion", "handle", "filenameHash"})})
public class ThumbnailRequest
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "trUuidIndex")
	private String uuid;

	@Column(nullable = false)
	private Date dateRequested;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Index(name = "trInstIndex")
	private Institution institution;

	@Column(nullable = false)
	@Index(name = "trItemUuid")
	private String itemUuid;

	@Column
	@Index(name = "trItemVersion")
	private int itemVersion;

	/**
	 * A pseudo-serialised destination FileHandle
	 */
	@Column(nullable = false, length = 255)
	@Index(name = "trHandle")
	private String handle;

	@Column(nullable = false)
	@Lob
	private String filename;

	@Column(nullable = false, length = 100)
	@Index(name = "trFilenameHash")
	private String filenameHash;

	@Column
	private boolean recreate;

	@Column(nullable = true, length = 255)
	private String globalTaskId;

	@Column(nullable = true, length = 80)
	private String taskId;

	/**
	 * A bit field.
	 */
	@Column(nullable = false)
	private int thumbnailTypes;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Date getDateRequested()
	{
		return dateRequested;
	}

	public void setDateRequested(Date dateRequested)
	{
		this.dateRequested = dateRequested;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

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

	public String getHandle()
	{
		return handle;
	}

	public void setHandle(String handle)
	{
		this.handle = handle;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilenameHash()
	{
		return filenameHash;
	}

	public void setFilenameHash(String filenameHash)
	{
		this.filenameHash = filenameHash;
	}

	public boolean isRecreate()
	{
		return recreate;
	}

	public void setRecreate(boolean recreate)
	{
		this.recreate = recreate;
	}

	public String getGlobalTaskId()
	{
		return globalTaskId;
	}

	public void setGlobalTaskId(String globalTaskId)
	{
		this.globalTaskId = globalTaskId;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public void setTaskId(String taskId)
	{
		this.taskId = taskId;
	}

	public int getThumbnailTypes()
	{
		return thumbnailTypes;
	}

	public void setThumbnailTypes(int thumbnailTypes)
	{
		this.thumbnailTypes = thumbnailTypes;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("(").append(uuid).append(") ")
			.append(PathUtils.filePath(itemUuid, Integer.toString(itemVersion), filename));
		return sb.toString();
	}
}
