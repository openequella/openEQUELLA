/*
 * JFlatDialog.java Created on October 1, 2002, 9:34 AM
 */

package com.dytech.edge.importexport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

@Deprecated
class JFlatDialog extends JDialog
{
	private JPanel mainPanel;

	/**
	 * Creates a non-modal dialog with the specified title and with the
	 * specified owner dialog.
	 */
	public JFlatDialog(Dialog owner, String title)
	{
		super(owner, title);

		setUndecorated(true);

		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.setBackground(new Color(204, 204, 204));
		pane.setBorder(new LineBorder(new Color(102, 102, 102), 1, true));
		setContentPane(pane);
		setSize(50, 24);

		// Setup the title bar:
		final JFlatTitleBar titleBar = new JFlatTitleBar(this, getTitle());
		pane.add(titleBar, BorderLayout.NORTH);

		// Setup the main panel:
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		pane.add(mainPanel, BorderLayout.CENTER);

		this.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				Title t = titleBar.getTitle();

				// Ensure the widths are large enough:
				Dimension pref = t.getPreferredSize();
				Dimension max = t.getMaximumSize();
				Dimension min = t.getMinimumSize();

				pref.width *= 1.1;
				max.width *= 1.1;
				min.width *= 1.1;

				t.setPreferredSize(pref);
				t.setMaximumSize(max);
				t.setMinimumSize(min);

				titleBar.revalidate();
			}
		});
	}

	/**
	 * Returns the main container of the dialog. Overrides
	 * <code>getContentPane</code> in JDialog.
	 */
	@Override
	public Container getContentPane()
	{
		return mainPanel;
	}

	protected Container getTrueContentPane()
	{
		return super.getContentPane();
	}

	public void revalidate()
	{
		invalidate();
		super.getContentPane().invalidate();
		doLayout();
		repaint();
		((JPanel) super.getContentPane()).revalidate();
	}

	@Override
	public void doLayout()
	{
		super.doLayout();
		super.getContentPane().doLayout();
		mainPanel.doLayout();
	}

}

@Deprecated
class JFlatTitleBar extends JPanel
{
	private Point clickPos, oldPos;
	private Title jlTitle;

	public JFlatTitleBar(final JDialog parent, String title)
	{
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				// Get mouse and parent coords in parenet's coord space:
				Point parLoc = parent.getLocation();
				clickPos = new Point(e.getX() + parLoc.x, e.getY() + parLoc.y);
				oldPos = new Point(parLoc);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				Point pos = new Point(oldPos);

				// Get the mouse current location in parent's coord space:
				Point parLoc = parent.getLocation();
				Point mouse = e.getPoint();
				mouse.x += parLoc.x;
				mouse.y += parLoc.y;
				pos.translate(mouse.x - clickPos.x, mouse.y - clickPos.y);
				parent.setLocation(pos);
			}
		});

		setPreferredSize(new Dimension(getPreferredSize().width, 20));
		setMinimumSize(new Dimension(1, 24));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		setBackground(new Color(62, 90, 141));

		// Setup the title:
		jlTitle = new Title(title);
		jlTitle.setForeground(Color.white);
		jlTitle.setBorder(new EmptyBorder(0, 3, 0, 3));

		setLayout(new BorderLayout());
		add(jlTitle);
	}

	public Title getTitle()
	{
		return jlTitle;
	}
}

@Deprecated
@SuppressWarnings("nls")
class Title extends JLabel
{
	private static final Image TEXT_BACK = new ImageIcon(JFlatDialog.class.getResource("TextBackRep.jpeg")).getImage();
	private static final Image transition = new ImageIcon(JFlatDialog.class.getResource("Transition.jpeg")).getImage();

	public Title(String text)
	{
		setBorder(new EmptyBorder(0, 8, 0, 0));
		setOpaque(false);
		setText(text);
		setBackground(Color.BLACK);
	}

	@Override
	public void paint(Graphics g)
	{
		// Draw the background:
		if( getBackground() != null )
		{
			Rectangle clip = g.getClipBounds();
			g.setColor(getBackground());
			g.fillRect(clip.x, clip.y, clip.width, clip.height);
		}

		// Draw the backing to the text:
		int transX = (int) g.getFontMetrics().getStringBounds(getText(), g).getWidth() + getInsets().left;

		if( TEXT_BACK != null && TEXT_BACK.getWidth(null) > 0 )
		{
			for( int i = 0; i < transX; i += TEXT_BACK.getWidth(null) )
				g.drawImage(TEXT_BACK, i, 0, null);
		}

		// Draw transition:
		if( transition != null )
		{
			g.drawImage(transition, transX, 0, null);
		}

		// Draw the text:
		super.paint(g);
	}
}