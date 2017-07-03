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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.imagemagick.ThumbnailOptions;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.thumbnail.ThumbnailGenerator;
import com.tle.core.workflow.thumbnail.ThumbnailQueueFile;
import com.tle.core.workflow.thumbnail.ThumbnailType;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;
import com.tle.core.workflow.thumbnail.service.ThumbnailRequestService;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class ThumbingCallable implements Callable<ThumbingCallableResult>
{
	private static final Logger LOGGER = Logger.getLogger(ThumbingCallable.class);

	@Inject
	private RunAsInstitution runAs;
	@Inject
	private ThumbnailRequestService thumbnailRequestService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private StagingService stagingService;
	@Inject
	private PluginTracker<ThumbnailGenerator> thumbnailTracker;

	private final Institution institution;
	private final String requestUuid;
	private final ItemId itemId;
	private final String serialHandle;

	@AssistedInject
	protected ThumbingCallable(@Assisted Institution institution, @Assisted("requestUuid") String requestUuid,
		@Assisted ItemId itemId, @Assisted("serialHandle") String serialHandle)
	{
		this.institution = institution;
		this.requestUuid = requestUuid;
		this.itemId = itemId;
		this.serialHandle = serialHandle;
	}

	@Override
	public ThumbingCallableResult call()
	{
		try
		{
			return runAs.executeAsSystem(institution, new Callable<ThumbingCallableResult>()
			{
				@Override
				public ThumbingCallableResult call()
				{
					return makeThumbs();
				}
			});
		}
		catch( Exception e )
		{
			LOGGER.error("Error in ThumbingRunnable", e);
			throw e;
		}
	}

	@Transactional
	public ThumbingCallableResult makeThumbs()
	{
		final ThumbnailRequest thumbnailRequest = thumbnailRequestService.getByUuid(requestUuid);
		final ThumbingCallableResult result = new ThumbingCallableResult(thumbnailRequest, CurrentInstitution.get(),
			requestUuid, itemId, serialHandle);
		try
		{
			if( Thread.interrupted() )
			{
				throw new InterruptedException();
			}

			if( thumbnailRequest == null )
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Thumb request " + requestUuid + " no longer available");
				}
				return result;
			}

			final String filename = thumbnailRequest.getFilename();
			final ItemId itemId = new ItemId(thumbnailRequest.getItemUuid(), thumbnailRequest.getItemVersion());
			LOGGER.info("Starting thumbnail generation for " + thumbnailRequest.toString());

			final FileHandle destHandle = deserialiseHandle(thumbnailRequest.getHandle());
			final ThumbnailQueueFile thumbQueueFile = new ThumbnailQueueFile(requestUuid);
			final ThumbnailGenerator thumbGen = getThumbnailGenerator(filename);

			final String thumbFile = PathUtils.filePath(FileSystemService.THUMBS_FOLDER,
				filename + FileSystemService.THUMBNAIL_EXTENSION);
			final String galleryThumb = PathUtils.filePath(FileSystemService.THUMBS_FOLDER,
				filename + FileSystemService.GALLERY_THUMBNAIL_EXTENSION);
			final String galleryPreview = PathUtils.filePath(FileSystemService.THUMBS_FOLDER,
				filename + FileSystemService.GALLERY_PREVIEW_EXTENSION);

			if( Thread.interrupted() )
			{
				throw new InterruptedException();
			}
			final int flags = thumbnailRequest.getThumbnailTypes();
			if( ThumbnailType.TYPE_GALLERY_PREVIEW.enabled(flags) )
			{
				final Dimension dimensions = thumbGen
					.getImageDimensions(fileSystemService.getExternalFile(thumbQueueFile, filename));
				final ThumbnailOptions options;
				if( dimensions == null || (dimensions.getHeight() > 500 || dimensions.getWidth() > 500) )
				{
					options = buildGalleryOptions(500, 500);
				}
				else
				{
					options = new ThumbnailOptions();
					options.setNoSize(true);
				}
				genThumb(thumbQueueFile, result, galleryPreview, options, filename);
			}

			if( Thread.interrupted() )
			{
				throw new InterruptedException();
			}
			if( ThumbnailType.TYPE_GALLERY_THUMB.enabled(flags) )
			{
				final ThumbnailOptions options = buildGalleryOptions(135, 135);
				genThumb(thumbQueueFile, result, galleryThumb, options, galleryPreview, filename);
			}

			if( Thread.interrupted() )
			{
				throw new InterruptedException();
			}
			if( ThumbnailType.TYPE_STANDARD_THUMB.enabled(flags) )
			{
				genThumb(thumbQueueFile, result, thumbFile, null, galleryPreview, galleryThumb, filename);
			}

			// Thumbs finished. Copy the produced ones to the destination
			for( String prodThumb : result.getBuiltThumbnails() )
			{
				if( Thread.interrupted() )
				{
					throw new InterruptedException();
				}

				// Copy completed thumbs to the destination, which could be the staging if it still exists
				// This is re-evaluated each time because it may change disappear at any time (ie item is saved)
				final FileHandle realDestHandle;
				if( destHandle instanceof StagingFile
					&& stagingService.stagingExists(((StagingFile) destHandle).getUuid()) )
				{
					// Make sure the file area is there too
					realDestHandle = (fileSystemService.fileExists(destHandle) ? destHandle
						: itemFileService.getItemFile(itemId, null));
				}
				else
				{
					realDestHandle = itemFileService.getItemFile(itemId, null);
				}
				fileSystemService.copy(thumbQueueFile, prodThumb, realDestHandle, prodThumb);
			}

			final long now = System.currentTimeMillis();
			LOGGER.info("Finished thumbnailing " + thumbnailRequest.toString() + " into "
				+ result.getBuiltThumbnails().size() + " thumbnails in " + (now - result.getStartTime()) + "ms.");
		}
		catch( Throwable ex )
		{
			final Throwable root = Throwables.getRootCause(ex);
			if( !(root instanceof InterruptedException) )
			{
				LOGGER.error("Error generating thumbnail", ex);
			}
		}
		return result;
	}

	/**
	 * 
	 * @param result
	 * @param thumbGen
	 * @param destinationFilename
	 * @param options
	 * @param sources The first source that exists will be used. Ie. it uses a fallback system
	 * @throws Exception
	 */
	private void genThumb(ThumbnailQueueFile thumbQueueFile, ThumbingCallableResult result, String destinationFilename,
		@Nullable ThumbnailOptions options, String... sources) throws Exception
	{
		final File dstFile = fileSystemService.getExternalFile(thumbQueueFile, destinationFilename);

		deleteExistingFile(thumbQueueFile, dstFile);

		for( String source : sources )
		{
			if( fileSystemService.fileExists(thumbQueueFile, source) )
			{
				final ThumbnailGenerator thumbGen = getThumbnailGenerator(source);
				if( options == null )
				{
					thumbGen.generateThumbnail(fileSystemService.getExternalFile(thumbQueueFile, source), dstFile);
				}
				else
				{
					thumbGen.generateThumbnailAdvanced(fileSystemService.getExternalFile(thumbQueueFile, source),
						dstFile, options);
				}

				if( dstFile.exists() )
				{
					result.addThumbnail(destinationFilename);
				}
				return;
			}
		}
		LOGGER.warn("No sources were available for thumbnailing into " + destinationFilename);
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

	private ThumbnailOptions buildGalleryOptions(int height, int width)
	{
		ThumbnailOptions options = new ThumbnailOptions();
		options.setHeight(height);
		options.setWidth(width);
		options.setGravity("center");
		options.setKeepAspect(true);
		return options;
	}

	private void deleteExistingFile(FileHandle handle, File dstFile) throws IOException
	{
		if( dstFile.exists() )
		{
			boolean wasDeleted = dstFile.delete();
			if( !wasDeleted )
			{
				LOGGER.warn("Failed to delete " + dstFile.getAbsolutePath());
			}
		}
		else
		{
			boolean madeDirs = dstFile.getParentFile().mkdirs();
			if( !(madeDirs || dstFile.getParentFile().exists()) )
			{
				throw new IOException(
					"Could not create/confirm directory " + dstFile.getParentFile().getAbsolutePath());
			}
		}
	}

	private FileHandle deserialiseHandle(String superSerialHandle)
	{
		try
		{
			//dirty
			String[] split = superSerialHandle.split(":");
			String type = split[0];
			String id = split[1];
			if( "staging".equals(type) )
			{
				return new StagingFile(id);
			}
			else if( "item".equals(type) )
			{
				return itemFileService.getItemFile(new ItemId(id), null);
			}
			else
			{
				throw new RuntimeException("Could not deserialise handle of type " + type);
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException("Could not deserialise handle", e);
		}
	}
}