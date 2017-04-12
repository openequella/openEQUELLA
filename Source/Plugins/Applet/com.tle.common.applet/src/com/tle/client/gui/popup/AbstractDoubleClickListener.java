package com.tle.client.gui.popup;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;

/**
 * @author Nicholas Read
 */
public abstract class AbstractDoubleClickListener extends MouseAdapter
{
	private final Action action;

	private boolean checkActionEnabled = true;

	public AbstractDoubleClickListener(Action action)
	{
		this.action = action;
	}

	public void setCheckActionEnabled(boolean checkActionEnabled)
	{
		this.checkActionEnabled = checkActionEnabled;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 )
		{
			selectItemUnderMouse(e);

			if( !checkActionEnabled || action.isEnabled() )
			{
				String actionCommand = (String) action.getValue(Action.ACTION_COMMAND_KEY);
				action.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, actionCommand, e
					.getWhen(), e.getModifiers()));
			}
		}
	}

	public abstract void selectItemUnderMouse(MouseEvent e);
}
