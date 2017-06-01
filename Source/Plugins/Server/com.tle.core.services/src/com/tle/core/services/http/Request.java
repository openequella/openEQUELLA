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

package com.tle.core.services.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;

/**
 * To be used with HttpService
 * 
 * @author Aaron
 */
public class Request
{
	public enum Method
	{
		GET, POST, PUT, DELETE, HEAD, OPTIONS, OTHER;

		public static Method fromString(String method)
		{
			try
			{
				return Method.valueOf(method.toUpperCase());
			}
			catch( Exception e )
			{
				return Method.OTHER;
			}
		}
	}

	private final String url;
	private Method method = Method.GET;
	private final List<NameValue> params = Lists.newArrayList();
	private final List<NameValue> headers = Lists.newArrayList();
	// Currently doesn't allow streaming or binary bodies
	private String body;
	private String mimeType;
	private String charset;

	public Request(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public Method getMethod()
	{
		return method;
	}

	public Request setMethod(Method method)
	{
		this.method = method;
		return this;
	}

	public List<NameValue> getParams()
	{
		return params;
	}

	public List<NameValue> getHeaders()
	{
		return headers;
	}

	public Request addParameter(String name, String value)
	{
		params.add(new NameValue(name, value));
		return this;
	}

	public Request addParameter(String name, int value)
	{
		params.add(new NameValue(name, Integer.toString(value)));
		return this;
	}

	public Request addHeader(String name, String value)
	{
		headers.add(new NameValue(name, value));
		return this;
	}

	public Request setAccept(String accept)
	{
		headers.add(new NameValue("Accept", accept));
		return this;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public String getCharset()
	{
		return charset;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	/**
	 * 
	 * @param qs A string of the form x=1&y=2 etc.  It will be broken up and *added* to the params list.
	 */
	public void setQueryString(String qs)
	{
		final Map<String, String> parsedQueryString = URLUtils.parseQueryString(qs, true);
		for( Map.Entry<String, String> kv : parsedQueryString.entrySet() )
		{
			addParameter(kv.getKey(), kv.getValue());
		}
	}

	public void setHtmlForm(FormParameters params)
	{
		setHtmlForm(params.getParameters());
	}

	public void setHtmlForm(List<NameValue> params)
	{
		setMimeType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		setCharset(Constants.UTF8);
		setBody(URLEncodedUtils.format(Lists.transform(params, new ParamConverter()), Constants.UTF8));
	}

	private static final class ParamConverter implements Function<NameValue, NameValuePair>
	{
		@Override
		public NameValuePair apply(NameValue nv)
		{
			return new BasicNameValuePair(nv.getName(), nv.getValue());
		}
	}

	public static class FormParameters
	{
		private List<NameValue> parameters = new ArrayList<>();

		private List<NameValue> getParameters()
		{
			return parameters;
		}

		public FormParameters addParameter(String name, String value)
		{
			parameters.add(new NameValue(name, value));
			return this;
		}

		public FormParameters addParameter(String name, int value)
		{
			parameters.add(new NameValue(name, Integer.toString(value)));
			return this;
		}
	}
}
