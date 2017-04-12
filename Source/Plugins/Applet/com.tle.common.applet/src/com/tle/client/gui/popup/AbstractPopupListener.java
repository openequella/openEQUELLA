package com.tle.client.gui.popup;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * @author Nicholas Read
 */
public abstract class AbstractPopupListener extends MouseAdapter
{
	private final JPopupMenu menu;

	public AbstractPopupListener(Action... actions)
	{
		menu = new JPopupMenu();
		for( Action action : actions )
		{
			menu.add(new JMenuItem(action));
		}
	}

	public AbstractPopupListener(Collection<? extends Action> actions)
	{
		menu = new JPopupMenu();
		for( Action action : actions )
		{
			menu.add(new JMenuItem(action));
		}
	}

	public AbstractPopupListener(JPopupMenu menu)
	{
		this.menu = menu;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		checkPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		checkPopup(e);
	}

	private void checkPopup(MouseEvent e)
	{
		if( e.isPopupTrigger() )
		{
			selectItemUnderMouse(e);

			Component source = (Component) e.getSource();
			menu.show(source, e.getX(), e.getY());
		}
	}

	public abstract void selectItemUnderMouse(MouseEvent e);
}
