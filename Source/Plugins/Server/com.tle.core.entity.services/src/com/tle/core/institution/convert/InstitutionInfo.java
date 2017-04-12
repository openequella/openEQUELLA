package com.tle.core.institution.convert;

import java.util.Set;

import com.tle.beans.Institution;

public class InstitutionInfo
{
	private Institution institution;
	private String buildVersion;
	private String serverURL;
	private Set<String> flags;
	private Set<String> xmlMigrations;
	private Set<String> itemXmlMigrations;
	private Set<String> postReadMigrations;

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
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

	public Set<String> getFlags()
	{
		return flags;
	}

	public void setFlags(Set<String> flags)
	{
		this.flags = flags;
	}

	public Set<String> getXmlMigrations()
	{
		return xmlMigrations;
	}

	public void setXmlMigrations(Set<String> xmlmigrations)
	{
		this.xmlMigrations = xmlmigrations;
	}

	public Set<String> getItemXmlMigrations()
	{
		return itemXmlMigrations;
	}

	public void setItemXmlMigrations(Set<String> itemxmlmigrations)
	{
		this.itemXmlMigrations = itemxmlmigrations;
	}

	public Set<String> getPostReadMigrations()
	{
		return postReadMigrations;
	}

	public void setPostReadMigrations(Set<String> postreadmigrations)
	{
		this.postReadMigrations = postreadmigrations;
	}
}