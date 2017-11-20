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

package com.tle.core.workflow.video;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.AbstractWorkflowOperation;

@Bind
public class CreateVideoPreviewOperation extends AbstractWorkflowOperation
{
	@Inject
	private VideoService videoService;

	@Override
	public boolean execute()
	{
		StagingFile staging = getStaging();
		if( staging == null )
		{
			return false;
		}

		boolean mod = false;
		List<FileAttachment> files = getAttachments().getList(AttachmentType.FILE);
		for( FileAttachment attachment : files )
		{
			//String thumbnail = attachment.getThumbnail();
			final String filename = attachment.getFilename();
			if( !"suppress".equals(attachment.getThumbnail()) )
			{
				if( videoService.canConvertVideo(filename) && !videoService.videoPreviewExists(staging, filename) )
				{
					videoService.makeGalleryVideoPreviews(staging, filename);
					mod = true;
				}
			}
		}
		return mod;
	}
}
