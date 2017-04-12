/*
 * Created on Mar 1, 2005
 */
package com.tle.admin.usermanagement.ldap;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.tle.beans.usermanagement.standard.LDAPSettings;

public abstract class AbstractLDAPPanel extends JPanel implements Changeable
{
	protected ChangeDetector changeDetector;
	protected LDAPSettings settings;

	public AbstractLDAPPanel()
	{
		changeDetector = new ChangeDetector();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	public boolean needsScrollPane()
	{
		return false;
	}

	public JComponent getComponent()
	{
		return this;
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	protected abstract String getTabName();

	public abstract void applySettings() throws Exception;

	public void showPanel() throws Exception
	{
		// Nothing to do here
	}

	@Override
	public String getName()
	{
		return getTabName();
	}
}
