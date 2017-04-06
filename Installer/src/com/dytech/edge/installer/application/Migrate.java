package com.dytech.edge.installer.application;

import java.io.InputStream;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Installer;
import com.dytech.installer.InstallerException;

public class Migrate
{
	public Migrate()
	{
	}

	public void start() throws InstallerException
	{
		InputStream script = getClass().getResourceAsStream("/script/migrate-script.xml");
		InputStream commands = getClass().getResourceAsStream("/script/migrate-commands.xml");

		new Installer(new PropBagEx(script), new PropBagEx(commands));
	}

	public static void main(String[] args) throws Exception
	{
		Migrate migrate = new Migrate();
		migrate.start();
	}
}
