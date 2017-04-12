package com.tle.upgrademanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.sun.net.httpserver.BasicAuthenticator;
import com.tle.common.hash.Hash;

@SuppressWarnings("nls")
public class MyAuthenticator extends BasicAuthenticator
{
	private final Properties userPassMap;

	public MyAuthenticator(ManagerConfig config)
	{
		super("EQUELLA Manager");

		userPassMap = new Properties();
		try( InputStream in = new BufferedInputStream(new FileInputStream(new File(config.getManagerDir(),
			"users.properties"))) )
		{
			userPassMap.load(in);
		}
		catch( Exception ex )
		{
			System.out.println("Could not find users.properties file");
			ex.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public boolean checkCredentials(String username, String password)
	{
		String storedPass = userPassMap.getProperty(username);
		if( storedPass == null )
		{
			return false;
		}

		if( Hash.isHashed(storedPass) )
		{
			return Hash.checkPasswordMatch(storedPass, password);
		}
		return storedPass.equals(password);
	}
}