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

package com.tle.core.harvester.old.dsoap;

/**
 * @author gfrancis
 */
public class SoapCallException extends Exception
{
	private int error;
	private String html;

	/**
	 * Creates a new instance of <code>SoapCallException</code> without detail
	 * message.
	 */
	public SoapCallException()
	{
		super();
	}

	/**
	 * Constructs an instance of <code>SoapCallException</code> with the
	 * specified detail message.
	 * 
	 * @param msg the detail message.
	 */
	public SoapCallException(String msg)
	{
		super(msg);
	}

	public SoapCallException(String msg, int code)
	{
		super(msg);
		error = code;
	}

	public SoapCallException(String msg, int code, String html)
	{
		super(msg);
		error = code;
		this.html = html;
	}

	public SoapCallException(Exception ex)
	{
		super(ex);
	}

	public SoapCallException(Exception ex, int code)
	{
		super(ex);
		error = code;
	}

	public int getErrorCode()
	{
		return error;
	}

	public String getHTML()
	{
		return html;
	}
}