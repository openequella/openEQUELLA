package com.tle.core.connectors.blackboard.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Availability
{
	public static final String YES = "Yes";
	public static final String NO = "No";
	private String available; //Yes
	private Boolean allowGuests;
	//private Object adaptiveRelease;

	public String getAvailable()
	{
		return available;
	}

	public void setAvailable(String available)
	{
		this.available = available;
	}

	public Boolean getAllowGuests()
	{
		return allowGuests;
	}

	public void setAllowGuests(Boolean allowGuests)
	{
		this.allowGuests = allowGuests;
	}

	@XmlRootElement
	public static class Duration
	{
		private String type; //Continuous

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}
	}
}
