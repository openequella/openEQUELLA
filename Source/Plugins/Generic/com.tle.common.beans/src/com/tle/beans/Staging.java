/*
 * Created on Oct 26, 2005
 */
package com.tle.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
public class Staging
{
	@Id
	@Column(length = 40)
	private String stagingID;
	@Column(length = 40)
	private String userSession;

	public Staging()
	{
		super();
	}

	public String getStagingID()
	{
		return stagingID;
	}

	public void setStagingID(String stagingID)
	{
		this.stagingID = stagingID;
	}

	public String getUserSession()
	{
		return userSession;
	}

	public void setUserSession(String userSession)
	{
		this.userSession = userSession;
	}
}
