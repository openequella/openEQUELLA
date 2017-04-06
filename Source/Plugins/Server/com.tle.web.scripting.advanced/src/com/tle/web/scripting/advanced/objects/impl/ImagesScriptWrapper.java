package com.tle.web.scripting.advanced.objects.impl;

import java.awt.Dimension;
import java.io.IOException;

import com.tle.beans.filesystem.FileHandle;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.advanced.objects.ImagesScriptObject;
import com.tle.web.scripting.impl.AbstractScriptWrapper;

/**
 * @author aholland
 */
public class ImagesScriptWrapper extends AbstractScriptWrapper implements ImagesScriptObject
{
	private final ImageMagickService imageMagick;
	private final FileSystemService fileSystem;
	private final FileHandle handle;

	public ImagesScriptWrapper(ImageMagickService imageMagick, FileSystemService fileSystem, FileHandle handle)
	{
		this.imageMagick = imageMagick;
		this.fileSystem = fileSystem;
		this.handle = handle;
	}

	@Override
	public Dimension getDimensions(String path) throws IOException
	{
		return imageMagick.getImageDimensions(handle, path);
	}

	@Override
	public void resize(String path, int newWidth, int newHeight, String newPath) throws IOException
	{
		imageMagick.sample(fileSystem.getExternalFile(handle, path), fileSystem.getExternalFile(handle, newPath),
			Integer.toString(newWidth), Integer.toString(newHeight));
	}
}