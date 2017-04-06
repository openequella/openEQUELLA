/*
 * Created on Feb 4, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.tle.conversion.exporters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Converts files of BMP, TIFF etc. to a JPeg output.
 * 
 * @author bmillar
 */
public class ImageExport implements Export
{
	/**
	 * @see com.dytech.export.Export#exportFile(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void exportFile(String infile, String outfile) throws IOException
	{
		ImageInputStream in = ImageIO.createImageInputStream(new File(infile));
		Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(in);
		ImageReader reader = imageReaders.next();
		reader.setInput(in);
		BufferedImage image = reader.read(0);

		ImageIO.write(image, "jpeg", new File(outfile)); //$NON-NLS-1$

		in.close();
	}

	@Override
	public Collection<String> getInputTypes()
	{
		Set<String> types = new HashSet<String>();
		String[] readerNames = ImageIO.getReaderFormatNames();
		for( String names : readerNames )
		{
			types.add(names.toLowerCase());
		}
		types.remove("jpeg");
		types.remove("jpg");
		types.remove("gif");
		types.remove("png");
		return types;
	}

	@Override
	public Collection<String> getOutputTypes()
	{
		return Collections.singleton("jpeg");
	}
}
