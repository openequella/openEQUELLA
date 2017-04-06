/*
 * Created on 1/12/2005
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
