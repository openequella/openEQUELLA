package com.tle.client.gui.popup;

import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JTree;

/**
 * @author Nicholas Read
 */
public class TreeDoubleClickListener extends AbstractDoubleClickListener
{
	private final JTree tree;

	public TreeDoubleClickListener(JTree tree, Action action)
	{
		super(action);
		this.tree = tree;
	}

	@Override
	public void selectItemUnderMouse(MouseEvent e)
	{
		tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
	}
}
