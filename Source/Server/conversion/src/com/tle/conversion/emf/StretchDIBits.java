/*
 * Created on Aug 1, 2005
 */
package com.tle.conversion.emf;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.emf.BitmapInfo;
import org.freehep.graphicsio.emf.BitmapInfoHeader;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFOutputStream;
import org.freehep.graphicsio.emf.EMFTag;
import org.freehep.graphicsio.raw.RawImageWriteParam;
import org.freehep.util.UserProperties;
import org.freehep.util.io.NoCloseOutputStream;

public class StretchDIBits extends EMFTag
{
	public static final int size = 80;
	private Rectangle bounds;
	private int x;
	private int y;
	private int width;
	private int height;
	private int xSrc;
	private int ySrc;
	private int widthSrc;
	private int heightSrc;
	private int usage;
	private int dwROP;
	private Color bkg;
	private BitmapInfo bmi;
	private RenderedImage rend;
	private BufferedImage image;

	public StretchDIBits()
	{
		super(81, 1);
	}

	public StretchDIBits(Rectangle bounds, int x, int y, int width, int height, RenderedImage rend, Color bkg)
	{
		this();
		this.bounds = bounds;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		xSrc = 0;
		ySrc = 0;
		widthSrc = image.getWidth();
		heightSrc = image.getHeight();
		usage = 0;
		dwROP = 0xcc0020;
		this.bkg = bkg;
		this.rend = rend;
		image = new BufferedImage(rend.getWidth(), rend.getHeight(), 1);
		image.setData(rend.getData());
		bmi = null;
	}

	@Override
	public EMFTag read(int tagID, EMFInputStream emf, int len) throws IOException
	{
		StretchDIBits tag = new StretchDIBits();
		tag.bounds = emf.readRECTL();
		tag.x = emf.readLONG();
		tag.y = emf.readLONG();
		tag.xSrc = emf.readLONG();
		tag.ySrc = emf.readLONG();
		tag.width = emf.readLONG();
		tag.height = emf.readLONG();

		emf.readDWORD();
		emf.readDWORD();
		emf.readDWORD();
		emf.readDWORD();
		tag.usage = emf.readDWORD();
		tag.dwROP = emf.readDWORD();
		tag.widthSrc = emf.readLONG();
		tag.heightSrc = emf.readLONG();
		tag.bmi = new BitmapInfo(emf);
		int bytes[] = emf.readUnsignedByte(len - 72 - 40);
		if( tag.width > 0 && tag.height > 0 )
		{
			// Jolse's awesome bit logic
			int offset = (((tag.width * 3) ^ 3) + 1) & 3;
			BufferedImage buf = new BufferedImage(tag.width, tag.height, BufferedImage.TYPE_INT_RGB);
			int x1 = 0;
			int y1 = tag.height - 1;
			for( int i = 0; i + 2 < bytes.length; i += 3 )
			{
				int rgb = (new Color(bytes[i + 2], bytes[i + 1], bytes[i])).getRGB();
				buf.setRGB(x1, y1, rgb);
				x1++;
				if( x1 < tag.width )
				{
					continue;
				}

				i += offset;
				x1 = 0;
				y1--;
				if( y1 < 0 )
				{
					break;
				}
			}

			tag.image = buf;
		}
		return tag;
	}

	@Override
	public void write(int tagID, EMFOutputStream emf) throws IOException
	{
		emf.writeRECTL(bounds);
		emf.writeLONG(x);
		emf.writeLONG(y);
		emf.writeLONG(xSrc);
		emf.writeLONG(ySrc);
		emf.writeLONG(widthSrc);
		emf.writeLONG(heightSrc);
		emf.writeDWORD(80);
		emf.writeDWORD(40);
		emf.writeDWORD(120);
		emf.pushBuffer();
		int encode = 0;
		UserProperties properties = new UserProperties();
		properties.setProperty(RawImageWriteParam.BACKGROUND, bkg);
		properties.setProperty(RawImageWriteParam.CODE, "BGR");
		properties.setProperty(RawImageWriteParam.PAD, 1);
		ImageGraphics2D.writeImage(rend, "raw", properties, new NoCloseOutputStream(emf));
		int length = emf.popBuffer();
		emf.writeDWORD(length);
		emf.writeDWORD(usage);
		emf.writeDWORD(dwROP);
		emf.writeLONG(width);
		emf.writeLONG(height);
		BitmapInfoHeader header = new BitmapInfoHeader(widthSrc, heightSrc, 24, encode, length, 0, 0, 0, 0);
		bmi = new BitmapInfo(header);
		bmi.write(emf);
		emf.append();
	}

	public BufferedImage getImage()
	{
		return image;
	}

	@Override
	public String toString()
	{
		return super.toString() + "\n" + "  bounds: " + bounds + "\n" + "  x, y, w, h: " + x + " " + y + " " + width
			+ " " + height + "\n" + "  xSrc, ySrc, widthSrc, heightSrc: " + xSrc + " " + ySrc + " " + widthSrc + " "
			+ heightSrc + "\n" + "  usage: " + usage + "\n" + "  dwROP: " + dwROP + "\n" + "  bkg: " + bkg + "\n"
			+ bmi.toString();
	}

	public Rectangle getBounds()
	{
		return bounds;
	}

	public int getHeight()
	{
		return height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}
}
