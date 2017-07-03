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

package com.tle.web.selection.contribute.workflow.operations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.entity.Schema;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.selection.contribute.SelectionHomeContributePortalSection.ContentFields;

public class EditQuickUploadContentOperation extends AbstractStandardWorkflowOperation
{
	private final InputStream inputStream;
	private final String filename;
	private final ContentFields fields;

	@Inject
	private ImageMagickService imageMagickService;
	@Inject
	private MimeTypeService mimeService;

	public EditQuickUploadContentOperation(ContentFields fields, String filename, InputStream inputStream)
	{
		this.fields = fields;
		this.inputStream = inputStream;
		this.filename = filename;
	}

	@Override
	public boolean execute()
	{
		final Item item = getItem();
		if( item.isNewItem() )
		{
			item.setDateCreated(new Date());
			item.setOwner(CurrentUser.getUserID());
		}
		item.setDateModified(new Date());

		final Schema schema = getSchema();
		final PropBagEx itemxml = getItemXml();
		itemxml.setNode(schema.getItemNamePath(), fields.getTitle());
		itemxml.setNode(schema.getItemDescriptionPath(), fields.getDescription());

		itemService.executeExtensionOperationsLater(params, "edit"); //$NON-NLS-1$

		FileAttachment fattach = new FileAttachment();
		fattach.setFilename(filename);
		fattach.setDescription(filename);
		FileHandle staging = getStaging();
		try
		{
			ModifiableAttachments attachments = new ModifiableAttachments(getItem());
			if( inputStream != null )
			{
				FileInfo finfo = fileSystemService.write(staging, filename, inputStream, false);
				fattach.setSize(fileSystemService.fileLength(staging, filename));
				fattach.setMd5sum(finfo.getMd5CheckSum());
				generateThumbnail(fattach, staging, filename);
				attachments.addAttachment(fattach);
			}
		}
		catch( IOException e )
		{
			throw new WorkflowException(e);
		}
		return true;
	}

	private void generateThumbnail(FileAttachment fattach, FileHandle staging, String attachFilename)
	{
		if( imageMagickService.supported(getMimeType(attachFilename)) )
		{
			File originalImage = fileSystemService.getExternalFile(staging, attachFilename);
			String thumbFilename = FileSystemService.THUMBS_FOLDER + '/' + attachFilename
				+ FileSystemService.THUMBNAIL_EXTENSION;
			File destImage = fileSystemService.getExternalFile(staging, thumbFilename);
			imageMagickService.generateStandardThumbnail(originalImage, destImage); //$NON-NLS-1$
			fattach.setThumbnail(thumbFilename);
		}
	}

	private String getMimeType(String attachFilename)
	{
		return mimeService.getMimeTypeForFilename(attachFilename);
	}
}
