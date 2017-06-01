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

package com.tle.admin;

import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tle.common.applet.client.ClientService;

public abstract class AdminTool
{
	protected JFrame parentFrame;
	protected JPanel managementPanel;
	protected Driver driver;
	protected ClientService clientService;

	public abstract void toolSelected();

	public abstract void setup(Set<String> grantedPrivilges, String name);

	public void setParent(JFrame f)
	{
		parentFrame = f;
	}

	public JFrame getParentFrame()
	{
		return parentFrame;
	}

	public void setManagementPanel(JPanel p)
	{
		managementPanel = p;
	}

	public final void setDriver(Driver driver)
	{
		this.driver = driver;
		this.clientService = driver.getClientService();
	}
}
