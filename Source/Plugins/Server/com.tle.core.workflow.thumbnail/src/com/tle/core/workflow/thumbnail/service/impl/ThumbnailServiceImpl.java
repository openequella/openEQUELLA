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

package com.tle.core.workflow.thumbnail.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.listener.ItemDeletedListener;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.core.workflow.thumbnail.ThumbnailGenerator;
import com.tle.core.workflow.thumbnail.ThumbnailType;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;
import com.tle.core.workflow.thumbnail.service.ThumbnailRequestService;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind(ThumbnailService.class)
@Singleton
public class ThumbnailServiceImpl implements ThumbnailService, ItemDeletedListener
{
	private static final Logger LOGGER = Logger.getLogger(ThumbnailServiceImpl.class);

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ThumbnailRequestService thumbnailRequestService;
	@Inject
	private PluginTracker<ThumbnailGenerator> thumbnailTracker;
	@Inject
	private ThumbingCallableTracker taskTracker;

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public String submitThumbnailRequest(ItemKey itemId, FileHandle handle, String filename, boolean forceIt,
		boolean clearPending)
	{
		//It can happen that the source file doesn't actually exist
		if( !fileSystemService.fileExists(handle, filename) )
		{
			LOGGER.debug("Source file " + filename + " doesn't exist, not thumbnailing");
			return null;
		}

		final ThumbnailGenerator thumbGen = getThumbnailGenerator(filename);
		if( thumbGen == null || !thumbGen.isEnabled() )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("No enabled thumbnail generator for filename " + filename);
			}
			return null;
		}

		if( thumbnailRequestService.exists(itemId, handle, filename) )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Thumbnail generation for: " + filename + " is already queued.");
			}
		}
		else
		{
			//determine what we are thumbnailing
			int thumbFlags = 0;
			for( ThumbnailType thumbType : ThumbnailType.values() )
			{
				if( thumbGen.supportsThumbType(thumbType)
					&& (forceIt || !fileSystemService.fileExists(handle, thumbFilename(filename, thumbType))) )
				{
					thumbFlags |= thumbType.getValue();
				}
			}
			if( thumbFlags == 0 )
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Not required to thumbnail: " + filename);
				}
			}
			else
			{
				thumbnailRequestService.newRequest(filename, itemId, handle, thumbFlags, forceIt, clearPending);
			}
		}

		return thumbFilename(filename, ThumbnailType.TYPE_STANDARD_THUMB);
	}

	@Transactional
	@Override
	public void cancelRequests(ItemKey itemId, FileHandle handle)
	{
		LOGGER.trace("Cancelling new thumbnail requests");
		for( ThumbnailRequest request : thumbnailRequestService.listForHandle(itemId, handle) )
		{
			deleteRequest(request);
		}
	}

	private String thumbFilename(String filename, ThumbnailType thumbType)
	{
		switch( thumbType )
		{
			case TYPE_GALLERY_THUMB:
				return PathUtils.filePath(FileSystemService.THUMBS_FOLDER,
					filename + FileSystemService.GALLERY_THUMBNAIL_EXTENSION);

			case TYPE_GALLERY_PREVIEW:
				return PathUtils.filePath(FileSystemService.THUMBS_FOLDER,
					filename + FileSystemService.GALLERY_PREVIEW_EXTENSION);

			case TYPE_STANDARD_THUMB:
			default:
				return PathUtils.filePath(FileSystemService.THUMBS_FOLDER,
					filename + FileSystemService.THUMBNAIL_EXTENSION);
		}
	}

	private ThumbnailGenerator getThumbnailGenerator(String filename)
	{
		final String mimeType = mimeTypeService.getMimeTypeForFilename(filename);

		Extension ext = thumbnailTracker.getExtension(mimeType);
		if( ext == null )
		{
			ext = thumbnailTracker.getExtension(mimeType.substring(0, mimeType.indexOf('/') + 1));
		}

		return ext != null ? thumbnailTracker.getBeanByExtension(ext) : null;
	}

	/**
	 * Factory method to spawn a task to do a single thumbnail
	 * 
	 * @param requestUuid
	 * @param institutionId
	 * @return
	 */
	public Task createThumbnailerTask(String requestUuid, long institutionId, ItemId itemId, String serialHandle)
	{
		return new SingleThumbnailTask(requestUuid, institutionId, itemId, serialHandle);
	}

	@Transactional
	@Override
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		final ItemIdKey itemId = event.getItemId();

		// Delete all thumb requests for this item
		for( ThumbnailRequest request : thumbnailRequestService.list(CurrentInstitution.get(), itemId) )
		{
			deleteRequest(request);
		}
	}

	@Transactional
	private void deleteRequest(ThumbnailRequest request)
	{
		//TODO: kill tasks?  They should realise it's not there anymore anyway...
		//taskTracker.killTasks(request);
		thumbnailRequestService.delete(request.getUuid());
	}

	private class SingleThumbnailTask extends SingleShotTask
	{
		private final String requestUuid;
		private final long institutionId;
		private final ItemId itemId;
		private final String serialHandle;

		public SingleThumbnailTask(String requestUuid, long institutionId, ItemId itemId, String serialHandle)
		{
			this.requestUuid = requestUuid;
			this.institutionId = institutionId;
			this.itemId = itemId;
			this.serialHandle = serialHandle;
		}

		@Override
		public void runTask()
		{
			// Wait for institution. 
			// Even though this task (and all tasks in general) wait for the institution keeper task, 
			// it can still attempt to run before the institution task is fully prepared. 

			Institution institution = institutionService.getInstitution(institutionId);
			int tries = 0;
			int backoff = 1000;
			while( institution == null && tries < 10 )
			{
				try
				{
					Thread.sleep(backoff);
					institution = institutionService.getInstitution(institutionId);
					tries++;
					backoff += backoff;
				}
				catch( InterruptedException ie )
				{
					//whatevs
				}
			}
			if( institution == null )
			{
				throw new RuntimeException("Gave up waiting for institution");
			}

			taskTracker.submitTask(institution, requestUuid, itemId, serialHandle);
		}

		@Override
		protected String getTitleKey()
		{
			return "com.tle.core.workflow.thumbnail.task.thumbnailer.title";
		}
	}
}
