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

package com.tle.core.item.serializer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemAttachmentListener;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;
import com.tle.core.workflow.video.VideoService;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class StandardAttachmentListener implements ItemAttachmentListener
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private VideoService videoService;

	@Override
	public void attachmentsChanged(ItemEditor editor, Item item, FileHandle fileHandle)
	{
		// FIXME: Copy pasta hack from CreateThumbnailOperation
		if( fileHandle == null )
		{
			return;
		}

		UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);
		List<FileAttachment> files = attachments.getList(AttachmentType.FILE);

		for( FileAttachment attachment : files )
		{
			String thumbnail = attachment.getThumbnail();
			if( Check.isEmpty(thumbnail) || !fileSystemService.fileExists(fileHandle, thumbnail) )
			{
				final String filename = attachment.getFilename();
				if( !"suppress".equals(attachment.getThumbnail()) )
				{
					attachment.setThumbnail(thumbnailService.submitThumbnailRequest(item.getItemId(), fileHandle,
						filename, true, true));
					if( videoService.canConvertVideo(filename) )
					{
						videoService.makeGalleryVideoPreviews(fileHandle, filename);
					}
				}
			}
		}
	}
}
