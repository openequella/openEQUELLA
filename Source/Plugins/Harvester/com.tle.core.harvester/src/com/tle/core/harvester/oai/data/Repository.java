/*
 * Created on Apr 12, 2005
 */

package com.tle.core.harvester.oai.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class Repository
{
	private String name;
	private String baseURL;
	private String protocolVersion;
	private String earliestDatestamp;
	private String deletedRecord;
	private String granularity;

	private Collection<String> adminEmails;

	public Collection<String> getAdminEmails()
	{
		if( adminEmails == null )
		{
			adminEmails = new ArrayList<String>();
		}
		return adminEmails;
	}

	public void addAdminEmail(String email)
	{
		getAdminEmails().add(email);
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public String getDeletedRecord()
	{
		return deletedRecord;
	}

	public void setDeletedRecord(String deletedRecord)
	{
		this.deletedRecord = deletedRecord;
	}

	public String getEarliestDatestamp()
	{
		return earliestDatestamp;
	}

	public void setEarliestDatestamp(String earliestDatestamp)
	{
		this.earliestDatestamp = earliestDatestamp;
	}

	public String getGranularity()
	{
		return granularity;
	}

	public void setGranularity(String granularity)
	{
		this.granularity = granularity;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getProtocolVersion()
	{
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion)
	{
		this.protocolVersion = protocolVersion;
	}
}
