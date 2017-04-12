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
import java.util.Set;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

/**
 * Converts files of BMP, TIFF etc. to a JPeg output.
 * 
 * @author bmillar
 */
public class JAIImageExport implements Export
{
	/**
	 * @see com.dytech.export.Export#exportFile(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void exportFile(String infile, String outfile) throws IOException
	{
		BufferedImage image = JAI.create("fileload", infile).getAsBufferedImage(); //$NON-NLS-1$
		ImageIO.write(image, "jpeg", new File(outfile)); //$NON-NLS-1$
	}

	@Override
	public Collection<String> getInputTypes()
	{
		Set<String> types = new HashSet<String>();
		types.add("tiff");
		types.add("pnm");
		return types;
	}

	@Override
	public Collection<String> getOutputTypes()
	{
		return Collections.singleton("jpeg");
	}
}
