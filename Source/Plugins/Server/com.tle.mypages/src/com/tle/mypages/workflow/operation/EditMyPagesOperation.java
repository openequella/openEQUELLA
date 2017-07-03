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

package com.tle.mypages.workflow.operation;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.service.MyContentFields;
import com.tle.mypages.service.MyPagesService;

public class EditMyPagesOperation extends AbstractWorkflowOperation
{
	private final InputStream inputStream;
	private final String filename;
	private final boolean removeExistingAttachments;
	private final boolean useExistingAttachment;
	private final MyContentFields fields;

	@Inject
	private MyPagesService myPagesService;

	@AssistedInject
	private EditMyPagesOperation(@Assisted MyContentFields fields, @Assisted String filename,
		@Assisted @Nullable InputStream inputStream, @Assisted("remove") boolean removeExistingAttachments,
		@Assisted("use") boolean useExistingAttachment)
	{
		this.fields = fields;

		this.inputStream = inputStream;
		this.filename = filename;
		this.removeExistingAttachments = removeExistingAttachments;
		this.useExistingAttachment = useExistingAttachment;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		if( item.isNewItem() )
		{
			item.setDateCreated(new Date());
			item.setOwner(CurrentUser.getUserID());
		}
		item.setDateModified(new Date());

		PropBagEx itemxml = getItemXml();
		itemxml.setNode(MyContentConstants.NAME_NODE, fields.getTitle());
		itemxml.setNode(MyContentConstants.KEYWORDS_NODE, fields.getTags());
		itemxml.setNode(MyContentConstants.CONTENT_TYPE_NODE, fields.getResourceId());
		itemService.executeExtensionOperationsLater(params, "edit"); //$NON-NLS-1$

		FileHandle staging = getStaging();
		ModifiableAttachments attachments = new ModifiableAttachments(getItem().getAttachments());
		if( removeExistingAttachments )
		{
			Iterator<FileAttachment> iter = attachments.getIterator(AttachmentType.HTML);
			while( iter.hasNext() )
			{
				FileAttachment fileAttachment = iter.next();
				fileSystemService.removeFile(staging, fileAttachment.getFilename());
				iter.remove();
			}
		}

		String oldFilename = null;
		final HtmlAttachment htmlAttachment;
		if( useExistingAttachment )
		{
			Iterator<HtmlAttachment> iter = attachments.getIterator(AttachmentType.HTML);
			if( iter.hasNext() )
			{
				htmlAttachment = iter.next();
				oldFilename = htmlAttachment.getFilename();
			}
			else
			{
				htmlAttachment = new HtmlAttachment();
			}
		}
		else
		{
			htmlAttachment = new HtmlAttachment();
		}
		htmlAttachment.setFilename(filename);
		htmlAttachment.setDescription(filename);
		final String draftFilename = htmlAttachment.getFilename();
		if( inputStream != null )
		{
			myPagesService.saveHtml(staging, draftFilename, convertStreamToString(inputStream));

			if( oldFilename == null )
			{
				attachments.addAttachment(htmlAttachment);
			}
			else if( !oldFilename.equals(filename) )
			{
				fileSystemService.removeFile(staging, oldFilename);
			}
		}
		else if( oldFilename != null && !oldFilename.equals(filename) )
		{
			fileSystemService.move(staging, oldFilename, filename);
		}

		return true;
	}

	private String convertStreamToString(java.io.InputStream is)
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
