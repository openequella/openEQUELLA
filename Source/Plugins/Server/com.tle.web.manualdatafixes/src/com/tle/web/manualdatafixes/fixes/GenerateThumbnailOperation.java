package com.tle.web.manualdatafixes.fixes;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;
import com.tle.core.workflow.video.VideoService;

@SuppressWarnings("nls")
public class GenerateThumbnailOperation extends AbstractWorkflowOperation
{
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
				final ItemFile itemFile = new ItemFile(item);
				attachment.setThumbnail(thumbnailService.submitThumbnailRequest(item.getItemId(), itemFile, filename,
					forceUpdate, false));

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
