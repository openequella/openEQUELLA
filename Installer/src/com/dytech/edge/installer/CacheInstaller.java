package com.dytech.edge.installer;

import java.io.InputStream;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Installer;
import com.dytech.installer.InstallerException;

public class CacheInstaller
{
	public CacheInstaller() throws InstallerException
	{
		InputStream script = getClass().getResourceAsStream("/script/cache-script.xml");
		InputStream commands = getClass().getResourceAsStream("/script/cache-commands.xml");

		new Installer(new PropBagEx(script), new PropBagEx(commands));
	}

	public static void main(String[] args) throws Exception
	{
		new CacheInstaller();
	}
}
