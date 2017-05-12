package com.tle.echoserver;

import org.apache.struts.action.ActionForm;

public class MainForm extends ActionForm
{
	private String method;
	private String echo;

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getEcho()
	{
		return echo;
	}

	public void setEcho(String echo)
	{
		this.echo = echo;
	}

}
