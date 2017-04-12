package com.tle.admin.hierarchy;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.tle.common.i18n.CurrentLocale;

/**
 * A mostly blank panel used to inidicate when no topic has been selected in the
 * hierarchy tool.
 * 
 * @author Nicholas Read
 * @created 4 June 2003
 */
@SuppressWarnings("nls")
public class NoTopicEditor extends JPanel
{
	private static final long serialVersionUID = 1L;

	public static NoTopicEditor noTopicSelected()
	{
		return new NoTopicEditor("noneselected");
	}

	public static NoTopicEditor notEditable()
	{
		return new NoTopicEditor("noteditable");
	}

	public NoTopicEditor(String message)
	{
		JLabel messageLabel = new JLabel(CurrentLocale.get("com.tle.admin.hierarchy.tool.notopic." + message));
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

		setLayout(new GridLayout(1, 1));
		add(messageLabel);
	}
}
