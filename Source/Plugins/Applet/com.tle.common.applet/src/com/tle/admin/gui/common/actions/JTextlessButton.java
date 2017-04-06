package com.tle.admin.gui.common.actions;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * @author Nicholas Read
 */
public class JTextlessButton extends JButton
{
	private static final long serialVersionUID = 1L;

	public JTextlessButton(Action action)
	{
		putClientProperty("hideActionText", true); //$NON-NLS-1$
		setAction(action);
	}
}
