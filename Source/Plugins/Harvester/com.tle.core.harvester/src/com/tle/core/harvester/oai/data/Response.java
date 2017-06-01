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
