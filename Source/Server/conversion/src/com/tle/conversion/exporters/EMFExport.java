package com.tle.conversion.exporters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.freehep.graphicsio.emf.Arc;
import org.freehep.graphicsio.emf.ArcTo;
import org.freehep.graphicsio.emf.Chord;
import org.freehep.graphicsio.emf.CreateBrushIndirect;
import org.freehep.graphicsio.emf.CreatePen;
import org.freehep.graphicsio.emf.EMFConstants;
import org.freehep.graphicsio.emf.EMFHeader;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFRectangle;
import org.freehep.graphicsio.emf.EMFTagSet;
import org.freehep.graphicsio.emf.Ellipse;
import org.freehep.graphicsio.emf.ExtCreateFontIndirectW;
import org.freehep.graphicsio.emf.ExtLogFontW;
import org.freehep.graphicsio.emf.ExtTextOutW;
import org.freehep.graphicsio.emf.FillPath;
import org.freehep.graphicsio.emf.LineTo;
import org.freehep.graphicsio.emf.LogBrush32;
import org.freehep.graphicsio.emf.LogFontW;
import org.freehep.graphicsio.emf.LogPen;
import org.freehep.graphicsio.emf.MoveToEx;
import org.freehep.graphicsio.emf.Pie;
import org.freehep.graphicsio.emf.SetTextColor;
import org.freehep.graphicsio.emf.TextW;
import org.freehep.util.io.Tag;

import com.tle.conversion.emf.StretchDIBits;

/**
 * A convenience class for converting an EMF graphic to a JPEG format file.
 * Modified by Charles O'Farrell on 1/08/2005. Added extra implementation to
 * ensure display of some specific emf images.
 * 
 * @author cofarrell
 */
public class EMFExport implements Export
{
	private Point currentPosition;

	/**
	 * Not used (but could be if fully implemented)
	 */
	// private Shape currentShape;
	public EMFExport()
	{
		// Nothing to do here.
	}

	/**
	 * Reads an EMF file and converts it to JPEG format saving it to
	 * <code>out</code>.
	 * 
	 * @see com.dytech.export.Export#exportFile(String, String)
	 */
	@Override
	public void exportFile(String in, String out) throws IOException
	{
		// Convert:
		FileInputStream fis = new FileInputStream(in);
		EMFInputStream emf = new ExportEMFInputStream(fis);
		Vector<Tag> tags = new Vector<Tag>();
		Rectangle bounds = convert(tags, emf);

		// Write the output as a JPEG:
		// Rectangle bounds = display.getBounds();
		BufferedImage buf = new BufferedImage(bounds.width + bounds.x, bounds.height + bounds.y,
			BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) buf.getGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, bounds.width + bounds.x, bounds.height + bounds.y);
		paint(g2, tags);

		ImageIO.write(buf, "jpeg", new File(out)); //$NON-NLS-1$
	}

	private static class ExportEMFTagSet extends EMFTagSet
	{
		public ExportEMFTagSet(int version)
		{
			super(version);
			addTag(new StretchDIBits());
		}
	}

	private static class ExportEMFInputStream extends EMFInputStream
	{
		public ExportEMFInputStream(InputStream is) throws IOException
		{
			super(is, new ExportEMFTagSet(DEFAULT_VERSION));
		}

		/**
		 * @return null if buffer was completely read. Otherwise rest of buffer
		 *         is read and returned. Overridden due to problem if
		 *         implementation doesn't read all bits (ie StretchDIBits)
		 */
		@Override
		public byte[] popBuffer() throws IOException
		{
			int index = getIntField(this, "index");
			int[] size = (int[]) getField(this, "size");
			if( index >= 0 )
			{
				int len = size[index];
				if( len > 0 )
				{
					readByte(len);
				}
				else if( len < 0 )
				{
					System.err.println("ByteCountInputStream: Internal Error");
				}
				index--;
				setField(this, "index", new Integer(index));
			}
			return null;
		}

	}

	private Rectangle convert(Vector<Tag> tags, EMFInputStream is) throws IOException
	{
		// set the size
		EMFHeader header = is.readHeader();
		Rectangle bounds = header.getBounds();

		Tag tag = is.readTag();
		while( tag != null )
		{
			try
			{
				tags.add(tag);
				tag = is.readTag();
			}
			catch( IOException e )
			{
				System.err.println("Error reading tag" + e.toString());
			}
		}

		return bounds;
	}

	private void paint(Graphics g, Vector<Tag> tags)
	{
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int size = tags.size();
		for( int i = 0; i < size; ++i )
		{
			Tag tag = tags.get(i);
			if( tag != null )
			{
				map(tag, g2);
			}
		}
	}

	/**
	 * the mapping function EMF tags -> java2d methods
	 */
	private void map(Tag tag, Graphics2D g2)
	{
		if( tag instanceof Arc )
		{
			// The Arc function draws an elliptical arc.
			//
			// BOOL Arc(
			// HDC hdc, // handle to device context
			// int nLeftRect, // x-coord of rectangle's upper-left corner
			// int nTopRect, // y-coord of rectangle's upper-left corner
			// int nRightRect, // x-coord of rectangle's lower-right corner
			// int nBottomRect, // y-coord of rectangle's lower-right corner
			// int nXStartArc, // x-coord of first radial ending point
			// int nYStartArc, // y-coord of first radial ending point
			// int nXEndArc, // x-coord of second radial ending point
			// int nYEndArc // y-coord of second radial ending point
			// );
			// The points (nLeftRect, nTopRect) and (nRightRect, nBottomRect)
			// specify the bounding rectangle.
			// An ellipse formed by the specified bounding rectangle defines
			// the curve of the arc.
			// The arc extends in the current drawing direction from the point
			// where it intersects the
			// radial from the center of the bounding rectangle to the
			// (nXStartArc, nYStartArc) point.
			// The arc ends where it intersects the radial from the center of
			// the bounding rectangle to
			// the (nXEndArc, nYEndArc) point. If the starting point and ending
			// point are the same,
			// a complete ellipse is drawn.
			Arc arc = (Arc) tag;

			// normalize start and end point to a circle
			double nx0 = arc.getStart().x / arc.getBounds().width;

			// double ny0 = arc.getStart().y / arc.getBounds().height;
			double nx1 = arc.getEnd().x / arc.getBounds().width;

			// double ny1 = arc.getEnd().y / arc.getBounds().height;
			// calculate angle of start point
			double alpha0 = Math.acos(nx0);
			double alpha1 = Math.acos(nx1);

			Arc2D arc2d = new Arc2D.Double(arc.getStart().x, arc.getStart().y, arc.getBounds().width,
				arc.getBounds().height, alpha0, alpha1 - alpha0, Arc2D.OPEN);
			// currentShape = arc2d;
			g2.draw(arc2d);
		}
		else if( tag instanceof ArcTo )
		{
			// The ArcTo function draws an elliptical arc.
			//
			// BOOL ArcTo(
			// HDC hdc, // handle to device context
			// int nLeftRect, // x-coord of rectangle's upper-left corner
			// int nTopRect, // y-coord of rectangle's upper-left corner
			// int nRightRect, // x-coord of rectangle's lower-right corner
			// int nBottomRect, // y-coord of rectangle's lower-right corner
			// int nXRadial1, // x-coord of first radial ending point
			// int nYRadial1, // y-coord of first radial ending point
			// int nXRadial2, // x-coord of second radial ending point
			// int nYRadial2 // y-coord of second radial ending point
			// );
			// ArcTo is similar to the Arc function, except that the current
			// position is updated.
			//
			// The points (nLeftRect, nTopRect) and (nRightRect, nBottomRect)
			// specify the bounding rectangle.
			// An ellipse formed by the specified bounding rectangle defines
			// the curve of the arc. The arc extends
			// counterclockwise from the point where it intersects the radial
			// line from the center of the bounding
			// rectangle to the (nXRadial1, nYRadial1) point. The arc ends
			// where it intersects the radial line from
			// the center of the bounding rectangle to the (nXRadial2,
			// nYRadial2) point. If the starting point and
			// ending point are the same, a complete ellipse is drawn.
			//
			// A line is drawn from the current position to the starting point
			// of the arc.
			// If no error occurs, the current position is set to the ending
			// point of the arc.
			//
			// The arc is drawn using the current pen; it is not filled.
			ArcTo arc = (ArcTo) tag;

			// normalize start and end point to a circle
			double nx0 = arc.getStart().x / arc.getBounds().width;

			// double ny0 = arc.getStart().y / arc.getBounds().height;
			double nx1 = arc.getEnd().x / arc.getBounds().width;

			// double ny1 = arc.getEnd().y / arc.getBounds().height;
			// calculate angle of start point
			double alpha0 = Math.acos(nx0);
			double alpha1 = Math.acos(nx1);

			// update currentPosition
			currentPosition = arc.getEnd();

			Arc2D arc2d = new Arc2D.Double(arc.getStart().x, arc.getStart().y, arc.getBounds().width,
				arc.getBounds().height, alpha0, alpha1 - alpha0, Arc2D.OPEN);
			// currentShape = arc2d;
			g2.draw(arc2d);
		}
		// else if( tag instanceof BeginPath )
		// {
		// The BeginPath function opens a path bracket in the specified
		// device context.
		// currentShape = null;
		// }
		else if( tag instanceof Chord )
		{
			// The Chord function draws a chord (a region bounded by the
			// intersection of an
			// ellipse and a line segment, called a secant). The chord is
			// outlined by using the
			// current pen and filled by using the current brush.
			Chord arc = (Chord) tag;

			// normalize start and end point to a circle
			double nx0 = arc.getStart().x / arc.getBounds().width;

			// double ny0 = arc.getStart().y / arc.getBounds().height;
			double nx1 = arc.getEnd().x / arc.getBounds().width;

			// double ny1 = arc.getEnd().y / arc.getBounds().height;
			// calculate angle of start point
			double alpha0 = Math.acos(nx0);
			double alpha1 = Math.acos(nx1);

			// update currentPosition
			currentPosition = arc.getEnd();

			Arc2D arc2d = new Arc2D.Double(arc.getStart().x, arc.getStart().y, arc.getBounds().width,
				arc.getBounds().height, alpha0, alpha1 - alpha0, Arc2D.CHORD);
			// currentShape = arc2d;
			g2.draw(arc2d);
		}
		// else if( tag instanceof CloseFigure )
		// {
		// The CloseFigure function closes an open figure in a path.
		// }
		else if( tag instanceof CreatePen )
		{
			// CreatePen
			//
			// The CreatePen function creates a logical pen that has the
			// specified style, width, and color.
			// The pen can subsequently be selected into a device context and
			// used to draw lines and curves.
			//
			// HPEN CreatePen(
			// int fnPenStyle, // pen style
			// int nWidth, // pen width
			// COLORREF crColor // pen color
			// );
			CreatePen cpen = (CreatePen) tag;
			LogPen lpen = cpen.getPen();

			float[] dash = null;
			if( lpen.getPenStyle() == EMFConstants.PS_DASH )
			{
				dash = new float[]{5, 5};
			}
			else if( lpen.getPenStyle() == EMFConstants.PS_DASHDOT )
			{
				dash = new float[]{5, 2, 1, 2};
			}
			else if( lpen.getPenStyle() == EMFConstants.PS_DASHDOTDOT )
			{
				dash = new float[]{5, 2, 1, 2, 1, 2};
			}
			else if( lpen.getPenStyle() == EMFConstants.PS_DOT )
			{
				dash = new float[]{1, 2};
			}
			else if( lpen.getPenStyle() == EMFConstants.PS_SOLID )
			{
				dash = new float[]{1};
			}
			// else
			// {
			// LOGGER.warn("got unsupported pen style " +
			// lpen.getPenStyle());
			// }

			BasicStroke bs = new BasicStroke(lpen.getWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, dash,
				0);
			g2.setStroke(bs);
		}
		else if( tag instanceof Ellipse )
		{
			// The Ellipse function draws an ellipse. The center of the ellipse
			// is the center of the specified bounding rectangle.
			// The ellipse is outlined by using the current pen and is filled
			// by using the current brush.
			// The current position is neither used nor updated by Ellipse.
			Ellipse el = (Ellipse) tag;

			Ellipse2D el2 = new Ellipse2D.Double(el.getBounds().getX(), el.getBounds().getY(), el.getBounds()
				.getWidth(), el.getBounds().getHeight());
			// currentShape = el2;
			g2.draw(el2);
		}
		else if( tag instanceof LineTo )
		{
			// The LineTo function draws a line from the current position up
			// to, but not including, the specified point.
			// The line is drawn by using the current pen and, if the pen is a
			// geometric pen, the current brush.
			LineTo lineTo = (LineTo) tag;
			Line2D l2 = new Line2D.Double(currentPosition, lineTo.getPoint());
			g2.draw(l2);
			// currentShape = l2;
		}
		else if( tag instanceof MoveToEx )
		{
			// The MoveToEx function updates the current position to the
			// specified point
			// and optionally returns the previous position.
			MoveToEx mte = (MoveToEx) tag;
			currentPosition = mte.getPoint();
		}
		else if( tag instanceof Pie )
		{
			Pie arc = (Pie) tag;

			// normalize start and end point to a circle
			double nx0 = arc.getStart().x / arc.getBounds().width;

			// double ny0 = arc.getStart().y / arc.getBounds().height;
			double nx1 = arc.getEnd().x / arc.getBounds().width;

			// double ny1 = arc.getEnd().y / arc.getBounds().height;
			// calculate angle of start point
			double alpha0 = Math.acos(nx0);
			double alpha1 = Math.acos(nx1);

			Arc2D arc2d = new Arc2D.Double(arc.getStart().x, arc.getStart().y, arc.getBounds().width,
				arc.getBounds().height, alpha0, alpha1 - alpha0, Arc2D.PIE);
			// currentShape = arc2d;
			g2.draw(arc2d);
		}
		else if( tag instanceof StretchDIBits )
		{
			StretchDIBits stretch = (StretchDIBits) tag;
			Rectangle bounds = stretch.getBounds();
			BufferedImage buf = stretch.getImage();
			Image scaled = buf.getScaledInstance(bounds.width, bounds.height, Image.SCALE_SMOOTH);
			g2.drawImage(scaled, bounds.x, bounds.y, Color.white, null);
		}

		// / EXTRA ////

		else if( tag instanceof FillPath )
		{
			FillPath path = (FillPath) tag;
			Rectangle bounds = path.getBounds();
			Rectangle2D rect = new Rectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height);
			g2.fill(rect);
			// currentShape = rect;
		}
		else if( tag instanceof EMFRectangle )
		{
			EMFRectangle emrect = (EMFRectangle) tag;
			Rectangle bounds = (Rectangle) getField(emrect, "bounds");
			Rectangle2D rect = new Rectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height);
			g2.draw(rect);
			// currentShape = rect;
		}
		else if( tag instanceof CreateBrushIndirect )
		{
			CreateBrushIndirect brush = (CreateBrushIndirect) tag;
			LogBrush32 brush32 = brush.getBrush();

			Color color = brush32.getColor();
			// int hatch = brush32.getHatch();
			// int style = brush32.getStyle();
			g2.setColor(color);
		}
		else if( tag instanceof ExtCreateFontIndirectW )
		{
			ExtCreateFontIndirectW extcFont = (ExtCreateFontIndirectW) tag;
			ExtLogFontW efont = (ExtLogFontW) getField(extcFont, "font");
			LogFontW font = (LogFontW) getField(efont, "font");
			String family = (String) getField(font, "faceFamily");
			int style = 0;
			boolean italic = getBooleanField(font, "italic");
			int size = getIntField(font, "height");
			if( italic )
			{
				style |= Font.ITALIC;
			}
			Font gFont = new Font(family, style, Math.abs(size));
			g2.setFont(gFont);
		}
		else if( tag instanceof SetTextColor )
		{
			SetTextColor textColour = (SetTextColor) tag;
			Color color = textColour.getColor();
			g2.setColor(color);
		}
		else if( tag instanceof ExtTextOutW )
		{
			ExtTextOutW textout = (ExtTextOutW) tag;
			// Rectangle bounds = (Rectangle)getField(textout, "bounds");
			// int mode = getIntField(textout, "mode");
			// float xScale = getFloatField(textout, "xScale");
			// float yScale = getFloatField(textout, "yScale");
			TextW text = (TextW) getField(textout, "text");

			Point pos = (Point) getField(text, "pos");
			String string = (String) getField(text, "string");
			// int options = getIntField(text, "options");
			// int[] widths = (int[])getField(text,"widths");
			// Rectangle textbounds = (Rectangle)getField(text, "bounds");
			g2.drawString(string, pos.x, pos.y);
		}
		// else if( tag instanceof EOF )
		// {
		// IGNORE
		// }

		// NOT SUPPORTED //

		// else if( tag instanceof PolyBezierTo16 )
		// {
		// PolyBezierTo16 poly = (PolyBezierTo16)tag;
		// Rectangle bounds = (Rectangle)getField(poly, "bounds");
		// int numberOfPoints = ((Integer)getField(poly,
		// "numberOfPoints")).intValue();
		// Point[] points = (Point[])getField(poly, "points");
		// }
		// else if( tag instanceof EndPath )
		// {
		// EndPath path = (EndPath)tag;
		// }
		// else if( tag instanceof SelectObject )
		// {
		// SelectObject object = (SelectObject) tag;
		// int index = object.getIndex();
		// }
		// else if( tag instanceof DeleteObject )
		// {
		// DeleteObject object = (DeleteObject) tag;
		// int index = object.getIndex();
		// }
		// else if( tag instanceof SetPolyFillMode )
		// {
		// SetPolyFillMode fmode = (SetPolyFillMode)tag;
		// int mode = ((Integer)getField(fmode, "mode")).intValue();
		// }
		// else if( tag instanceof ExtSelectClipRgn )
		// {
		// ExtSelectClipRgn cliprgn = (ExtSelectClipRgn)tag;
		// int mode = ((Integer)getField(cliprgn, "mode")).intValue();
		// Region rgn = (Region)getField(cliprgn, "rgn");
		// }
		// else if( tag instanceof SetStretchBltMode )
		// {
		// SetStretchBltMode smode = (SetStretchBltMode)tag;
		// int mode = ((Integer)getField(smode, "mode")).intValue();
		// }
		// else if( tag instanceof SetBkMode )
		// {
		// SetBkMode bkmode = (SetBkMode) tag;
		// int mode = getIntField(bkmode, "mode");
		// }
		// else if( tag instanceof SetTextAlign )
		// {
		// SetTextAlign align = (SetTextAlign)tag;
		// int mode = getIntField(align, "mode");
		// }
		// else
		// {
		// LOGGER.warn("tag " + tag.getClass() + " not supported");
		// }
	}

	float getFloatField(Object o, String name)
	{
		return ((Float) getField(o, name)).floatValue();
	}

	static int getIntField(Object o, String name)
	{
		return ((Integer) getField(o, name)).intValue();
	}

	static boolean getBooleanField(Object o, String name)
	{
		return ((Boolean) getField(o, name)).booleanValue();
	}

	static Object getField(Object o, String name)
	{
		Field field = getField(o.getClass(), name);
		try
		{
			return field.get(o);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	static void setField(Object o, String name, Object value)
	{
		try
		{
			Field field = getField(o.getClass(), name);
			field.set(o, value);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private static Field getField(Class<?> c, String name)
	{
		Field field = null;

		while( field == null && c != null )
		{
			try
			{
				field = c.getDeclaredField(name);
				field.setAccessible(true);
			}
			catch( Exception ex )
			{
				c = c.getSuperclass();
			}
		}
		if( field == null )
		{
			throw new RuntimeException(name + " doesn't exist in class " + c);
		}
		return field;
	}

	@Override
	public Collection<String> getInputTypes()
	{
		return Collections.singleton("emf");

	}

	@Override
	public Collection<String> getOutputTypes()
	{
		return Collections.singleton("jpeg");
	}
}
