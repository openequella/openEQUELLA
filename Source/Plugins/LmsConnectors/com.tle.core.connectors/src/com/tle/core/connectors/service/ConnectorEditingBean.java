package com.tle.core.connectors.service;

import com.tle.core.services.entity.EntityEditingBean;

/**
 * @author Aaron
 */
public class ConnectorEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String lmsType;
	private String serverUrl;
	private boolean useLoggedInUsername;
	private String usernameScript;
	private String contentExportableExpression;
	private String contentViewableExpression;
	private Object extraData;

	public String getLmsType()
	{
		return lmsType;
	}

	public void setLmsType(String lmsType)
	{
		this.lmsType = lmsType;
	}

	public String getServerUrl()
	{
		return serverUrl;
	}

	public void setServerUrl(String serverUrl)
	{
		this.serverUrl = serverUrl;
	}

	public boolean isUseLoggedInUsername()
	{
		return useLoggedInUsername;
	}

	public void setUseLoggedInUsername(boolean useLoggedInUsername)
	{
		this.useLoggedInUsername = useLoggedInUsername;
	}

	public String getUsernameScript()
	{
		return usernameScript;
	}

	public void setUsernameScript(String usernameScript)
	{
		this.usernameScript = usernameScript;
	}

	public Object getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Object extraData)
	{
		this.extraData = extraData;
	}

	public String getContentExportableExpression()
	{
		return contentExportableExpression;
	}

	public void setContentExportableExpression(String contentExportableExpression)
	{
		this.contentExportableExpression = contentExportableExpression;
	}

	public String getContentViewableExpression()
	{
		return contentViewableExpression;
	}

	public void setContentViewableExpression(String contentViewableExpression)
	{
		this.contentViewableExpression = contentViewableExpression;
	}
}
