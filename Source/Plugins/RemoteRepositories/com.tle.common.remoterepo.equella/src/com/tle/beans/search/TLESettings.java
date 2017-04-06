/*
 * Created on 1/12/2005
 */
package com.tle.beans.search;

@SuppressWarnings("nls")
public class TLESettings extends XmlBasedSearchSettings
{
	private static final String SEARCH_TYPE = "LEdgeSearchEngine";

	private static final String USERNAME = "username";
	private static final String INSTITUTIONURL = "insturl";
	private static final String SHAREDSECRETID = "secretid";
	private static final String SHAREDSECRETVALUE = "secretvalue";
	private static final String USELOGGEDINUSER = "userloggedinuser";

	private String username = "";
	private String institutionUrl = "http://";
	private String sharedSecretId = "";
	private String sharedSecretValue = "";
	private boolean useLoggedInUser = true;

	@Override
	protected String getType()
	{
		return SEARCH_TYPE;
	}

	@Override
	protected void _load()
	{
		super._load();
		username = get(USERNAME, username);
		institutionUrl = get(INSTITUTIONURL, institutionUrl);
		sharedSecretId = get(SHAREDSECRETID, sharedSecretId);
		sharedSecretValue = get(SHAREDSECRETVALUE, sharedSecretValue);
		useLoggedInUser = get(USELOGGEDINUSER, useLoggedInUser);
	}

	@Override
	protected void _save()
	{
		super._save();
		put(USERNAME, username);
		put(INSTITUTIONURL, institutionUrl);
		put(SHAREDSECRETID, sharedSecretId);
		put(SHAREDSECRETVALUE, sharedSecretValue);
		put(USELOGGEDINUSER, useLoggedInUser);
	}

	public String getInstitutionUrl()
	{
		return institutionUrl;
	}

	public void setInstitutionUrl(String institutionUrl)
	{
		this.institutionUrl = institutionUrl;
	}

	public String getSharedSecretId()
	{
		return sharedSecretId;
	}

	public void setSharedSecretId(String sharedSecretId)
	{
		this.sharedSecretId = sharedSecretId;
	}

	public String getSharedSecretValue()
	{
		return sharedSecretValue;
	}

	public void setSharedSecretValue(String sharedSecretValue)
	{
		this.sharedSecretValue = sharedSecretValue;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public boolean isUseLoggedInUser()
	{
		return useLoggedInUser;
	}

	public void setUseLoggedInUser(boolean useLoggedInUser)
	{
		this.useLoggedInUser = useLoggedInUser;
	}
}
