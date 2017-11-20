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
import java.util.Collection;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.SubItemFile;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

@SuppressWarnings("nls")
@Bind
@Singleton
public class LargeImageViewer extends AbstractResourceViewer
{
	private static final Logger LOGGER = Logger.getLogger(LargeImageViewer.class);
	private static final String TILES_FOLDER = "_TILES";

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private ImageTiler imageTiler;

	@Override
	public String getViewerId()
	{
		return LargeImageViewerConstants.VIEWER_ID;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return LargeImageViewerSection.class;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		if( resource.isExternalResource() )
		{
			return false;
		}
		String mimeType = resource.getMimeType();
		return mimeType.startsWith(LargeImageViewerConstants.SUPPORTED_MIME_MATCH);
	}

	public void startTileProcessor(final Collection<Pair<File, File>> images)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					imageTiler.tile(images);
				}
				catch( Exception e )
				{
					LOGGER.error("Error while tiling images", e);
				}
			}
		}.start();
	}

	public void rotateImage(final File srcImage, final File destImage, final int angle) throws Exception
	{
		imageTiler.rotate(srcImage, destImage, angle);
	}

	/**
	 * @param tilesFolder
	 * @return Can return null if the previous tiling never finised without
	 *         error (or no tiling has ever occurred)
	 */
	public Properties getTileProperties(File tilesFolder)
	{
		return imageTiler.readTilePropertiesFile(tilesFolder);
	}

	/**
	 * If a tiling fails it gets written to a text file. if this file exists the
	 * contents of the file are returned, otherwise null is returned.
	 * 
	 * @param tilesFolder
	 * @return
	 */
	public String getError(File tilesFolder)
	{
		return imageTiler.getError(tilesFolder);
	}

	public FileHandle getTileBaseHandle(FileHandle base, String filepath)
	{
		if( base instanceof StagingFile )
		{
			return new SubTemporaryFile((StagingFile) base, getTileBasePath(filepath));
		}
		return new SubItemFile((ItemFile) base, getTileBasePath(filepath));
	}

	public FileHandle getTileBaseHandle(ViewableItem resource, String filepath)
	{
		return getTileBaseHandle(resource.getFileHandle(), filepath);
	}

	/**
	 * Package protected
	 * 
	 * @param filename
	 * @return
	 */
	String getTileBasePath(String filename)
	{
		return PathUtils.filePath(TILES_FOLDER, filename);
	}

	@Override
	public ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource)
	{
		ViewItemUrl viewerUrl = resource.createDefaultViewerUrl();
		viewerUrl.setViewer(getViewerId());

		// need to include selection session info (see Redmine #4290)
		viewerUrl.addFlag(ViewItemUrl.FLAG_IGNORE_SESSION_TEMPLATE);
		viewerUrl.removeFlag(ViewItemUrl.FLAG_IS_RESOURCE);

		return viewerUrl;
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		LargeImageViewerConfigDialog cd = componentFactory.createComponent(parentId, "livcd", tree,
			LargeImageViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}
}
