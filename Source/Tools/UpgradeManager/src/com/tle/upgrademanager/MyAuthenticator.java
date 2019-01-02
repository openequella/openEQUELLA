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