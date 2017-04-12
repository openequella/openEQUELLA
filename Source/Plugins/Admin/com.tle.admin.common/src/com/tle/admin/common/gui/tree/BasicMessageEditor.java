package com.tle.admin.common.gui.tree;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.tle.admin.gui.EditorException;
import com.tle.common.LazyTreeNode;

/**
 * @author Nicholas Read
 */
public class BasicMessageEditor extends AbstractTreeNodeEditor
{
	public BasicMessageEditor(String message)
	{
		JLabel messageLabel = new JLabel(message);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

		setLayout(new GridLayout(1, 1));
		add(messageLabel);
	}

	// None of the abstract methods should be invoked, as this editor is does
	// nothing.

	@Override
	protected LazyTreeNode getUpdatedNode()
	{
		throw new RuntimeException();
	}

	@Override
	protected void save()
	{
		throw new RuntimeException();
	}

	@Override
	protected void validation() throws EditorException
	{
		// nothing to validate
	}
}
