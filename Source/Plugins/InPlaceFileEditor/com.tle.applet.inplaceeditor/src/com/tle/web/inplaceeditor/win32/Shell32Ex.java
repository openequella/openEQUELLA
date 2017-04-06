package com.tle.web.inplaceeditor.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author Aaron
 */
public interface Shell32Ex extends Shell32
{
	@SuppressWarnings("nls")
	Shell32Ex SHELL32 = (Shell32Ex) Native.loadLibrary("shell32", Shell32Ex.class, W32APIOptions.UNICODE_OPTIONS);

	HINSTANCE ShellExecute(int hWnd, String lpVerb, String lpFile, String lpParameters, String lpDirectory, int nShow);
}
