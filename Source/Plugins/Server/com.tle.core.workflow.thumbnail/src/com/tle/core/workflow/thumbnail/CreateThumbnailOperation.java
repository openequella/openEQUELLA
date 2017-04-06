/*
 * Created on 27/03/2006
 */
package com.tle.core.workflow.thumbnail;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;

@SuppressWarnings("nls")
@Bind
public class CreateThumbnailOperation extends AbstractWorkflowOperation
{
	@Inject
	private ThumbnailService thumbnailService;

	@Override
	public boolean execute()
	{
		StagingFile staging = getStaging();
		if( staging == null )
		{
			return false;
		}

		boolean mod = false;
		final List<FileAttachment> files = getAttachments().getList(AttachmentType.FILE);
		final ItemId itemId = getItemId();
		for( FileAttachment attachment : files )
		{
			if( !"suppress".equals(attachment.getThumbnail()) )
			{
				final String filename = attachment.getFilename();
				boolean clearPending = false;
				//I.e. not 'Save and continue'
				final String u = params.getAttributes().get("unlock");
				if( u != null && Boolean.valueOf(u) )
				{
					clearPending = true;
				}

				final String thumbnail = thumbnailService.submitThumbnailRequest(itemId, staging, filename, false,
					clearPending);
				if( thumbnail != null )
				{
					attachment.setThumbnail(thumbnail);
					mod = true;
				}
			}
		}

		return mod;
	}
}
