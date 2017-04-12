package com.tle.core.workflow.thumbnail;

import java.awt.Dimension;
import java.io.File;

import com.tle.core.imagemagick.ThumbnailOptions;

public interface ThumbnailGenerator
{
	void generateThumbnail(File src, File dest) throws Exception;

	void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) throws Exception;

	Dimension getImageDimensions(File srcFile) throws Exception;

	/**
	 * 
	 * @param type
	 * @return
	 */
	boolean supportsThumbType(ThumbnailType type);

	/**
	 * E.g. video thumbnailer may be disabled because of not being installed
	 * @return
	 */
	boolean isEnabled();
}
