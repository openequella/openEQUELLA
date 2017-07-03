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

package com.tle.web.viewitem.conversion;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.ConversionFile;
import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.imagemagick.ThumbnailOptions;
import com.tle.core.services.FileSystemService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

/**
 * TODO: should be probably call off to extension points, eg. a
 * com.tle.web.imagemagick plugin. Granted, ImageMagick is a mandatory plugin,
 * but may not be in future and we should be able to drop in plugins with an
 * ImageConverter interface...
 * 
 * @author aholland
 */
@Bind
@Singleton
public class ConvertToImageViewer extends AbstractResourceViewer implements ViewItemViewer
{
	private static final String TARGET_EXTENSION = ".jpeg"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final Set<String> UNREQUIRED_IMAGE_TYPES = new HashSet<String>(Arrays.asList(new String[]{
			"image/jpeg", "image/png", "image/gif"}));

	@Inject
	private ImageMagickService imageMagickService;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public String getViewerId()
	{
		return "toimg"; //$NON-NLS-1$
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return null;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource)
	{
		return this;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final String resultFilename = resource.getFilenameWithoutPath() + TARGET_EXTENSION;
		final ViewableItem viewableItem = resource.getViewableItem();
		final FileHandle fileHandle = viewableItem.getFileHandle();
		final ConversionFile conversionFile = new ConversionFile(fileHandle);
		final String convertedFile = PathUtils.filePath(conversionFile.getMyPathComponent(), resultFilename);

		if( !fileSystemService.fileExists(conversionFile, resultFilename) )
		{
			File dest = fileSystemService.getExternalFile(conversionFile, resultFilename);

			ThumbnailOptions thumbnailOptions = new ThumbnailOptions();
			thumbnailOptions.setNoSize(true);
			thumbnailOptions.setSkipBlankCheck(true);
			imageMagickService.generateThumbnailAdvanced(
				fileSystemService.getExternalFile(fileHandle, resource.getFilepath()), dest, thumbnailOptions);
		}
		info.forwardToUrl(viewableItem.createStableResourceUrl(convertedFile).getHref());
		return null;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		String mimeType = resource.getMimeType();
		if( imageMagickService.supported(mimeType) && !UNREQUIRED_IMAGE_TYPES.contains(mimeType) )
		{
			IAttachment attach = resource.getAttachment();
			if( attach != null )
			{
				if( attach instanceof FileAttachment )
				{
					return ((FileAttachment) attach).isConversion();
				}
			}
			return true;
		}
		return false;
	}
}
