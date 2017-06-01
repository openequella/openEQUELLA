/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
