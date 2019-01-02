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

package com.tle.web.inplaceeditor.win32;

import java.awt.Component;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;
import java.util.logging.Logger;

import com.tle.web.inplaceeditor.Opener;

@SuppressWarnings("nls")
public class WindowsOpener implements Opener
{
	private static final Logger LOGGER = Logger.getLogger(WindowsOpener.class.getName());

	private final Shell32Ex shell32 = Shell32Ex.SHELL32;

	// private final User32Ex user32 = User32Ex.USER32;

	// private static final int GW_HWNDNEXT = 2;

	@Override
	public void openWith(Component parent, final String filepath, String mimetype)
	{
		LOGGER.finest("OS is windows.  Invoking ShellExecute with shell32.dll,OpenAs_RunDLL " + filepath);

		final Permissions permissions = new Permissions();
		permissions.add(new PropertyPermission("w32.ascii", "read"));
		LOGGER.finest("Added read PropertyPermission for 'w32.ascii'");

		permissions.add(new PropertyPermission("jna.boot.library.path", "read"));
		LOGGER.finest("Added read PropertyPermission for 'jna.boot.library.path'");

		permissions.add(new PropertyPermission("os.arch", "read"));
		LOGGER.finest("Added read PropertyPermission for 'os.arch'");

		permissions.add(new PropertyPermission("os.name", "read"));
		LOGGER.finest("Added read PropertyPermission for 'os.name'");

		permissions.add(new RuntimePermission("loadLibrary.jnidispatch"));
		LOGGER.finest("Added read RuntimePermission for 'loadLibrary.jnidispatch'");

		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});
		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				// HINSTANCE res =
				shell32.ShellExecute(0, null, "rundll32", "shell32.dll,OpenAs_RunDLL " + filepath, null, 1);
				// HWND hWnd = GetWinHandle(res);
				// if( hWnd != null )
				// {
				// user32.SetFocus(hWnd);
				// }
				return null;
			}
		}, context);
	}

	// private HWND GetWinHandle(HINSTANCE inst)
	// {
	// long lInst = Pointer.nativeValue(inst.getPointer());
	// // Grab the first window handle that Windows finds:
	// HWND tempHwnd = user32.FindWindow(null, null);
	//
	// // Loop until you find a match or there are no more window handles:
	// while( tempHwnd != null )
	// {
	// // Check if no parent for this window
	// if( user32.GetParent(tempHwnd) == null )
	// {
	// IntByReference otherInst = new IntByReference();
	// user32.GetWindowThreadProcessId(tempHwnd, otherInst);
	// // Check for PID match
	// if( lInst == otherInst.getValue() )
	// {
	// // Return found handle
	// return tempHwnd;
	// }
	// }
	//
	// // Get the next window handle
	// tempHwnd = user32.GetWindow(tempHwnd, GW_HWNDNEXT);
	// }
	// return null;
	// }
}
