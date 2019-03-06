package com.tle.webtests.test.admin.multidb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class DockerControl
{
	private JSch jsch = new JSch();
	private String hostname;
	private String username;
	private String password;
	private String version;

	public DockerControl()
	{
		try
		{
			String pemPath = DockerControl.class.getResource("COREOS.pem").toURI().getPath();
			System.out.println(pemPath);
			jsch.addIdentity(pemPath);
		}
		catch( JSchException | URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}

	public void reset()
	{
		try
		{
			Session s = setupSession();
			Channel c = s.openChannel("exec");

			((ChannelExec) c).setCommand("sudo systemctl restart " + version
				+ "@postgres.service; etcdctl get /services/postgres" + version);
			InputStream in = c.getInputStream();
			c.connect();

			String output = null;

			byte[] tmp = new byte[1024];
			while( true )
			{
				while( in.available() > 0 )
				{
					int i = in.read(tmp, 0, 1024);
					if( i < 0 )
						break;
					output = (new String(tmp, 0, i));
				}
				if( c.isClosed() )
				{
					break;
				}
				try
				{
					Thread.sleep(1000);
				}
				catch( Exception ee )
				{
					Throwables.propagate(ee);
				}
			}

			if( !Objects.equals(Strings.nullToEmpty(output).trim(), "running") )
			{
				throw new RuntimeException("Postgres did not restart successfully");
			}

			c.disconnect();
			s.disconnect();
		}
		catch( JSchException | IOException e )
		{
			Throwables.propagate(e);
		}
	}

	private Session setupSession() throws JSchException
	{
		Session s = jsch.getSession("core", hostname, 22);
		s.setConfig("StrictHostKeyChecking", "no");
		s.connect();
		return s;
	}

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}
}
