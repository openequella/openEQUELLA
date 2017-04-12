package com.tle.client.gui.popup;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * @author Nicholas Read
 */
public class TablePopupListener extends AbstractPopupListener
{
	private final JTable table;

	public TablePopupListener(JTable table, Action... actions)
	{
		super(actions);
		this.table = table;
	}

	public TablePopupListener(JTable table, List<? extends Action> actions)
	{
		super(actions);
		this.table = table;
	}

	public TablePopupListener(JTable table, JPopupMenu menu)
	{
		super(menu);
		this.table = table;
	}

	@Override
	public void selectItemUnderMouse(MouseEvent e)
	{
		table.editingCanceled(null);
		int row = table.rowAtPoint(e.getPoint());
		table.getSelectionModel().setSelectionInterval(row, row);
	}
}
