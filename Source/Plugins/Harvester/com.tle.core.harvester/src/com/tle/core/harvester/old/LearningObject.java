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

package com.tle.core.harvester.old;

import java.util.Date;

/**
 * Represents a Learning Object from a content repository.
 * 
 * @author Nicholas Read
 */
public class LearningObject
{
	protected String identifier;

	protected String title;
	protected Date created;
	protected Date modified;

	protected boolean attachment;
	protected boolean equellaItem;

	public LearningObject(String identifier, String title, Date created, boolean attachment)
	{
		this.identifier = identifier;
		this.title = title;
		this.created = created;
		this.attachment = attachment;
		this.equellaItem = false;
	}

	public Date getCreationDate()
	{
		return created;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getTitle()
	{
		return title;
	}

	/**
	 * @return Returns the attachments.
	 */
	public boolean hasAttachment()
	{
		return attachment;
	}

	public void setAttachment(boolean attachment)
	{
		this.attachment = attachment;
	}

	public void setModifiedDate(Date modified)
	{
		this.modified = modified;
	}

	public Date getModifiedDate()
	{
		return modified;
	}

	public boolean isEquellaItem()
	{
		return equellaItem;
	}

	public void isEquellaItem(boolean equellaItem)
	{
		this.equellaItem = equellaItem;
	}
}
