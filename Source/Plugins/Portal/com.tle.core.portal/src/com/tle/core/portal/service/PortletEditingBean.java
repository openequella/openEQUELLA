package com.tle.core.portal.service;

import com.tle.core.services.entity.EntityEditingBean;

public class PortletEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String type;
	private boolean closeable;
	private boolean minimisable;
	private boolean institutional;
	private String config;
	private Object extraData;
	private String targetExpression;
	private boolean admin;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean isCloseable()
	{
		return closeable;
	}

	public void setCloseable(boolean closeable)
	{
		this.closeable = closeable;
	}

	public boolean isMinimisable()
	{
		return minimisable;
	}

	public void setMinimisable(boolean minimisable)
	{
		this.minimisable = minimisable;
	}

	public boolean isInstitutional()
	{
		return institutional;
	}

	public void setInstitutional(boolean institutional)
	{
		this.institutional = institutional;
	}

	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	public Object getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Object extraData)
	{
		this.extraData = extraData;
	}

	public String getTargetExpression()
	{
		return targetExpression;
	}

	public void setTargetExpression(String targetExpression)
	{
		this.targetExpression = targetExpression;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}
}