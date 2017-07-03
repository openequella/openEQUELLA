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

package com.tle.web.manualdatafixes.fixes;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;
import com.tle.core.workflow.video.VideoService;

@SuppressWarnings("nls")
public class GenerateThumbnailOperation extends AbstractWorkflowOperation
{
	private static final Logger LOGGER = Logger.getLogger(GenerateThumbnailOperation.class);

	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private VideoService videoService;

	private final boolean forceUpdate;

	@AssistedInject
	public GenerateThumbnailOperation(@Assisted boolean forceUpdate)
	{
		this.forceUpdate = forceUpdate;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		int vidCtr = 0;
		final List<FileAttachment> files = getAttachments().getList(AttachmentType.FILE);
		for( FileAttachment attachment : files )
		{
			if( !"suppress".equals(attachment.getThumbnail()) )
			{
				String filename = attachment.getUrl();
				final ItemFile itemFile = itemFileService.getItemFile(item);
				attachment.setThumbnail(
					thumbnailService.submitThumbnailRequest(item.getItemId(), itemFile, filename, forceUpdate, false));

				if( videoService.canConvertVideo(filename) )
				{
					if( forceUpdate || !videoService.videoPreviewExists(itemFile, filename) )
					{
						if( videoService.makeGalleryVideoPreviews(itemFile, filename) )
						{
							vidCtr++;
						}
					}
				}
			}
		}
		if( item.getThumb().equals("initial") )
		{
			item.setThumb("default");
		}

		if( vidCtr > 0 )
		{
			LOGGER.info("Generated thumbnails and " + vidCtr + " video previews for item: " + item.getItemId());
		}
		else
		{
			LOGGER.info("Generated thumbnails for item: " + item.getItemId());
		}
		return true;
	}
}
