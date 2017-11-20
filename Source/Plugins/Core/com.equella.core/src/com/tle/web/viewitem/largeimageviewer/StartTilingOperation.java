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

package com.tle.web.viewitem.largeimageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Pair;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;

/**
 * @author Aaron
 */
@Bind
public class StartTilingOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private LargeImageViewer largeImageViewer;
	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public boolean execute()
	{
		params.addAfterCommitHook(ItemOperationParams.COMMIT_HOOK_PRIORITY_LOW, new Runnable()
		{
			@Override
			public void run()
			{
				final Item item = getItem();
				final Collection<Pair<File, File>> images = new ArrayList<Pair<File, File>>();
				final Iterator<Attachment> it = getAttachments().getIterator(AttachmentType.FILE);
				while( it.hasNext() )
				{
					final FileAttachment fa = (FileAttachment) it.next();
					if( isViewerEnabledForAttachment(fa) )
					{
						final ItemFile itemFile = itemFileService.getItemFile(item);
						final File originalImage = fileSystemService.getExternalFile(itemFile, fa.getFilename());
						final File destFolder = fileSystemService
							.getExternalFile(largeImageViewer.getTileBaseHandle(itemFile, fa.getUrl()), null);
						images.add(new Pair<File, File>(originalImage, destFolder));
					}
				}
				if( !images.isEmpty() )
				{
					largeImageViewer.startTileProcessor(images);
				}
			}
		});

		return false;
	}

	private boolean isViewerEnabledForAttachment(FileAttachment fa)
	{
		final MimeEntry entry = mimeTypeService.getEntryForFilename(fa.getFilename());
		if( entry != null )
		{
			final List<String> enabledList = new ArrayList<String>(
				mimeTypeService.getListFromAttribute(entry, MimeTypeConstants.KEY_ENABLED_VIEWERS, String.class));
			return enabledList.contains(LargeImageViewerConstants.VIEWER_ID);
		}
		return false;
	}
}
