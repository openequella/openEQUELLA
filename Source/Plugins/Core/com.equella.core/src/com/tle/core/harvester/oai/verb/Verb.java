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

package com.tle.core.harvester.oai.verb;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dytech.common.io.UnicodeReader;
import com.thoughtworks.xstream.XStream;
import com.tle.common.URLUtils;
import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.MetadataFormat;
import com.tle.core.harvester.oai.data.OAIError;
import com.tle.core.harvester.oai.data.Response;
import com.tle.core.harvester.oai.error.CannotDisseminateFormatException;
import com.tle.core.harvester.oai.error.IdDoesNotExistException;
import com.tle.core.harvester.oai.error.NoMetadataFormatsException;
import com.tle.core.harvester.oai.error.NoRecordsMatchException;
import com.tle.core.harvester.oai.error.NoSetHierarchyException;
import com.tle.core.harvester.oai.xstream.XStreamFactory;

@SuppressWarnings("nls")
public abstract class Verb
{
	public static final String IDENTIFIER = "identifier";
	public static final String RESUMPTION_TOKEN = "resumptionToken";
	public static final String METADATA_PREFIX = "metadataPrefix";

	public static final String DC_PREFIX = MetadataFormat.DUBLIN_CORE_FORMAT.getMetadataPrefix();

	private static final XStream xstream = XStreamFactory.getXStream();

	protected URL url;
	private Map<String, String> map;

	public Verb()
	{
		map = new HashMap<String, String>();
	}

	private String generateParamaters()
	{
		StringBuilder buf = new StringBuilder();
		buf.append("?verb=");
		buf.append(URLUtils.basicUrlEncode(getVerb()));

		for( Entry<String, String> entry : getParameters().entrySet() )
		{
			buf.append('&');
			buf.append(entry.getKey());
			buf.append('=');
			buf.append(URLUtils.basicUrlEncode(entry.getValue()));
		}

		return buf.toString();
	}

	protected void addParamater(String name, String value)
	{
		if( value != null )
		{
			map.put(name, value);
		}
	}

	public Map<String, String> getParameters()
	{
		return map;
	}

	public abstract String getVerb();

	public void setURL(URL url)
	{
		this.url = url;
	}

	protected List listFromXML(Response response)
	{
		return (List) response.getMessage();
	}

	public Response call()
	{
		try
		{
			HttpURLConnection con = (HttpURLConnection) URLUtils.newURL(url, url.getPath() + generateParamaters())
				.openConnection();
			con.setConnectTimeout(10000);
			con.setRequestProperty("User-Agent", "OAIHarvester/2.0");
			String enc = con.getContentEncoding();
			if( enc == null )
			{
				enc = "UTF-8";
			}
			return (Response) xstream.fromXML(new UnicodeReader(con.getInputStream(), enc));
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	protected void checkIdDoesNotExistError(Response response) throws IdDoesNotExistException
	{
		OAIError error = response.getError();
		if( error != null )
		{
			String code = error.getCode();
			if( code.equals("idDoesNotExist") )
			{
				throw new IdDoesNotExistException(error);
			}
		}
	}

	protected void checkNoMetadataFormats(Response response) throws NoMetadataFormatsException
	{
		OAIError error = response.getError();
		if( error != null )
		{
			String code = error.getCode();
			if( code.equals("noMetadataFormats") )
			{
				throw new NoMetadataFormatsException(error);
			}
		}
	}

	protected void checkCannotDisseminateFormat(Response response) throws CannotDisseminateFormatException
	{
		OAIError error = response.getError();
		if( error != null )
		{
			String code = error.getCode();
			if( code.equals("cannotDisseminateFormat") )
			{
				throw new CannotDisseminateFormatException(error);
			}
		}
	}

	protected void checkNoRecordsMatch(Response response) throws NoRecordsMatchException
	{
		OAIError error = response.getError();
		if( error != null )
		{
			String code = error.getCode();
			if( code.equals("noRecordsMatch") )
			{
				throw new NoRecordsMatchException(error);
			}
		}
	}

	protected void checkNoSetHierarchy(Response response) throws NoSetHierarchyException
	{
		OAIError error = response.getError();
		if( error != null )
		{
			String code = error.getCode();
			if( code.equals("noSetHierarchyException") )
			{
				throw new NoSetHierarchyException(error);
			}
		}
	}
}
