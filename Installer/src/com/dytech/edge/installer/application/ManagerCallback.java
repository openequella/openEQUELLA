package com.dytech.edge.installer.application;

import java.awt.Component;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JOptionPane;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;
import com.tle.common.Check;

public class ManagerCallback implements Callback
{
	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();
		String port = output.getNode("service/port"); //$NON-NLS-1$

		int result = JOptionPane.YES_OPTION;
		Component parent = installer.getFrame();
		if( Check.isEmpty(port) )
		{
			result = JOptionPane
				.showConfirmDialog(
					parent,
					"You have not specified a port for EQUELLA manager to run on. Should we use the default port (3000)?", "Warning", //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if( result == JOptionPane.YES_OPTION )
			{
				port = "3000";
			}
			else
			{
				return;
			}
		}

		try
		{
			ServerSocket socket = new ServerSocket(Integer.valueOf(port));
			socket.close();
		}
		catch( IOException e )
		{
			String message = "Your computer could not bind to the given port."
				+ "  Please ensure that\n no other services are running on this port.\n\n";
			JOptionPane.showMessageDialog(parent, message);
			return;
		}

		output.setNode("service/port", port);
		installer.gotoPage(installer.getCurrentPageNumber() + 1);
	}
}
