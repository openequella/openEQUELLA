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

package com.tle.beans.item.attachments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@DiscriminatorValue("file")
public class FileAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.FILE;
	}

	/**
	 * Equivilent to <code>getUrl</code>.
	 */
	public String getFilename()
	{
		return getUrl();
	}

	/**
	 * Equivilent to <code>setUrl</code>.
	 */
	public void setFilename(String filename)
	{
		setUrl(filename);
	}

	public long getSize()
	{
		return getLongValue(value1, 0L);
	}

	public void setSize(long size)
	{
		value1 = String.valueOf(size);
	}

	public boolean isConversion()
	{
		return getBooleanValue(value2);
	}

	public void setConversion(boolean conversion)
	{
		value2 = String.valueOf(conversion);
	}

	@Override
	public void setThumbnail(String thumbnail)
	{
		value3 = null;
		super.setThumbnail(thumbnail);
	}

	@Override
	public String getThumbnail()
	{
		if( thumbnail == null )
		{
			return value3;
		}
		return super.getThumbnail();
	}

	@Override
	public String toString()
	{
		return getFilename();
	}
}
