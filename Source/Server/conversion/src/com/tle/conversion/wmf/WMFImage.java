package com.tle.conversion.wmf;

// Copyright 1997, 1998 Carmen Delessio (carmen@blackdirt.com)
// Black Dirt Software http://www.blackdirt.com/graphics
// Free for non-commercial use

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class WMFImage extends Canvas
{
	private static final Logger LOGGER = LoggerFactory.getLogger(WMFImage.class);

	/* Text Alignment Options */
	public static final int TA_NOUPDATECP = 0;
	public static final int TA_UPDATECP = 1;

	public static final int TA_LEFT = 0;
	public static final int TA_RIGHT = 2;
	public static final int TA_CENTER = 6;

	public static final int TA_TOP = 0;
	public static final int TA_BOTTOM = 8;
	public static final int TA_BASELINE = 24;

	/* Pen Styles */
	public static final int PS_SOLID = 0x00000000;
	public static final int PS_DASH = 0x00000001;
	public static final int PS_DOT = 0x00000002;
	public static final int PS_DASHDOT = 0x00000003;
	public static final int PS_DASHDOTDOT = 0x00000004;
	public static final int PS_NULL = 0x00000005;
	public static final int PS_INSIDEFRAME = 0x00000006;
	public static final int PS_USERSTYLE = 0x00000007;
	public static final int PS_ALTERNATE = 0x00000008;

	/* Modes for WmfSetMapMode */
	public static final int MM_TEXT = 0x00000001;
	public static final int MM_LOMETRIC = 0x00000002;
	public static final int MM_HIMETRIC = 0x00000003;
	public static final int MM_LOENGLISH = 0x00000004;
	public static final int MM_HIENGLISH = 0x00000005;
	public static final int MM_TWIPS = 0x00000006;
	public static final int MM_ISOTROPIC = 0x00000007;
	public static final int MM_ANISOTROPIC = 0x00000008;
	public static final int MM_DPI = 0x00000009;

	private int j;
	private int bytes_read;

	private int windowLong;
	private short windowInt;

	private int textAlign = 0;

	public short inch;
	private int resolution;

	private static MetaRecord mRecord;
	private java.util.Vector<MetaRecord> metaRecordVector;
	private java.util.Enumeration<MetaRecord> metaRecordInfo;
	private DataInputStream d;

	private int recordIndex;
	public WMFHandleTable ht;
	private int[] ncount;

	byte[] byteData; // Unpacked data: one byte per pixel
	byte[] rawData; // the raw unpacked data
	int[] intData; // Unpacked data: one int per pixel

	public WMFToolkit cvtTool = new WMFToolkit();
	private boolean drawFilled;
	private short oldx;
	private short oldy;
	private String javaGraphic = new String("");
	private String javaDeclare = new String("");

	private short numLines;

	public short wmfWidth;
	public short wmfHeight;

	// private short htmlWidth;// = 800;
	// private short htmlHeight;// = 600;

	public short logExtX;// = 1024;
	public short logExtY;// = 768;

	public short logOrgX = 0;
	public short logOrgY = 0;

	public short devExtX = 0;
	public short devExtY = 0;

	public int AppletWidth;
	public int AppletHeight;

	public float d_x;
	public float d_y;
	public Graphics wmfGraphics;
	public BufferedImage wmfImageBuffer;

	public int fontStyle;
	public int fontWeight;
	public boolean fontItalic = false;

	private Color penColour = null;
	private Color textColour = null;
	private Color brushColour = null;

	private boolean outline = false;

	public WMFImage(InputStream is, int w, int h, int resolution) throws IOException
	{
		this.resolution = resolution;
		this.AppletWidth = w;
		this.AppletHeight = h;
		this.devExtX = (short) w;
		devExtY = (short) h;
		mRecord = new MetaRecord(windowLong, windowInt, null);
		d = new DataInputStream(is);
		parseit();
	}

	public WMFImage(InputStream is, int w, int h) throws IOException
	{
		this(is, w, h, -1);
	}

	public Image getImage()
	{
		return (wmfImageBuffer);
	}

	@Override
	public void addNotify()
	{
		super.addNotify();

		createImage();
	}

	public BufferedImage createImage()
	{
		if( wmfWidth <= 0 || wmfHeight <= 0 )
		{
			wmfGraphics = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB).getGraphics();
			WMFPlay(AppletWidth, AppletHeight); // get the bounds
		}

		if( wmfWidth <= 0 || wmfHeight <= 0 )
		{
			return null;
		}

		wmfImageBuffer = new BufferedImage(wmfWidth, wmfHeight, BufferedImage.TYPE_INT_RGB);

		wmfGraphics = wmfImageBuffer.getGraphics();
		if( wmfGraphics == null )
		{
			LOGGER.error(" graphics is null");
		}
		else
		{
			wmfGraphics.fillRect(0, 0, wmfWidth, wmfHeight);
		}

		WMFPlay(AppletWidth, AppletHeight);

		return wmfImageBuffer;
	}

	@Override
	public void paint(Graphics g)
	{
		g.drawImage(wmfImageBuffer, 0, 0, this);
	}

	@Override
	public void update(Graphics g)
	{
		paint(g);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(AppletWidth, AppletHeight);
	}

	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension((int) Math.round(0.25 * AppletWidth), (int) Math.round(0.25 * AppletHeight));
	}

	public synchronized void parseit() throws IOException
	{
		byte[] f_long = new byte[4];
		byte[] parmBuffer;
		short x;
		short y;
		short x2;
		short y2;
		short count = 0;

		// begin metafile header
		windowLong = readLong(d); // key 4 bytes
		if( windowLong == -1698247209 ) // placable
		{
			windowInt = readInt(d); // unused
			x2 = readInt(d);
			y2 = readInt(d);
			x = readInt(d);
			y = readInt(d);
			wmfWidth = (short) Math.abs(x2 - x);
			wmfHeight = (short) Math.abs(y2 - y);

			x = cvtTool.twip2pixel(x); // add inch stuff here
			cvtTool.twip2pixel(y);

			windowInt = readInt(d); // inch
			inch = windowInt;
			if( resolution == -1 )
			{
				resolution = cvtTool.getScreenResolution();
			}
			x = (short) (inch / resolution);
			if( x < 1 )
			{
				x = 1;
			}

			wmfWidth = (short) (wmfWidth / x);
			wmfHeight = (short) (wmfHeight / x);

			AppletWidth = wmfWidth;
			AppletHeight = wmfHeight;

			d_x = (float) AppletWidth / wmfWidth;
			d_y = (float) AppletHeight / wmfHeight;

			devExtX = wmfWidth;
			devExtY = wmfHeight;

			windowLong = readLong(d); // reserved
			windowInt = readInt(d);// checksum

			// metaheader
			for( int i = 0; i < 3; i++ )
			{
				windowInt = readInt(d);
			}
			windowLong = readLong(d);
			windowInt = readInt(d);
			windowLong = readLong(d);
			windowInt = readInt(d);
		}
		else
		{
			// standard

			/*
			 * WORD FileType; /* Type of metafile (0=memory, 1=disk) * / WORD
			 * HeaderSize; /* Size of header in WORDS (always 9) * / WORD
			 * Version; /* Version of Microsoft Windows used * / DWORD FileSize;
			 * /* Total size of the metafile in WORDs * / WORD NumOfObjects; /*
			 * Number of objects in the file * / DWORD MaxRecordSize; /* The
			 * size of largest record in WORDs * / WORD NumOfParams; /* Not Used
			 * (always 0) * /
			 */

			// not placeable- no metafile header
			// already read long to check for key
			// short fileType = (short)(windowLong & 0xFFFF);
			// short headerSize = (short)(windowLong >> 16);
			windowInt = readInt(d);
			windowLong = readLong(d);
			windowInt = readInt(d);
			windowLong = readLong(d);
			windowInt = readInt(d);
		}

		metaRecordVector = new java.util.Vector<MetaRecord>();
		while( true )
		{
			count++;
			try
			{
				d.readFully(f_long);
			}
			catch( EOFException e )
			{
				LOGGER.debug("**** eof");
				break;
			}

			if( bytes_read == -1 )
			{
				break;
			}
			windowLong = flipLong(f_long);

			windowInt = readInt(d);

			if( windowInt == 0 )
			{
				break;
			}

			if( windowLong >= 3 )
			{
				windowLong = (windowLong * 2) - 6;
			}

			parmBuffer = null;
			parmBuffer = new byte[windowLong];
			d.readFully(parmBuffer);
			metaRecordVector.addElement(new MetaRecord(windowLong, windowInt, parmBuffer));
		}
	}

	public void WMFPlay(int w, int h)
	{
		AppletWidth = w;
		AppletHeight = h;
		devExtX = (short) w;
		devExtY = (short) h;

		ht = new WMFHandleTable();
		drawFilled = false;
		metaRecordInfo = metaRecordVector.elements();
		recordIndex = 0;

		Rectangle bounds = new Rectangle();

		while( metaRecordInfo.hasMoreElements() )
		{
			mRecord = metaRecordInfo.nextElement();
			Rectangle subBounds = wmfListRecord(mRecord, false, true, wmfGraphics);
			bounds.add(subBounds);
			recordIndex++;
		}

		wmfWidth = (short) bounds.width;
		wmfHeight = (short) bounds.height;
	}

	private synchronized Rectangle wmfListRecord(MetaRecord mRecord, boolean fromSelect, boolean play, Graphics g)
	{
		Rectangle bounds = new Rectangle();
		short x;
		short y;
		short x2;
		short y2;
		short numChars;
		// short wOptions;
		int selColor;
		short w;
		short h;
		// short lbhatch;
		short lbstyle;
		short numPoints;
		DataInputStream parmStream;
		ByteArrayInputStream parmIn;
		String shapeName;
		String tempBuffer;
		String currentFont = "Dialog";
		byte[] textBuffer;
		float fontHeight = 10;
		short fontHeightShort = 10;
		Polygon poly = new Polygon();

		if( g == null )
		{
			LOGGER.error(" graphics is null");
		}

		parmIn = new ByteArrayInputStream(mRecord.getParm());
		parmStream = new DataInputStream(parmIn);

		int fn = mRecord.getFunction();
		LOGGER.debug("0x" + Integer.toHexString(fn) + " ");
		try
		{
			switch( fn )
			{

				case 0x2fa:
					// create pen indirect
					if( !fromSelect )
					{
						ht.addObject(recordIndex, mRecord);
						LOGGER.debug("PEN_INDIRECT(" + penColour + ")");
					}
					else
					{
						lbstyle = readInt(parmStream); // if 5 outline is off
						outline = lbstyle != PS_NULL;
						x = readInt(parmStream);
						y = readInt(parmStream);
						selColor = readLong(parmStream);
						cvtTool.setColors(selColor);
						penColour = new Color(cvtTool.getRed(), cvtTool.getGreen(), cvtTool.getBlue());
						if( g != null )
						{
							g.setColor(penColour);
						}
						LOGGER.debug("PEN_INDIRECT(" + penColour + ", style = " + lbstyle + ")");
					}

					break;

				case 0x6ff:
					LOGGER.debug("REGION");
					// create region
					if( !fromSelect )
					{
						ht.addObject(recordIndex, mRecord);
					}
					break;

				case 0x12e:
					// setTextAlign
					textAlign = readLong(parmStream);
					LOGGER.debug("SETTEXTALIGN [" + textAlign + "]");
					break;

				case 0x2fb:
					LOGGER.debug("FONT_INDIRECT");
					// createFontIndirect
					if( !fromSelect )
					{ // if not selecting it, just add it to table
						ht.addObject(recordIndex, mRecord);
					}
					else
					{
						fontHeightShort = readInt(parmStream);
						fontHeight = fontHeightShort;
						fontHeightShort = (short) fontHeight;
						if( fontHeightShort < 0 )
						{
							fontHeightShort *= -1;
							fontHeightShort = mapY(fontHeightShort);
						}
						else
						{
							fontHeight = (fontHeight / inch);
							fontHeight = (fontHeight * 72);
							fontHeightShort = (short) fontHeight;
							if( fontHeightShort < 5 )
							{
								fontHeightShort = 9;
							}
						}
						x2 = readInt(parmStream); // width
						y2 = readInt(parmStream); // esc
						y2 = readInt(parmStream); // orientation
						y2 = readInt(parmStream); // weight
						fontWeight = y2;
						textBuffer = new byte[1];
						try
						{
							parmStream.read(textBuffer);
						}
						catch( IOException e )
						{
							LOGGER.error("Error", e);
						}

						x = textBuffer[0]; // italic
						fontItalic = false;
						if( x < 0 )
						{
							fontItalic = true;
						}

						textBuffer = new byte[7];
						try
						{
							parmStream.read(textBuffer);
						}
						catch( IOException e )
						{
							LOGGER.error("Error", e);
						}
						tempBuffer = new String(textBuffer);

						textBuffer = new byte[32]; // name of font
						try
						{
							parmStream.read(textBuffer);
						}
						catch( IOException e )
						{
							LOGGER.error("Error", e);
						}
						tempBuffer = new String(textBuffer);

						currentFont = "Dialog";
						if( tempBuffer.startsWith("Courier") )
						{
							currentFont = "Courier";
						}
						else if( tempBuffer.startsWith("MS Sans Serif") )
						{
							currentFont = "Dialog";
						}
						else if( tempBuffer.startsWith("Arial") )
						{
							currentFont = "Helvetica";
						}
						else if( tempBuffer.startsWith("Arial Narrow") )
						{
							currentFont = "Helvetica";
						}
						else if( tempBuffer.startsWith("Arial Black") )
						{
							currentFont = "Helvetica";
							fontWeight = 700;
						}
						else if( tempBuffer.startsWith("Times New Roman") )
						{
							currentFont = "TimesRoman";
						}
						else if( tempBuffer.startsWith("Wingdings") )
						{
							currentFont = "ZapfDingbats";
						}
						if( fontItalic )
						{
							fontStyle = Font.ITALIC;
							if( fontWeight >= 700 )
							{ // bold + italic
								fontStyle = 3;
							}
						}
						else
						{
							fontStyle = Font.PLAIN; // plain
							if( fontWeight >= 700 )
							{ // bold
								fontStyle = Font.BOLD;
							}
						}
						if( g != null )
						{
							g.setFont(new Font(currentFont, fontStyle, fontHeightShort));
						}
					}
					break;
				case 0x2fc:
					// createBrushIndirect

					if( !fromSelect )
					{ // if not seleceting it, just add it to table
						ht.addObject(recordIndex, mRecord);
						LOGGER.debug("BRUSH_INDIRECT");
					}
					else
					{ // selected - use it
						lbstyle = readInt(parmStream);
						selColor = readLong(parmStream);
						// lbhatch = readInt(parmStream);
						outline = lbstyle != PS_NULL;
						drawFilled = lbstyle <= 0;

						cvtTool.setColors(selColor);
						if( play )
						{
							// g.setColor(new Color(cvtTool.getRed(),
							// cvtTool.getGreen(), cvtTool
							// .getBlue()));
							brushColour = new Color(cvtTool.getRed(), cvtTool.getGreen(), cvtTool.getBlue());
						}
						else
						{
							javaGraphic = javaGraphic + "    g.setColor( new Color( " + cvtTool.getRed() + ","
								+ cvtTool.getGreen() + "," + cvtTool.getBlue() + "));" + "\n";
						}
						LOGGER.debug("BRUSH_INDIRECT (style=" + lbstyle + ")");
					}

					break;

				case 0x12d:
					LOGGER.debug("SELECT");
					// select object
					windowInt = readInt(parmStream);
					mRecord = ht.selectObject(windowInt);

					if( g != null )
					{
						wmfListRecord(mRecord, true, play, g);
					}
					break;

				case 0x1f0:
					LOGGER.debug("DELETE");
					// delete object
					windowInt = readInt(parmStream);
					ht.deleteObject(windowInt);
					break;

				case 0x41b:
					LOGGER.debug("RECTANGLE");

					y2 = readInt(parmStream);
					x2 = readInt(parmStream);
					y = readInt(parmStream);
					x = readInt(parmStream);
					x = mapX(x);
					x2 = mapX(x2);
					y = mapY(y);
					y2 = mapY(y2);
					w = (short) Math.abs(x2 - x);
					h = (short) Math.abs(y2 - y);

					tempBuffer = "" + "( " + x + ", " + y + ", " + w + ", " + h + ");//  rectangle";

					if( drawFilled && g != null && brushColour != null )
					{
						g.setColor(brushColour);
						g.fillRect(x, y, w, h);
					}
					if( g != null && outline && penColour != null )
					{
						g.setColor(penColour);
						g.drawRect(x, y, w, h);
					}

					bounds = new Rectangle(x, y, w, h);

					break;

				case 0x418:
					LOGGER.debug("OVAL");
					// Oval
					y2 = readInt(parmStream);
					x2 = readInt(parmStream);
					y = readInt(parmStream);
					x = readInt(parmStream);

					x = mapX(x);
					x2 = mapX(x2);
					y = mapY(y);
					y2 = mapY(y2);

					w = (short) Math.abs(x2 - x);
					h = (short) Math.abs(y2 - y);

					tempBuffer = "" + "( " + x + ", " + y + ", " + w + ", " + h + ");//  rectangle";

					if( drawFilled && g != null && brushColour != null )
					{
						g.setColor(brushColour);
						g.fillOval(x, y, w, h);
					}
					if( g != null && outline && penColour != null )
					{
						g.setColor(penColour);
						g.drawOval(x, y, w, h);
					}

					bounds = new Rectangle(x, y, w, h);

					break;

				case 0x325:
					LOGGER.debug("POLYLINE");
					// polyline
					poly = new Polygon();
					numPoints = readInt(parmStream);

					for( int i = 0; i < numPoints; i++ )
					{
						x = readInt(parmStream);
						y = readInt(parmStream);
						int _x = mapX(x);
						int _y = mapY(y);
						poly.addPoint(_x, _y);
						LOGGER.debug("  Polyline (" + x + ", " + y + ")->(" + _x + ", " + _y + ")");
					}
					if( g != null && drawFilled && brushColour != null )
					{
						g.setColor(brushColour);
						g.fillPolygon(poly);
					}
					if( g != null && outline && penColour != null )
					{
						g.setColor(penColour);
						g.drawPolygon(poly);
					}

					bounds = poly.getBounds();
					break;

				case 0x324:
					LOGGER.debug("POLYGON");
					// polygon
					poly = new Polygon();
					numPoints = readInt(parmStream);

					oldx = readInt(parmStream);
					oldy = readInt(parmStream);

					oldx = mapX(oldx);
					oldy = mapY(oldy);

					poly.addPoint(oldx, oldy);
					for( int i = 0; i < numPoints - 1; i++ )
					{
						x = readInt(parmStream);
						y = readInt(parmStream);
						int _x = mapX(x);
						int _y = mapY(y);
						poly.addPoint(_x, _y);
						LOGGER.debug("  Polygon (" + x + ", " + y + ")->(" + _x + ", " + _y + ")");
					}
					poly.addPoint(oldx, oldy);

					// Fill:
					if( drawFilled && g != null && brushColour != null )
					{
						g.setColor(brushColour);
						g.fillPolygon(poly);
					}

					// Outline:
					if( g != null && outline && penColour != null )
					{
						g.setColor(penColour);
						g.drawPolygon(poly);
					}

					bounds = poly.getBounds();

					break;

				case 0x538:
					LOGGER.debug("POLYPOLYGON");
					// polypolygon
					int numPolys = readInt(parmStream);

					ncount = new int[numPolys];
					for( j = 0; j < numPolys; j++ )
					{

						ncount[j] = readInt(parmStream);

					}

					for( j = 0; j < numPolys; j++ )
					{
						poly = new Polygon();

						numPoints = (short) ncount[j];

						oldx = readInt(parmStream);
						oldy = readInt(parmStream);

						oldx = mapX(oldx);
						oldy = mapY(oldy);

						poly.addPoint(oldx, oldy);

						for( int i = 0; i < numPoints - 1; i++ )
						{
							x = readInt(parmStream);
							y = readInt(parmStream);
							x = mapX(x);
							y = mapY(y);
							poly.addPoint(x, y);

						}
						poly.addPoint(oldx, oldy);

						if( drawFilled && g != null && brushColour != null )
						{
							g.setColor(brushColour);
							g.fillPolygon(poly);
						}
						if( g != null && outline && penColour != null )
						{
							g.setColor(penColour);
							g.drawPolygon(poly);
						}
						bounds = poly.getBounds();
					}

					break;

				case 0x214:
					LOGGER.debug("MOVETO");
					// moveto
					oldy = readInt(parmStream);
					oldx = readInt(parmStream);
					oldx = mapX(oldx);
					oldy = mapY(oldy);

					break;

				case 0x213:
					LOGGER.debug("LINETO");
					// lineto
					numLines++;
					shapeName = "line" + numLines;
					javaDeclare = javaDeclare + "    Polygon " + shapeName + "= new Polygon();" + "\n";
					y = readInt(parmStream);
					x = readInt(parmStream);
					x = mapX(x);
					y = mapY(y);

					if( g != null && penColour != null )
					{
						g.setColor(penColour);
						g.drawLine(oldx, oldy, x, y);
					}

					bounds.x = Math.min(oldx, x);
					bounds.y = Math.min(oldy, y);
					bounds.width = Math.abs(oldx - x);
					bounds.width = Math.abs(oldy - y);

					oldx = x;
					oldy = y;

					break;

				case 0x209:
					LOGGER.debug("TEXT_COLOUR");
					// set text color
					// save text color
					// when writing text, switch to text colors
					// when done writing, switch back

					selColor = readLong(parmStream);
					cvtTool.setColors(selColor);
					textColour = new Color(cvtTool.getRed(), cvtTool.getGreen(), cvtTool.getBlue());

					if( g != null )
					{
						g.setColor(textColour);
					}
					break;

				case 0x201:
					LOGGER.debug("BK_COLOUR");
					// set BK color
					if( g != null && penColour != null )
					{
						g.setColor(penColour);
					}
					break;

				case 0xa32:
					LOGGER.debug("EXTTEXT");
					if( g == null || textColour == null )
					{
						return bounds;
						// exttext...
					}

					y = readInt(parmStream);
					x = readInt(parmStream);

					x = mapX(x);
					y = mapY(y);

					numChars = readInt(parmStream);
					// wOptions = readInt(parmStream);
					textBuffer = new byte[numChars];
					short[] spaceBuffer = new short[numChars + 1];

					/* int flags = */
					readInt(parmStream); // not sure what these 2 bytes are

					try
					{
						parmStream.read(textBuffer);

						// Read extra distances:
						for( int i = 0; i < spaceBuffer.length; ++i )
						{
							spaceBuffer[i] = readInt(parmStream);
						}
					}
					catch( IOException e )
					{
						LOGGER.error("Error", e);
					}
					tempBuffer = new String(textBuffer);
					LOGGER.debug("  \"" + tempBuffer + "\"");

					// Setup alignment
					boolean centre = (textAlign & TA_CENTER) == TA_CENTER;
					boolean right = (textAlign & TA_RIGHT) == TA_RIGHT;
					boolean baseline = (textAlign & TA_BASELINE) == TA_BASELINE;
					boolean bottom = (textAlign & TA_BOTTOM) == TA_BOTTOM;

					int _x = x,
					_y = y; // default to left/baseline

					FontMetrics mets = g.getFontMetrics();
					int dec = mets.getDescent();
					Rectangle2D strBounds = g.getFontMetrics().getStringBounds(tempBuffer, g);
					if( centre )
					{
						_x = (short) (x - strBounds.getWidth() / 2);
					}
					else if( right )
					{
						_x = (short) (x - strBounds.getWidth());
					}

					if( !baseline )
					{
						if( bottom )
						{
							_y = (short) (y - dec);
						}
						else
						// top - default
						{
							int upper = (int) strBounds.getHeight() - dec;
							_y = (short) (y + upper);
						}
					}
					g.setColor(textColour);
					g.drawString(tempBuffer, _x, _y);

					bounds = strBounds.getBounds();
					break;

				case 0x521:
					LOGGER.debug("TEXTOUT");
					if( g == null || textColour == null )
					{
						return bounds;
						// TEXTOUT
					}

					numChars = readInt(parmStream);
					textBuffer = new byte[numChars + 1];
					try
					{
						parmStream.read(textBuffer);
					}
					catch( IOException e )
					{
						LOGGER.error("Error", e);
					}

					tempBuffer = new String(textBuffer);

					y = readInt(parmStream);
					x = readInt(parmStream);

					x = mapX(x);
					y = mapY(y);

					// Setup alignment
					centre = (textAlign & TA_CENTER) == TA_CENTER;
					right = (textAlign & TA_RIGHT) == TA_RIGHT;
					baseline = (textAlign & TA_BASELINE) == TA_BASELINE;
					bottom = (textAlign & TA_BOTTOM) == TA_BOTTOM;

					_x = x;
					_y = y; // default to left/baseline

					mets = g.getFontMetrics();
					dec = mets.getDescent();
					strBounds = g.getFontMetrics().getStringBounds(tempBuffer, g);
					if( centre )
					{
						_x = (short) (x - strBounds.getWidth() / 2);
					}
					else if( right )
					{
						_x = (short) (x - strBounds.getWidth());
					}

					if( !baseline )
					{
						if( bottom )
						{
							_y = (short) (y - dec);
						}
						else
						// top - default
						{
							int upper = (int) strBounds.getHeight() - dec;
							_y = (short) (y + upper);
						}
					}

					g.setColor(textColour);
					g.drawString(tempBuffer, _x, _y);

					bounds = strBounds.getBounds();

					break;

				case 0xF43:
					LOGGER.debug("STRETCHDIB");
					// stretch DIB
					Image image;
					BMPImage bmp = null;
					tempBuffer = new String(mRecord.getParm());
					tempBuffer = tempBuffer.substring(22);
					bmp = new BMPImage(tempBuffer, 1);
					LOGGER.debug(" instantiated");
					image = bmp.getImage();
					if( g != null )
					{
						g.drawImage(image, 0, 0, this);
					}
					break;

				case 0x20B:
					// set_window_org
					logOrgY = readInt(parmStream);
					logOrgX = readInt(parmStream);
					LOGGER.debug("SET_WINDOW_ORG[" + logOrgX + ", " + logOrgY + "]");
					break;

				case 0x20C:
					// set_window_ext
					logExtY = readInt(parmStream);
					logExtX = readInt(parmStream);
					LOGGER.debug("SET_WINDOW_EXT[" + logExtX + ", " + logExtY + "]");
					break;

				// // Not fully implemented: ////

				case 0xb41:
					// DibStretchBlt
					int srcY = readInt(parmStream);
					int srcX = readInt(parmStream);
					int srcHeight = readInt(parmStream);
					int srcWidth = readInt(parmStream);
					int dstHeight = readInt(parmStream);
					int dstWidth = readInt(parmStream);
					int dstY = readInt(parmStream);
					int dstX = readInt(parmStream);

					if( dstHeight == 0 )
					{
						dstHeight = srcHeight;
					}
					if( dstWidth == 0 )
					{
						dstWidth = srcWidth;
					}

					bmp = null;
					tempBuffer = new String(mRecord.getParm());
					tempBuffer = tempBuffer.substring(20);
					bmp = new BMPImage(tempBuffer, 1);
					image = bmp.getImage();
					if( g != null )
					{
						g.drawImage(image, 0, 0, dstWidth, dstHeight, this);
					}

					bounds.add(new Rectangle(0, 0, dstWidth, dstHeight));

					LOGGER.debug("DIB_STRETCH_BLT[" + srcX + ", " + srcY + ": " + srcWidth + "x" + srcHeight + " -> "
						+ dstX + ", " + dstY + ": " + dstWidth + "x" + dstHeight + "]");
					break;

				case 0x103:
					// SetMapMode
					int mode = readInt(parmStream);
					if( mode != MM_TEXT )
					{
						LOGGER.warn("SET_MAP_MODE: Unsupported, non-text mapping set [" + mode + "]");
					}

					break;

				/* Apparently these three don't matter: */
				case 0xf7:
					// CreatePalette
				case 0x234:
					// SelectPalette
				case 0x107:
					// SetStretchBltMode

					// // ////
				default:
					javaGraphic = "// unrecognized function " + Integer.toHexString(fn) + "\n";
					LOGGER.error(javaGraphic);
			} // end switch
		}
		catch( Exception e1 )
		{
			LOGGER.error("Failed to parse tag.", e1);
		}

		try
		{
			parmStream.close();
		}
		catch( IOException e )
		{
			LOGGER.error("Error", e);
		}

		return bounds;
	}

	public short mapX(short x)
	{
		d_x = (float) devExtX / logExtX;
		x = (short) (x - logOrgX);
		x = (short) (x * d_x);
		return (x);
	}

	public short mapY(short y)
	{
		d_y = (float) devExtY / logExtY;
		y = (short) (y - logOrgY);
		y = (short) (y * d_y);
		return (y);
	}

	public short scaleX(short x)
	{
		d_x = (float) devExtX / logExtX;
		x = (short) (x * d_x);
		return (x);
	}

	public short scaleY(short y)
	{
		d_y = (float) devExtY / logExtY;
		y = (short) (y * d_y);
		return (y);
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

	public int readLong(DataInputStream d)
	{
		byte[] longBuf = new byte[4];

		try
		{
			d.read(longBuf);
			// d.readFully(longBuf);
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
			d.read(intBuf);
			// d.readFully(intBuf);
			return flipInt(intBuf);
		}
		catch( IOException e )
		{
			LOGGER.error("Error", e);
			return 99;
		}

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
