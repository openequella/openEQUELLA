package com.tle.conversion.exporters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.imageio.ImageIO;

import com.tle.conversion.wmf.WMFImage;

/**
 * @author bmillar
 */
public class WMFExport implements Export
{
	/*
	 * (non-Javadoc)
	 * @see com.dytech.export.Exporter#exportFile(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void exportFile(String in, String out) throws IOException
	{
		BufferedImage buf = null;
		try( FileInputStream fis = new FileInputStream(in) )
		{
			WMFImage wmf = new WMFImage(fis, 800, 600, 96);

			buf = wmf.createImage();
			if( buf == null )
			{
				throw new IOException("Error creating image");
			}
		}

		ImageIO.write(buf, "jpeg", new File(out)); //$NON-NLS-1$
	}

	@Override
	public Collection<String> getInputTypes()
	{
		return Collections.singleton("wmf");

	}

	@Override
	public Collection<String> getOutputTypes()
	{
		return Collections.singleton("jpeg");
	}
}