package com.tle.web.api.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class UserBean extends AbstractExtendableBean
{
	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private String emailAddress;
	@JsonProperty("_export")
	private UserExportBean exportDetails;

	public UserBean(String id)
	{
		this.id = id;
	}

	public UserBean()
	{
		// nothing
	}

	public String getId()
	{
		return id;
	}

	public String getUsername()
	{
		return username;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getEmailAddress()
	{
		return emailAddress;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}

	public UserExportBean getExportDetails()
	{
		return exportDetails;
	}

	public void setExportDetails(UserExportBean exportDetails)
	{
		this.exportDetails = exportDetails;
	}
}
