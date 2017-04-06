package com.tle.admin.gui.common.actions;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
public abstract class TLEAction extends AbstractAction
{
	public TLEAction()
	{
		super();
	}

	public TLEAction(String displayName)
	{
		putValue(Action.NAME, displayName);
	}

	public TLEAction(String displayName, String icon)
	{
		this(displayName);
		setIcon(icon);
	}

	public void setShortDescription(String description)
	{
		putValue(Action.SHORT_DESCRIPTION, description);
	}

	public void setIcon(URL path)
	{
		Check.checkNotNull(path);

		putValue(Action.SMALL_ICON, new ImageIcon(path));
	}

	public void setIcon(Class<?> base, String path)
	{
		setIcon(base.getResource(path));
	}

	public void setIcon(String path)
	{
		setIcon(this.getClass(), path);
	}

	public void setMnemonic(int keyEvent)
	{
		putValue(MNEMONIC_KEY, keyEvent);
	}

	public void update()
	{
		// To be overridden
	}

	public KeyStroke invokeForWindowKeyStroke()
	{
		return null;
	}
}
