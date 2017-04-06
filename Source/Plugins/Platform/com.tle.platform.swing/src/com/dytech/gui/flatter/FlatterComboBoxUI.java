package com.dytech.gui.flatter;

import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class FlatterComboBoxUI extends BasicComboBoxUI implements Serializable
{
	public static ComponentUI createUI(JComponent c)
	{
		return new FlatterComboBoxUI();
	}

	@Override
	protected JButton createArrowButton()
	{
		return new FlatterIcons.ArrowButton(SwingConstants.SOUTH);
	}
}
