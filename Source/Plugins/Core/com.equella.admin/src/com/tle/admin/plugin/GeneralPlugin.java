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

package com.tle.admin.plugin;

import java.awt.Dialog;

import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JNameValuePanel;
import com.tle.common.applet.client.ClientService;

public abstract class GeneralPlugin<T> extends JNameValuePanel
{
	public Dialog parentDialog;
	public ClientService clientService;

	public abstract void load(T settings);

	public abstract boolean save(T settings) throws EditorException;

	@SuppressWarnings("unused")
	public void validation() throws EditorException
	{
		// TO BE OVERRIDEN
	}

	public void init()
	{
		// TO BE OVERRIDEN
	}

	public final void setParent(GeneralPlugin<?> parent)
	{
		clientService = parent.clientService;
		parentDialog = parent.parentDialog;
	}

	public boolean hasSave()
	{
		return true;
	}

	/**
	 * Sets the parenting dialog of the plugin.
	 */
	public final void setParent(Dialog dialog)
	{
		this.parentDialog = dialog;

	}

	public final void setClientService(ClientService clientService)
	{
		this.clientService = clientService;
	}

	public String getHelp()
	{
		return null;
	}

	public String getDocumentName()
	{
		return null;
	}
}
