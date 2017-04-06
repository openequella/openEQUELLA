package com.dytech.edge.installer.application;

import javax.swing.JOptionPane;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;

/**
 * @author Nicholas Read
 */
public class DatabaseHostCallback implements Callback
{
	/**
	 * Class constructor.
	 */
	public DatabaseHostCallback()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
	 */
	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();

		String host = output.getNode("datasource/host").trim();

		int first = host.indexOf(':');
		int last = host.lastIndexOf(':');
		if( first >= 0 && first != last )
		{
			JOptionPane.showMessageDialog(installer.getFrame(),
				"Your database hostname is" + " malformed.  You must enter a valid hostname.\nIf you do not specify a"
					+ " port number, then the default will be used.\nIf the database is running"
					+ " on a port different to the default, then you\nmust specify it in the format"
					+ " {hostname}:{port}", "Hostname Invalid", JOptionPane.ERROR_MESSAGE);
			return;
		}

		installer.gotoRelativePage(1);
	}
}
