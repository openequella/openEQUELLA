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
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.common.PathUtils;

/*
 * @author aholland
 */
@SuppressWarnings("nls")
@Entity
@AccessType("field")
@DiscriminatorValue("html")
public class HtmlAttachment extends FileAttachment
{
	private static final long serialVersionUID = 1L;

	public static final String FILENAME = "page.html";
	public static final String DRAFT_FOLDER = "_mypages_draft";
	public static final String COMMITTED_FOLDER = "_mypages";

	private static final String PARENT_FOLDER_KEY = "parent";

	@Transient
	@XStreamOmitField
	private boolean isDraft;
	@Transient
	@XStreamOmitField
	private boolean isDelete;
	@Transient
	@XStreamOmitField
	private boolean isNew;

	public HtmlAttachment()
	{
		this("");
	}

	public HtmlAttachment(String description)
	{
		super();
		this.description = description;
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.HTML;
	}

	@Override
	public String getFilename()
	{
		return PathUtils.filePath(getFolder(), FILENAME);
	}

	public String getFolder()
	{
		if( isDraft() )
		{
			return getDraftFolder();
		}
		return getNormalFolder();
	}

	public String getDraftFolder()
	{
		return PathUtils.filePath(getParentFolder(), DRAFT_FOLDER, getUuid());
	}

	public String getNormalFolder()
	{
		return PathUtils.filePath(getParentFolder(), COMMITTED_FOLDER, getUuid());
	}

	public boolean isDraft()
	{
		return isDraft;
	}

	public void setDraft(boolean isDraft)
	{
		this.isDraft = isDraft;
	}

	public boolean isDelete()
	{
		return isDelete;
	}

	public void setDelete(boolean isDelete)
	{
		this.isDelete = isDelete;
	}

	public boolean isNew()
	{
		return isNew;
	}

	public void setNew(boolean isNew)
	{
		this.isNew = isNew;
	}

	public void setParentFolder(String parent)
	{
		setData(PARENT_FOLDER_KEY, parent);
	}

	public String getParentFolder()
	{
		return (String) getData(PARENT_FOLDER_KEY);
	}
}
