/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
