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

package com.tle.mycontent.workflow.operations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.service.MyContentFields;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class EditMyContentOperation extends AbstractWorkflowOperation // NOSONAR
{
	private final InputStream inputStream;
	private final String filename;
	private final boolean removeExistingAttachments;
	private final boolean useExistingAttachment;
	private final MyContentFields fields;
	private final String stagingUuid;

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private MimeTypeService mimeService;

	@AssistedInject
	private EditMyContentOperation(@Assisted MyContentFields fields, @Assisted @Nullable String filename,
		@Assisted @Nullable InputStream inputStream, @Assisted("staginguuid") @Nullable String stagingUuid,
		@Assisted("remove") boolean removeExistingAttachments, @Assisted("use") boolean useExistingAttachment)
	{
		this.fields = fields;
		this.inputStream = inputStream;
		this.filename = filename;
		this.stagingUuid = stagingUuid;
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
		itemService.executeExtensionOperationsLater(params, "edit"); //$NON-NLS-1$]
		if( stagingUuid != null )
		{
			getItemPack().setStagingID(stagingUuid);
		}
		FileHandle staging = getStaging();
		try
		{
			ModifiableAttachments attachments = new ModifiableAttachments(getItem());
			if( removeExistingAttachments )
			{
				Iterator<FileAttachment> iter = attachments.getIterator(AttachmentType.FILE);
				while( iter.hasNext() )
				{
					FileAttachment fileAttachment = iter.next();
					fileSystemService.removeFile(staging, fileAttachment.getFilename());
					iter.remove();
				}
			}

			String oldFilename = null;
			final FileAttachment fattach;
			if( useExistingAttachment )
			{
				Iterator<FileAttachment> iter = attachments.getIterator(AttachmentType.FILE);
				if( iter.hasNext() )
				{
					fattach = iter.next();
					oldFilename = fattach.getFilename();
				}
				else
				{
					fattach = new FileAttachment();
				}
			}
			else
			{
				fattach = new FileAttachment();
			}

			if( filename != null )
			{
				fattach.setFilename(filename);
				fattach.setDescription(filename);
			}

			if( inputStream != null )
			{
				fileSystemService.write(staging, filename, inputStream, false);
				inputStream.close();
				fattach.setSize(fileSystemService.fileLength(staging, filename));
				final String thumbnail = thumbnailService.submitThumbnailRequest(getItemId(), staging, filename, false, true);
				if( thumbnail != null ) {
					fattach.setThumbnail(thumbnail);
				}

				if( oldFilename == null )
				{
					attachments.addAttachment(fattach);
				}
				else if( !oldFilename.equals(filename) )
				{
					fileSystemService.removeFile(staging, oldFilename);
				}
			}
			else if( oldFilename != null && !oldFilename.equals(filename) && filename != null )
			{
				fileSystemService.move(staging, oldFilename, filename);
			}

			if( stagingUuid != null )
			{
				final ItemFile itemFile = itemFileService.getItemFile(item);
				fileSystemService.saveFiles(getStaging(), itemFile);
				attachments.addAttachment(fattach);
			}
			return true;
		}
		catch( IOException e )
		{
			throw new WorkflowException(e);
		}
	}
}
