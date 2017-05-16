package com.tle.conversion.wmf;

// Copyright 1997, 1998 Carmen Delessio (carmen@blackdirt.com)
// Black Dirt Software http://www.blackdirt.com/graphics
// Free for non-commercial use

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class BMPImage extends Canvas
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BMPImage.class);

	private int i;
	private int j;

	private DataInputStream d;

	private int BMPwidth; // image width in pixels
	private int BMPheight; // image height in pixels (if < 0, "top-down")
	private Color colorTable[]; // color table

	private int pixels[]; // array of pixels

	private int colorIndex;
	private int bytesPerLine;
	private byte scanline[];
	private Image BMPimage;

	public BMPImage(InputStream is)
	{
		try
		{
			d = new DataInputStream(is);
			parseit();
			BMPimage = createImage(new MemoryImageSource(BMPwidth, BMPheight, pixels, 0, BMPwidth));
		}
		catch( IOException ex )
		{
			LOGGER.error("Error Creating Bitmap Image");
		}
	}

	public BMPImage(String bmpString, int typeFlag)
	{
		byte bytePicture[];
		ByteArrayInputStream byteInput = null;

		// add string error check, length, BM, etc

		bytePicture = bmpString.getBytes();
		byteInput = new ByteArrayInputStream(bytePicture);
		try
		{
			d = new DataInputStream(byteInput);
			parseit(typeFlag);
			BMPimage = createImage(new MemoryImageSource(BMPwidth, BMPheight, pixels, 0, BMPwidth));
		}
		catch( IOException ex )
		{
			LOGGER.error("Error Creating Bitmap Image");
		}
	}

	public BMPImage(String bmpString)
	{
		byte bytePicture[];
		ByteArrayInputStream byteInput = null;
		bytePicture = bmpString.getBytes();
		byteInput = new ByteArrayInputStream(bytePicture);
		try
		{
			d = new DataInputStream(byteInput);
			parseit();
			int pwidth = getBMPwidth();
			BMPimage = createImage(new MemoryImageSource(pwidth, getBMPheight(), getPixels(), 0, pwidth));
		}
		catch( IOException ex )
		{
			LOGGER.error("Error Creating Bitmap Image");
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(getBMPwidth(), getBMPheight());
	}

	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension(getBMPwidth(), getBMPheight());
	}

	@Override
	public void paint(Graphics g)
	{
		g.drawImage(BMPimage, 0, 0, this);
	}

	@Override
	public void update(Graphics g)
	{
		paint(g);
	}

	public MemoryImageSource getMemoryImageSource()
	{
		int w = getBMPwidth();
		return (new MemoryImageSource(w, getBMPheight(), getPixels(), 0, w));
	}

	public Image getImage()
	{
		return (BMPimage);
	}

	private synchronized int getBMPwidth()
	{
		return BMPwidth;
	}

	private synchronized int getBMPheight()
	{
		return BMPheight;
	}

	private synchronized int[] getPixels()
	{
		return pixels;
	}

	public synchronized final void parseit() throws IOException
	{
		parseit(0);
	}

	public synchronized final void parseit(int typeFlag) throws IOException
	{
		byte[] tempBuffer;

		// begin bitmap header
		if( typeFlag == 0 )
		{
			// read header - bitmapfile
			tempBuffer = null;
			tempBuffer = new byte[2]; // get BM

			d.read(tempBuffer);

			readLong(d); // BMPsize
			readLong(d); // BMPreserved
			readLong(d); // BMPimageoffset
		}

		readLong(d); // BMPheadersize

		BMPwidth = readLong(d);
		BMPheight = readLong(d);

		readInt(d); // BMPplanes

		short BMPbitsPerPixel = readInt(d);

		readLong(d); // BMPcompression
		readLong(d); // BMPsizeOfBitmap
		readLong(d); // BMPhorzResolution
		readLong(d); // BMPvertResolution
		readLong(d); // BMPcolorsUsed
		readLong(d); // BMPcolorsImportant

		pixels = new int[BMPwidth * (BMPheight + 1)];

		if( BMPbitsPerPixel == 1 )
		{
			colorTable = new Color[2];
			for( i = 0; i < 2; i++ )
			{
				colorTable[i] = win2Color(readLong(d));
			}
			bytesPerLine = BMPwidth / 8; // width is # of pixels, twice as many
			// bytes as pixles
			// only used to read in scan lines
			if( bytesPerLine * 8 < BMPwidth )
			{ // if pixel is on odd boundary
				bytesPerLine++;
			}

			while( bytesPerLine % 4 != 0 )
			{
				bytesPerLine++; // get even boundary, DWORD boundary
			}

			scanline = new byte[bytesPerLine]; // declare a buffer sufficient
			// for 1 line

			for( i = BMPheight - 1; i >= 0; i-- )
			{ // bottom up, start with last line
				d.readFully(scanline, 0, bytesPerLine); // read in a line

				for( j = 0; j < BMPwidth; j += 8 )
				{
					colorIndex = (scanline[j / 8]) >> 7 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) >> 6 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j + 1] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) >> 5 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j + 2] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) >> 4 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j + 3] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) >> 3 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j + 4] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) >> 2 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j + 5] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) >> 1 & 0x01; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j + 6] = colorTable[colorIndex].getRGB();

					colorIndex = (scanline[j / 8]) & 0x01; // 1st 4 bits of
					// byte shifted and
					// masked
					pixels[i * BMPwidth + j + 7] = colorTable[colorIndex].getRGB();

				}
			}
		} // if bpp = 1

		if( BMPbitsPerPixel == 4 )
		{
			colorTable = new Color[16];
			for( i = 0; i < 16; i++ )
			{
				colorTable[i] = win2Color(readLong(d));
			}
			bytesPerLine = BMPwidth / 2; // width is # of pixels, twice as many
			// bytes as pixles
			// only used to read in scan lines
			if( bytesPerLine * 2 < BMPwidth )
			{ // if pixel is on odd boundary
				bytesPerLine++;
			}

			while( bytesPerLine % 4 != 0 )
			{
				bytesPerLine++; // get even boundary, DWORD boundary
			}

			scanline = new byte[bytesPerLine]; // declare a buffer sufficient
			// for 1 line
			for( i = BMPheight - 1; i >= 0; i-- )
			{ // bottom up, start with last line
				d.readFully(scanline, 0, bytesPerLine); // read in a line

				for( j = 0; j < BMPwidth; j += 2 )
				{
					colorIndex = (scanline[j / 2] >> 4) & 0x0F; // 1st 4 bits
					// of byte
					// shifted and
					// masked
					pixels[i * BMPwidth + j] = colorTable[colorIndex].getRGB();
					colorIndex = (scanline[j / 2]) & 0x0F; // 2nd 4 bits masked
					pixels[i * BMPwidth + j + 1] = colorTable[colorIndex].getRGB();
				}
			}
		} // if bpp = 4

		if( BMPbitsPerPixel == 8 )
		{
			colorTable = new Color[256];
			for( i = 0; i < 256; i++ )
			{
				colorTable[i] = win2Color(readLong(d));
			}
			bytesPerLine = BMPwidth; // width is # of pixels, 1 pixels for each
			// byte
			while( bytesPerLine % 4 != 0 )
			{
				bytesPerLine++; // get even boundary
			}
			scanline = new byte[bytesPerLine]; // declare a buffer sufficient
			// for 1 line

			for( i = BMPheight - 1; i >= 0; i-- )
			{ // bottom up, start with last line
				d.readFully(scanline); // read in a line
				for( j = 0; j < BMPwidth; j++ )
				{
					colorIndex = scanline[j];
					if( colorIndex < 0 )
					{
						colorIndex += 256;
					}
					pixels[i * BMPwidth + j] = colorTable[colorIndex].getRGB();
				}
			}
		} // if bpp = 8

		if( BMPbitsPerPixel == 24 )
		{
			int winBlue;
			int winGreen;
			int winRed;
			bytesPerLine = 3 * BMPwidth; // width is # of pixels, 3 bytes for
			// each pixel
			while( bytesPerLine % 4 != 0 )
			{
				bytesPerLine++; // get even boundary
			}
			scanline = new byte[bytesPerLine + 4]; // declare a buffer
			// sufficient for 1 line

			for( i = BMPheight - 1; i >= 0; i-- )
			{ // bottom up, start with last line
				d.readFully(scanline, 0, bytesPerLine); // read in a line
				for( j = 0; j < bytesPerLine; j += 3 )
				{ // work with 3 bytes at a time
					// j
					// j+1
					// j+2
					winBlue = (scanline[j]) & 0xff;
					winGreen = ((scanline[j + 1]) & 0xff) << 8;
					winRed = ((scanline[j + 2]) & 0xff) << 16;
					pixels[i * BMPwidth + j / 3] = 0xff000000;
					pixels[i * BMPwidth + j / 3] |= winRed;
					pixels[i * BMPwidth + j / 3] |= winGreen;
					pixels[i * BMPwidth + j / 3] |= winBlue;
				}
			}
		} // if bpp = 24

	}

	public Color win2Color(int colorValue)
	{

		// windows does it backwards
		int rgbBlue = 16711680; // ff0000
		int rgbGreen = 65280; // 00ff00
		int rgbRed = 255; // 0000ff

		int javaBlue;
		int javaGreen;
		int javaRed;

		javaRed = (colorValue & rgbBlue) / 65536;
		javaGreen = (colorValue & rgbGreen) / 256;
		javaBlue = colorValue & rgbRed;

		return (new Color(javaRed, javaGreen, javaBlue));
	}

	public int readLong(DataInputStream d)
	{
		byte[] longBuf = new byte[4];

		try
		{
			d.readFully(longBuf);
			return flipLong(longBuf);
		}
		catch( IOException e )
		{
			LOGGER.error("Error", e);
			return 99;
		}

	}

	public short readInt(DataInputStream d)
	{
		byte[] intBuf = new byte[2];

		try
		{
			d.readFully(intBuf);
			return flipInt(intBuf);
		}
		catch( IOException e )
		{
			LOGGER.error("Error", e);
			return 99;
		}

	}

	public int flipLong(byte[] byteFlip)
	{
		DataInputStream dl;
		ByteArrayInputStream b_in;
		byte[] bytebuffer;
		bytebuffer = new byte[4];
		bytebuffer[0] = byteFlip[3];
		bytebuffer[1] = byteFlip[2];
		bytebuffer[2] = byteFlip[1];
		bytebuffer[3] = byteFlip[0];

		b_in = new ByteArrayInputStream(bytebuffer);
		dl = new DataInputStream(b_in);
		try
		{
			return dl.readInt();
		}
		catch( IOException e )
		{
			LOGGER.error("Error", e);
		}
		return 0;

	}

	public short flipInt(byte[] byteFlip)
	{
		DataInputStream d;
		ByteArrayInputStream b_in;
		byte[] bytebuffer;

		bytebuffer = new byte[2];
		bytebuffer[0] = byteFlip[1];
		bytebuffer[1] = byteFlip[0];

		b_in = new ByteArrayInputStream(bytebuffer);
		d = new DataInputStream(b_in);
		try
		{
			return d.readShort();
		}
		catch( IOException e )
		{
			LOGGER.error("Error", e);
		}
		return 0;

	}
}
