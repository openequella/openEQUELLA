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
@DiscriminatorValue("zip")
public class ZipAttachment extends Attachment
{
	private static final long serialVersionUID = 1L;
	/**
	 * Files extracted from a parent zip file record the original file's
	 * attachment UUID (mainly as a flag, so as to allow zip's content to be
	 * treated differently than stand-alone files).
	 */
	public static final String KEY_ZIP_ATTACHMENT_UUID = "ZIP_ATTACHMENT_UUID"; //$NON-NLS-1$

	public ZipAttachment()
	{
		super();
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.ZIP;
	}

	public boolean isMapped()
	{
		return getBooleanValue(value1);
	}

	public void setMapped(boolean mapped)
	{
		value1 = String.valueOf(mapped);
	}

	/**
	 * For zip attachments, make use of value2 to hold boolean flag as to
	 * whether the zip file itself is attached to an item (as distinct from or
	 * as well as attaching the zip's contents). Require the user to
	 * particularly specify that they want an actual true or false
	 */
	public boolean isAttachZip()
	{
		if( Check.isEmpty(value2) )
		{
			return true;
		}
		else
		{
			return getBooleanValue(value2);
		}
	}

	public void setAttachZip(boolean attachZip)
	{
		value2 = String.valueOf(attachZip);
	}
}
