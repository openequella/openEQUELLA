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
