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

import com.tle.common.Check;

@Entity
@AccessType("field")
@DiscriminatorValue("ims")
@SuppressWarnings("nls")
public class ImsAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;
	private static final String KEY_EXPAND_IMS_PACKAGE = "EXPAND_IMS_PACKAGE";

	public ImsAttachment()
	{
		super();
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.IMS;
	}

	public long getSize()
	{
		return getLongValue(value1, 0L);
	}

	public void setSize(long size)
	{
		value1 = String.valueOf(size);
	}

	public String getScormVersion()
	{
		return value2;
	}

	public void setScormVersion(String scorm)
	{
		value2 = scorm;
	}

	public void setExpand(boolean expand)
	{
		setData(KEY_EXPAND_IMS_PACKAGE, expand);
	}

	public boolean isExpand()
	{
		Object expand = getData(KEY_EXPAND_IMS_PACKAGE);
		if( expand == null || ((Boolean) expand) )
		{
			return true;
		}

		return false;
	}

	public boolean isScorm()
	{
		return !Check.isEmpty(getScormVersion());
	}
}
