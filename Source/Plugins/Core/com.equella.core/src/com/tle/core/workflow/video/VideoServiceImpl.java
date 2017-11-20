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

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.guice.Bind;
import com.tle.core.libav.LibAvService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.freetext.SupportedVideoMimeTypeExtension;

@Bind(VideoService.class)
@Singleton
public class VideoServiceImpl implements VideoService
{
	private static final Logger LOGGER = Logger.getLogger(VideoServiceImpl.class);

	@Inject
	private LibAvService libAvService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeTypeService;

	private PluginTracker<SupportedVideoMimeTypeExtension> supportedVideoAttachments;

	@Override
	public boolean isVideo(Attachment attachment)
	{
		String mimeType = mimeTypeService.getMimeEntryForAttachment(attachment);
		List<SupportedVideoMimeTypeExtension> supportedVideoTypes = supportedVideoAttachments.getBeanList();
		for( SupportedVideoMimeTypeExtension type : supportedVideoTypes )
		{
			if( type.isSupportedMimeType(mimeType) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canConvertVideo(String filename)
	{
		return mimeTypeService.getMimeTypeForFilename(filename).startsWith("video");
	}

	@Override
	public boolean videoPreviewExists(FileHandle handle, String filename)
	{
		final String thumbFile = FileSystemService.VIDEO_PREVIEW_FOLDER + '/' + filename
			+ FileSystemService.VIDEO_PREVIEW_EXTENSION;
		final File dstFile = fileSystemService.getExternalFile(handle, thumbFile);
		return dstFile.exists();
	}

	@Override
	public boolean makeGalleryVideoPreviews(FileHandle handle, String filename)
	{
		if( libAvService.isLibavInstalled() )
		{
			try
			{

				libAvService.generatePreviewVideo(handle, filename);
			}
			catch( Exception ex )
			{
				LOGGER.error("Error generating video preview ", ex); //$NON-NLS-1$
				return false;
			}
			return true;
		}

		// go away, libav is not even installed
		return false;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		supportedVideoAttachments = new PluginTracker<SupportedVideoMimeTypeExtension>(pluginService,
			"com.tle.core.workflow.video", "supportedVideoMimeType", null);
		supportedVideoAttachments.setBeanKey("bean");
	}
}
