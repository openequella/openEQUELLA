package com.tle.upgrademanager.helpers;

public class AjaxMessage
{
	private final String type;
	private final String message;
	private String redirect;

	public AjaxMessage(String t, String m)
	{
		type = t;
		message = m;
	}

	public String getType()
	{
		return type;
	}

	public String getMessage()
	{
		return message;
	}

	public String getRedirect()
	{
		return redirect;
	}

	public void setRedirect(String redirect)
	{
		this.redirect = redirect;
	}
}