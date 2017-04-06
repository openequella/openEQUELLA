package com.tle.web.contentrestrictions.dialog;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class SelectedQuota implements Serializable
{
	private int quotaIndex;
	private long quota;
	private String expression;
	private final Map<String, Object> validationErrors = Maps.newHashMap();

	public SelectedQuota()
	{
	}

	public SelectedQuota(int quotaIndex, long quota, String expression)
	{
		this.quotaIndex = quotaIndex;
		this.quota = quota;
		this.expression = expression;
	}

	public int getQuotaIndex()
	{
		return quotaIndex;
	}

	public void setQuotaIndex(int quotaIndex)
	{
		this.quotaIndex = quotaIndex;
	}

	public long getQuota()
	{
		return quota;
	}

	public void setQuota(long quota)
	{
		this.quota = quota;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public Map<String, Object> getValidationErrors()
	{
		return validationErrors;
	}
}
