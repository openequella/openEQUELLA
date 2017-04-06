package com.tle.core.imagemagick;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import com.tle.beans.filesystem.FileHandle;

public interface ImageMagickService
{
	boolean supported(String mimeType);

	Dimension getImageDimensions(FileHandle handle, String filename) throws IOException;

	Dimension getImageDimensions(File image) throws IOException;

	void sample(File src, File dest, String width, String height, String... options) throws IOException;

	void crop(File src, File dest, String width, String height, String... options) throws IOException;

	void rotate(File src, File dest, int angle, String... options) throws IOException;

	/**
	 * @param srcFile
	 * @param dstFile
	 * @param format Not used
	 * @param options
	 */
	void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options);

	void generateStandardThumbnail(File srcFile, File dstFile);

	void sampleNoRatio(File src, File dest, String width, String height, String... options) throws IOException;

}
