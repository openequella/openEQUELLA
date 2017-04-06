package com.tle.core.customlinks.service;

import com.tle.core.services.entity.EntityEditingBean;

public class CustomLinkEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String url;
	private int order;
	private String targetExpression;
	private String fileName;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public String getTargetExpression()
	{
		return targetExpression;
	}

	public void setTargetExpression(String targetExpression)
	{
		this.targetExpression = targetExpression;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
}