/*
 * Copyright 2019 Apereo
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
