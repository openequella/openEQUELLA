package com.tle.client.gui.popup;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPopupMenu;

/**
 * @author Nicholas Read
 */
public class ListPopupListener extends AbstractPopupListener
{
	private final JList list;

	public ListPopupListener(JList list, Action... actions)
	{
		super(actions);
		this.list = list;
	}

	public ListPopupListener(JList list, List<? extends Action> actions)
	{
		super(actions);
		this.list = list;
	}

	public ListPopupListener(JList list, JPopupMenu menu)
	{
		super(menu);
		this.list = list;
	}

	@Override
	public void selectItemUnderMouse(MouseEvent e)
	{
		int row = list.locationToIndex(e.getPoint());
		list.getSelectionModel().setSelectionInterval(row, row);
	}
}
