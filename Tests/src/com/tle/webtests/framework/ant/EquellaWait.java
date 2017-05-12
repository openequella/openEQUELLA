package com.tle.webtests.framework.ant;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class EquellaWait extends Task
{
	private int maxwait;
	private URL url;

	@SuppressWarnings("nls")
	@Override
	public void execute() throws BuildException
	{
		long start = System.currentTimeMillis();
		long end = start + maxwait * 1000;
		int wrongStatus = 0;
		int noConnection = 0;
		int waitedSeconds = 10;
		while( end > System.currentTimeMillis() )
		{
			try
			{
				HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();
				if( httpConnect.getResponseCode() == 200 )
				{
					return;
				}
				else
				{
					wrongStatus++;
				}
			}
			catch( IOException e )
			{
				noConnection++;
			}
			if( waitedSeconds * 1000 + start < System.currentTimeMillis() )
			{
				log("Waited " + waitedSeconds + " seconds and had " + wrongStatus + " wrong response codes and "
					+ noConnection + " bad connections");
				waitedSeconds += 10;
			}
			try
			{
				Thread.sleep(1000);
			}
			catch( InterruptedException e )
			{
				throw new BuildException(e);
			}
		}
		throw new BuildException("Timeout exceeded waiting for equella");
	}

	public URL getUrl()
	{
		return url;
	}

	public void setUrl(URL url)
	{
		this.url = url;
	}

	public int getMaxwait()
	{
		return maxwait;
	}

	public void setMaxwait(int maxwait)
	{
		this.maxwait = maxwait;
	}
}
