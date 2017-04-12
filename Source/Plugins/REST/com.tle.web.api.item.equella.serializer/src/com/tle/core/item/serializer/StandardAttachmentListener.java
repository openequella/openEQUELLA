package com.tle.core.item.serializer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.filesystem.FileHandle;
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
