/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
