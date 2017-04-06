package com.tle.client.gui.popup;

import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JList;

/**
 * @author Nicholas Read
 */
public class ListDoubleClickListener extends AbstractDoubleClickListener
{
	private final JList list;

	public ListDoubleClickListener(JList list, Action action)
	{
		super(action);
		this.list = list;
	}

	@Override
	public void selectItemUnderMouse(MouseEvent e)
	{
		int row = list.locationToIndex(e.getPoint());
		list.getSelectionModel().setSelectionInterval(row, row);
	}
}
