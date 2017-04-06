package com.dytech.edge.installer.application;

import java.awt.HeadlessException;

import javax.swing.JOptionPane;

public class Launch
{
	public static void main(String[] args) throws Exception
	{
		if( System.getProperty("java.version").compareTo("1.6") < 0 )
		{
			try
			{
				JOptionPane.showMessageDialog(null,
					"Equella requires Java 6u1 or greater in order to perform the installation.");
			}
			catch( HeadlessException e )
			{
				System.out.println("");
				System.out.println("------------------------------------------------------------------------------");
				System.out.println("");
				System.out.println("The Equella requires a more recent version of Java.");
				System.out.println("Please ensure that JAVA_HOME points to version 6u1 or higher.");
				System.out.println("");
				System.out.println("------------------------------------------------------------------------------");
				System.out.println("");
			}
			return;
		}

		// No GENERICS or VARARGS! This has to be Java 1.4 compatible.

		Class mainClass = Class.forName("com.dytech.edge.installer.application.Main"); //$NON-NLS-1$
		Object mainObj = mainClass.newInstance();

		if( args.length == 2 && "--unsupported".equals(args[0]) )
		{
			mainClass.getMethod("setExistingResults", new Class[]{String.class}).invoke(mainObj, new Object[]{args[1]});
		}

		mainClass.getMethod("start", new Class[0]).invoke(mainObj, new Object[0]); //$NON-NLS-1$
	}
}
