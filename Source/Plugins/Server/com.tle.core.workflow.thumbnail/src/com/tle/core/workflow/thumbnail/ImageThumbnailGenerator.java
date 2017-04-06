package com.tle.core.workflow.thumbnail;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.imagemagick.ThumbnailOptions;

@Bind
@Singleton
public class ImageThumbnailGenerator implements ThumbnailGenerator
{
	@Inject
	private ImageMagickService imageMagickService;

	@Override
	public void generateThumbnail(File src, File dest)
	{
		imageMagickService.generateStandardThumbnail(src, dest);
	}

	@Override
	public void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) throws Exception
	{
		imageMagickService.generateThumbnailAdvanced(srcFile, dstFile, options);
	}

	@Override
	public Dimension getImageDimensions(File srcFile) throws IOException
	{
		return imageMagickService.getImageDimensions(srcFile);
	}

	@Override
	public boolean supportsThumbType(ThumbnailType type)
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
