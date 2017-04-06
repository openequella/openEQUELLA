/*
 * Created on Apr 13, 2005
 */

package com.tle.core.harvester.oai.data;

/**
 * 
 */
public class ResumptionToken
{
	private int completeListSize;
	private int cursor;
	private String token;

	public int getCompleteListSize()
	{
		return completeListSize;
	}

	public void setCompleteListSize(int completeListSize)
	{
		this.completeListSize = completeListSize;
	}

	public int getCursor()
	{
		return cursor;
	}

	public void setCursor(int cursor)
	{
		this.cursor = cursor;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}
}
