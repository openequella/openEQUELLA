/*
 * Created on 27/03/2006
 */
package com.tle.core.workflow.video;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

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
