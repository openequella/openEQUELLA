package com.tle.admin.security.tree;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Nicholas Read
 */
public class MessagePanel extends JPanel implements SecurityTreeTab
{
	private static final long serialVersionUID = 1L;

	public MessagePanel(String message)
	{
		JLabel label = new JLabel(message);
		label.setHorizontalAlignment(SwingConstants.CENTER);

		setLayout(new GridLayout(1, 1));
		add(label);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeTab#hasChanges()
	 */
	@Override
	public boolean hasChanges()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeTab#saveChanges()
	 */
	@Override
	public void saveChanges()
	{
		// Nothing to save
	}
}
