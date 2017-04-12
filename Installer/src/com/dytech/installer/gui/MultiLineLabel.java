package com.dytech.installer.gui;

import javax.swing.JTextArea;
import javax.swing.LookAndFeel;

/**
 * Overcomes the problem of multiple lines in a <code>JLabel</code>. Is actually
 * a <code>JTextArea</code> in disguise using a new Look & Feel.
 * 
 * @author Nicholas Read
 */
public class MultiLineLabel extends JTextArea
{
	private static final String LABEL_BORDER = "Label.border";
	private static final String LABEL_BACKGROUND = "Label.background";
	private static final String LABEL_FOREGROUND = "Label.foreground";
	private static final String LABEL_FONT = "Label.font";

	/**
	 * Constructor for <code>MultiLineLabel</code>
	 */
	public MultiLineLabel()
	{
		super();
	}

	/**
	 * Constructor for <code>MultiLineLabel</code> with text.
	 */
	public MultiLineLabel(String text)
	{
		super(text);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#updateUI()
	 */
	@Override
	public void updateUI()
	{
		super.updateUI();
		setLineWrap(true);
		setWrapStyleWord(true);
		setHighlighter(null);
		setEditable(false);
		setFocusable(false);
		LookAndFeel.installBorder(this, LABEL_BORDER);
		LookAndFeel.installColorsAndFont(this, LABEL_BACKGROUND, LABEL_FOREGROUND, LABEL_FONT);
	}
}
