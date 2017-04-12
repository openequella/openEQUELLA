package com.tle.conversion.wmf;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;

class WMFToolkit
{
	private Integer screenResolution;
	// private int screenWidth;
	// private int screenHeight;
	private Dimension screen;
	private int twipsPerPixel;

	private int red;
	private int green;
	private int blue;

	public static int vbBlue = 16711680; // ff0000
	public static int vbGreen = 65280; // 00ff00
	public static int vbRed = 255; // 0000ff

	public WMFToolkit()
	{
		red = 0;
		green = 0;
		blue = 0;
	}

	public int getRed()
	{
		return red;
	}

	public int getGreen()
	{
		return green;
	}

	public int getBlue()
	{
		return blue;
	}

	public int getScreenWidth()
	{
		if( screen == null )
		{
			screen = Toolkit.getDefaultToolkit().getScreenSize();
		}
		return screen.width;
	}

	public int getScreenHeight()
	{
		if( screen == null )
		{
			screen = Toolkit.getDefaultToolkit().getScreenSize();
		}
		return screen.height;
	}

	private void checkRes()
	{
		try
		{
			if( screenResolution == null )
			{
				screenResolution = new Integer(Toolkit.getDefaultToolkit().getScreenResolution());
				twipsPerPixel = 1440 / screenResolution.intValue();
			}
		}
		catch( HeadlessException he )
		{
			screenResolution = new Integer(92);
			twipsPerPixel = 1440 / screenResolution.intValue();
		}
	}

	public int getScreenResolution()
	{
		checkRes();
		return screenResolution.intValue();
	}

	public int getTwipsPerPixel()
	{
		checkRes();
		return twipsPerPixel;
	}

	public short twip2pixel(short t)
	{
		return (short) (t / getTwipsPerPixel());
	}

	public void setColors(int colorValue)
	{
		blue = (colorValue & vbBlue) / 65536;
		green = (colorValue & vbGreen) / 256;
		red = colorValue & vbRed;
	}
}
