package com.tle.web.echo.data;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class EchoPresenter implements Serializable
{
	private String firstname;
	private String lastname;
	private String email;
	private String sectionroles;
	private Map<String, Object> extras = Maps.newHashMap();

	public EchoPresenter()
	{

	}

	public EchoPresenter(String firstname, String lastname, String email, String sectionroles)
	{
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.sectionroles = sectionroles;
	}

	public String getFirstname()
	{
		return firstname;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}

	public String getLastname()
	{
		return lastname;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getSectionroles()
	{
		return sectionroles;
	}

	public void setSectionroles(String sectionroles)
	{
		this.sectionroles = sectionroles;
	}

	public Map<String, Object> getExtras()
	{
		return extras;
	}

	public void setExtras(Map<String, Object> extras)
	{
		this.extras = extras;
	}
}
