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

package com.tle.core.institution.convert;

import java.util.HashSet;
import java.util.Set;

import com.tle.beans.Institution;
import com.tle.core.institution.convert.Converter.ConverterId;

// Use InstitutionInfo instead
@Deprecated
public class InstitutionImport
{
	private Institution institution;
	private String buildVersion;
	// Still in old institution exports
	private Set<ConverterId> conversions;
	private Set<String> strConversions;
	private Set<String> flags;

	private String serverURL;

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public Set<String> getConversions()
	{
		if( strConversions == null && conversions != null ) // NOSONAR
		{
			strConversions = new HashSet<String>();
			for( ConverterId cid : conversions )
			{
				strConversions.add(cid.name());
			}
		}
		return strConversions;
	}

	public void setConversions(Set<String> conversions)
	{
		this.strConversions = conversions;
		this.conversions = null;
	}

	public String getBuildVersion()
	{
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion)
	{
		this.buildVersion = buildVersion;
	}

	public String getServerURL()
	{
		return serverURL;
	}

	public void setServerURL(String serverURL)
	{
		this.serverURL = serverURL;
	}

	public boolean hasFlag(String flag)
	{
		return flags.contains(flag);
	}

	public void setFlags(Set<String> flags)
	{
		this.flags = flags;
	}

	public Set<String> getFlags()
	{
		return flags;
	}
}
