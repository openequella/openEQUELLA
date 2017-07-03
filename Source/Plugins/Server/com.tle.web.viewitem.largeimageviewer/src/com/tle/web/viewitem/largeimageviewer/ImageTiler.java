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

import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.common.io.FileUtils;
import com.tle.common.Pair;
import com.tle.common.filesystem.FileSystemHelper;
import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.web.sections.SectionsRuntimeException;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ImageTiler
{
	private static final int TILE_SIZE = 256;
	private static final String TILE_PROPERTIES_FILENAME = "info.properties";
	private static final String ERROR_FILENAME = "error.txt";
	private static final Logger LOGGER = Logger.getLogger(ImageTiler.class);
	private static final String ORIGINAL_TILE_FOLDER = "/orig/";

	@Inject
	private ImageMagickService imageMagickService;

	public void tile(Collection<Pair<File, File>> images) throws Exception
	{
		List<File> actuallyNeedTiling = new ArrayList<File>();

		for( Pair<File, File> image : images )
		{
			if( tileInit(image.getFirst(), image.getSecond()) )
			{
				actuallyNeedTiling.add(image.getSecond());
			}
		}

		Exception firstException = null;
		for( File destFolder : actuallyNeedTiling )
		{
			try
			{
				tileMain(destFolder);
			}
			catch( Exception ex )
			{
				firstException = (firstException == null ? ex : firstException);
				FileUtils.delete(getFirstTileDirectory(destFolder).toPath());

				final StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				FileSystemHelper.createFile(new File(destFolder, ERROR_FILENAME), sw.toString().getBytes());
			}
		}
		if( firstException != null )
		{
			throw firstException;
		}
	}

	public String getError(File tileFolder)
	{
		final File errorFile = new File(tileFolder, ERROR_FILENAME);
		if( errorFile.exists() )
		{
			try
			{
				return new String(Files.readAllBytes(errorFile.toPath()));
			}
			catch( IOException io )
			{
				// what are you going to do about it?
			}
		}
		return null;
	}

	private File getFirstTile(File destFolder, String originalFilename)
	{
		if( originalFilename != null )
		{
			return new File(getFirstTileDirectory(destFolder), originalFilename);
		}
		for( File onlyChild : getFirstTileDirectory(destFolder).listFiles() )
		{
			return onlyChild;
		}
		// backup
		return new File(destFolder, "0.jpg");
	}

	private File getFirstTileDirectory(File destFolder)
	{
		return new File(destFolder, ORIGINAL_TILE_FOLDER);
	}

	/**
	 * We split the work up so that we can 'start' tiling as many images as the
	 * contributor wants, and the viewer will see that an image is
	 * "currently being processed".
	 * 
	 * @See com.tle.web.plugin.image.LargeImageViewer.view(SectionContext
	 *      context)
	 */
	private boolean tileInit(File originalImage, File destFolder) throws IOException
	{
		File image = getFirstTile(destFolder, originalImage.getName());
		if( image.exists() && image.lastModified() >= originalImage.lastModified()
			&& getTilePropertiesFile(destFolder).exists() )
		{
			// Original image is older than the tiles (and last tiling completed
			// successfully) -> do nothing.
			return false;
		}

		FileUtils.delete(destFolder.toPath());
		boolean madeDirs = destFolder.mkdirs();
		if( !madeDirs && !destFolder.exists() )
		{
			throw new IOException("Could not create directory " + destFolder.getAbsolutePath());
		}

		copyFile(originalImage, image);

		return true;
	}

	private void tileMain(File destFolder) throws IOException
	{
		int zoomLevel = 0;

		File image = getFirstTile(destFolder, null);
		Dimension imageSize = imageMagickService.getImageDimensions(image);

		StringBuilder geometry = new StringBuilder();

		boolean first = true;
		while( first || imageSize.width > TILE_SIZE || imageSize.height > TILE_SIZE )
		{
			first = false;
			final int rowCount = (int) Math.ceil(imageSize.getHeight() / TILE_SIZE);
			final int colCount = (int) Math.ceil(imageSize.getWidth() / TILE_SIZE);

			if( geometry.length() > 0 )
			{
				geometry.append(',');
			}
			geometry.append('[');
			geometry.append(colCount);
			geometry.append(',');
			geometry.append(rowCount);
			geometry.append(']');

			for( int row = 0; row < rowCount; row++ )
			{
				File stripImg = createRowStrip(image, imageSize.width, zoomLevel, row, destFolder);
				createTiles(stripImg, zoomLevel, row, destFolder);
				boolean wasDeleted = stripImg.delete();
				if( !wasDeleted )
				{
					LOGGER.warn("Apparent failure to delete " + stripImg.getAbsolutePath());
				}
			}

			zoomLevel++;
			File nextImage = new File(destFolder, zoomLevel + ".jpg");
			halveTheSize(image, nextImage);

			image = nextImage;
			imageSize = imageMagickService.getImageDimensions(image);
		}

		// We want to keep the image at the current zoom level as the thumbnail
		boolean wasRenamed = image.renameTo(new File(destFolder, "thumbnail.jpg"));
		// We haven't thrown an error on this point before, but it's always
		// helpful to leave a fingerprint in the logs, if nothing else?
		if( !wasRenamed )
		{
			LOGGER.warn("Apparent failure to rename " + image.getAbsolutePath() + " to " + destFolder.getAbsolutePath()
				+ ", thumbnail.jpg");
		}
		zoomLevel--;

		for( int i = 0; i <= zoomLevel; i++ )
		{
			new File(destFolder, i + ".jpg").delete();
		}

		try( Writer writer = new FileWriter(getTilePropertiesFile(destFolder)) )
		{
			Properties props = new Properties();
			props.setProperty("geometry", '[' + geometry.toString() + ']');
			props.store(writer, null);
		}

		FileUtils.delete(getFirstTileDirectory(destFolder).toPath());
	}

	private File getTilePropertiesFile(File tilesFolder)
	{
		return new File(tilesFolder, TILE_PROPERTIES_FILENAME);
	}

	public Properties readTilePropertiesFile(File tilesFolder)
	{
		Properties props = null;
		File tilePropertiesFile = getTilePropertiesFile(tilesFolder);
		if( tilePropertiesFile.exists() )
		{
			try( FileReader in = new FileReader(tilePropertiesFile) )
			{
				props = new Properties();
				props.load(in);
			}
			catch( IOException ioe )
			{
				throw new SectionsRuntimeException(ioe);
			}
		}
		return props;
	}

	private void createTiles(final File src, final int zoomLevel, final int row, final File basePath) throws IOException
	{
		File result = new File(basePath, zoomLevel + "_" + row + "_%d.jpg");
		imageMagickService.crop(src, result, "256", "0", "+repage");
	}

	public void rotate(final File srcImage, final File destImage, final int angle) throws IOException
	{
		imageMagickService.rotate(srcImage, destImage, angle);
	}

	private File createRowStrip(final File src, int imageWidth, final int zoomLevel, final int row, final File basePath)
		throws IOException
	{
		int mustBeWidth = imageWidth + (TILE_SIZE - (imageWidth % TILE_SIZE));
		File result = new File(basePath, zoomLevel + "_" + row + ".jpg");
		imageMagickService.crop(src, result, "0", "256+0+" + (row * 256), "+repage", "-background", "black", "-extent",
			mustBeWidth + "x256");
		return result;
	}

	private void halveTheSize(final File src, final File dest) throws IOException
	{
		imageMagickService.sample(src, dest, "50%", "50%");
	}

	private static void copyFile(final File src, final File dest) throws IOException
	{
		boolean madeDirs = dest.getParentFile().mkdirs();
		if( !madeDirs && !dest.getParentFile().exists() )
		{
			throw new IOException("Could not create directory " + dest.getParentFile().getAbsolutePath());
		}
		FileUtils.copy(src.toPath(), dest.toPath());
	}
}