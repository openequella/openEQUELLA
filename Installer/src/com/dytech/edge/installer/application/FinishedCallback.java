package com.dytech.edge.installer.application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.installer.DatabaseCommand;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;
import com.tle.common.hash.Hash;

@SuppressWarnings("nls")
public class FinishedCallback implements Callback
{
	private static final Pattern URL_PATT = WebHostCallback.URL_PATT;

	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();

		// Hash password to a different property
		String p = output.getNode("admin.password");
		System.out.println("password is: " + p);
		output.setNode("hashed.admin.password", Hash.hashPassword(p));
		output.deleteNode("admin.password");
		System.out.println("password is now: " + output.getNode("admin.password"));

		// These are the defaults in case the URL is incorrect
		String hostname = "localhost";
		String context = "/";
		int port = 80;

		String url = output.getNode("webserver/url").trim();
		Matcher m = URL_PATT.matcher(url);
		if( m.matches() )
		{
			System.out.println("url matches " + m.groupCount() + " groups");
			switch( m.groupCount() )
			{
				case 3:
					String c = m.group(3);
					if( c != null && c.length() > 0 )
					{
						context = c;
					}
					// Don't break!

				case 2:
					String portStr = m.group(2);
					if( portStr != null )
					{
						port = Integer.parseInt(portStr);
					}
					// Don't break!

				case 1:
					hostname = m.group(1);
			}
		}
		else
		{
			System.err.println(url + " does not match regex");
			System.exit(1);
		}

		System.out.println("Hostname is " + hostname);
		System.out.println("Port is " + port);
		System.out.println("Context is " + context);

		output.setNode("webserver/host", hostname);
		output.setNode("webserver/port", port);
		output.setNode("webserver/context", context);

		output.setNode("datastore/initialise", "true");
		output.setNode("tomcat/path", output.getNode("install.path") + "/tomcat");

		if( output.getNode("webserver/binding").equals("restrict") )
		{
			output.setNode("webserver/tomcat_addr", " address=\"" + output.getNode("webserver/host") + "\" ");
		}

		installer.finished();

		// This needs to happen AFTER finished() call to make sure host isn't
		// overridden
		String dbhost = output.getNode("datasource/host");
		if( dbhost.indexOf(':') >= 0 )
		{
			String[] bits = dbhost.split(":");
			output.setNode("datasource/host", bits[0]);
			output.setNode("datasource/port", bits[1]);
		}
		else
		{
			String dbtype = output.getNode("datasource/dbtype");
			output.setNode("datasource/port", DatabaseCommand.getDefaultPort(dbtype));
		}
	}
}
