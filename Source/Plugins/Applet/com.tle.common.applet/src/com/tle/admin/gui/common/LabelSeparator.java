package com.tle.admin.gui.common;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import com.dytech.gui.TableLayout;

public class LabelSeparator extends JComponent
{
	private static final long serialVersionUID = 1L;

	public LabelSeparator(String text)
	{
		this(new JLabel(text));
	}

	public LabelSeparator(JLabel label)
	{
		JSeparator separator = new JSeparator();

		final int height1 = label.getPreferredSize().height;
		final int height2 = separator.getPreferredSize().height;
		final int height3 = (height1 - height2) / 2;
		final int width1 = label.getPreferredSize().width;

		final int[] rows = {height3, height2, height3,};
		final int[] cols = {width1, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		add(label, new Rectangle(0, 0, 1, 3));
		add(separator, new Rectangle(1, 1, 1, 1));
	}
}
