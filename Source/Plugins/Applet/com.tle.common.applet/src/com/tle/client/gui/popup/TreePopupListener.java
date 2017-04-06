package com.tle.client.gui.popup;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * @author Nicholas Read
 */
public class TreePopupListener extends AbstractPopupListener
{
	private final JTree tree;

	public TreePopupListener(JTree tree, Action... actions)
	{
		super(actions);
		this.tree = tree;
	}

	public TreePopupListener(JTree tree, Collection<? extends Action> actions)
	{
		super(actions);
		this.tree = tree;
	}

	public TreePopupListener(JTree tree, JPopupMenu menu)
	{
		super(menu);
		this.tree = tree;
	}

	@Override
	public void selectItemUnderMouse(MouseEvent e)
	{
		tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
	}
}
