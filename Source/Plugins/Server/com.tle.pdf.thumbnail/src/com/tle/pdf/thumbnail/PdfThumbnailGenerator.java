package com.tle.pdf.thumbnail;

import java.awt.Dimension;
import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.imagemagick.ThumbnailOptions;
import com.tle.core.workflow.thumbnail.ThumbnailGenerator;
import com.tle.core.workflow.thumbnail.ThumbnailType;

@Bind
@Singleton
public class PdfThumbnailGenerator implements ThumbnailGenerator
{
	@Inject
	private ImageMagickService imageMagickService;

	@SuppressWarnings("nls")
	@Override
	public void generateThumbnail(File src, File dest) throws Exception
	{
		File parent = src.getParentFile();
		imageMagickService.generateStandardThumbnail(new File(parent, src.getName() + "[0]"), dest);
	}

	@Override
	public void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) throws Exception
	{
		// One size only
	}

	@Override
	public Dimension getImageDimensions(File srcFile)
	{
		return null;
	}

	@Override
	public boolean supportsThumbType(ThumbnailType type)
	{
		return type == ThumbnailType.TYPE_STANDARD_THUMB;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
