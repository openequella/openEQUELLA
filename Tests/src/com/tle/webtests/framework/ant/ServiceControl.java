package com.tle.webtests.framework.ant;

import java.io.File;
import java.net.URLEncoder;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServiceControl extends Task
{
	private String url;
	private String password;
	private String command;
	private File zipfile;
	private HttpClient client;

	@SuppressWarnings("nls")
	@Override
	public void execute() throws BuildException
	{
		if( command == null )
		{
			throw new BuildException("You must specify a command");
		}
		client = new HttpClient();
		client.getState().setCredentials(new AuthScope(null, -1, null),
			new UsernamePasswordCredentials("admin", password));
		DefaultHttpMethodRetryHandler rh = new DefaultHttpMethodRetryHandler(5, true);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, rh);
		// client.getHostConfiguration().setProxy("localhost", 8888);
		if( command.equals("stop") )
		{
			stop();
		}
		else if( command.equals("start") )
		{
			start();
		}
		else if( command.equals("deploy") )
		{
			deploy();
		}
		else
		{
			throw new BuildException("Bad command specified: '" + command + "'");
		}
	}

	@SuppressWarnings("nls")
	private void deploy()
	{
		try
		{

			PostMethod uploadPost = new PostMethod(url + "upload/");
			String version = "tle-upgrade-4.0.r25000 (branches+Trophy).zip";
			FilePart filepart = new FilePart("file", version, zipfile);
			MultipartRequestEntity entity = new MultipartRequestEntity(new Part[]{filepart,
					new StringPart("action-upload", "Upload")}, uploadPost.getParams());

			uploadPost.setRequestEntity(entity);
			verifyAction(uploadPost);
			uploadPost.releaseConnection();
			GetMethod deployGet = new GetMethod(url + "deploy/deploy/"
				+ URLEncoder.encode(version, "UTF-8").replaceAll("\\+", "%20"));
			deployGet.setFollowRedirects(false);
			deployGet.setQueryString(new NameValuePair[]{new NameValuePair("KeepThis", "true")});
			verifyAction(deployGet);
			Header location = deployGet.getResponseHeader("location");
			String progressUrl = location.getValue();
			String progressUuid = progressUrl.substring(progressUrl.lastIndexOf('/') + 1);
			boolean finished = false;
			while( !finished )
			{
				GetMethod progGet = new GetMethod(url + "ajax/" + progressUuid);
				client.executeMethod(progGet);
				String response = progGet.getResponseBodyAsString();
				JsonParser parser = new JsonParser();
				JsonElement respJson = parser.parse(response);
				JsonArray array = respJson.getAsJsonArray();
				for( JsonElement mesElem : array )
				{
					JsonObject mesObj = mesElem.getAsJsonObject();
					finished |= "finish".equals(mesObj.getAsJsonPrimitive("type").getAsString());
					log(mesObj.getAsJsonPrimitive("message").getAsString());
				}
				progGet.releaseConnection();
				Thread.sleep(3000);
			}

		}
		catch( Exception e )
		{
			throw new BuildException(e);
		}
	}

	private void verifyAction(HttpMethod method)
	{
		method.setFollowRedirects(false);
		try
		{
			int resp = client.executeMethod(method);
			if( resp != 303 )
			{
				throw new BuildException("Expected a 303 redirect"); //$NON-NLS-1$
			}
		}
		catch( Exception e )
		{
			throw new BuildException(e);
		}
	}

	@SuppressWarnings("nls")
	private void start()
	{
		verifyAction(new GetMethod(url + "server/start"));
	}

	@SuppressWarnings("nls")
	private void stop()
	{
		verifyAction(new GetMethod(url + "server/stop"));
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public File getZipfile()
	{
		return zipfile;
	}

	public void setZipfile(File zipfile)
	{
		this.zipfile = zipfile;
	}

	public String getCommand()
	{
		return command;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public static void main(String[] args)
	{
		ServiceControl control = new ServiceControl();
		control.setUrl("http://tle3:3000/");
		control.setPassword("admin");
		control.setZipfile(new File("staging/tle-upgrade.zip"));
		control.setCommand("deploy");
		control.init();
		control.execute();
	}
}
