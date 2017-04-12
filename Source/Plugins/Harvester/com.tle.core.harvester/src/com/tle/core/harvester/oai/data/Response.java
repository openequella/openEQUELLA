/*
 * Created on Apr 13, 2005
 */

package com.tle.core.harvester.oai.data;

/**
 * 
 */
public class Response
{
	private String responseDate;
	private Request request;
	private OAIError error;
	private Object message;
	private String messageNodeName;

	public String getMessageNodeName()
	{
		return messageNodeName;
	}

	public void setMessageNodeName(String messageNodeName)
	{
		this.messageNodeName = messageNodeName;
	}

	public OAIError getError()
	{
		return error;
	}

	public void setError(OAIError error)
	{
		this.error = error;
	}

	public Object getMessage()
	{
		return message;
	}

	public void setMessage(Object message)
	{
		this.message = message;
	}

	public Request getRequest()
	{
		return request;
	}

	public void setRequest(Request request)
	{
		this.request = request;
	}

	public String getResponseDate()
	{
		return responseDate;
	}

	public void setResponseDate(String responseDate)
	{
		this.responseDate = responseDate;
	}
}
