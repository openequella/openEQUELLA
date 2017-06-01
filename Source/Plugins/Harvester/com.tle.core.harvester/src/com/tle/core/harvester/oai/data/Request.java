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

import java.util.HashMap;
import java.util.Map;

import com.tle.core.harvester.oai.error.BadArgumentException;

/**
 * 
 */
public class Request
{
	private Map attributes;
	private String node;

	public Request()
	{
		attributes = new HashMap();
	}

	public Map getAttributes()
	{
		return attributes;
	}

	public String getAttribute(String name)
	{
		String[] array = (String[]) attributes.get(name);
		String attribute = null;
		if( array != null && array.length > 0 )
		{
			attribute = array[0];
		}
		return attribute;

	}

	public void setAttributes(Map attributes)
	{
		this.attributes = attributes;
	}

	public String getNode()
	{
		return node;
	}

	public void setNode(String node)
	{
		this.node = node;
	}

	//

	private String getAttribute(String attribute, boolean required) throws BadArgumentException
	{
		String ident = getAttribute(attribute);
		if( ident == null && required )
		{
			throw new BadArgumentException(getVerb(), "argument '" + attribute + "' required but not supplied.");
		}
		return ident;
	}

	private String getVerb() throws BadArgumentException
	{
		return getAttribute("verb", false);
	}

	public String getIdentifier(boolean required) throws BadArgumentException
	{
		return getAttribute("identifier", required);
	}

	public String getFrom(boolean required) throws BadArgumentException
	{
		return getAttribute("from", required);
	}

	public String getUntil(boolean required) throws BadArgumentException
	{
		return getAttribute("until", required);
	}

	public String getSet(boolean required) throws BadArgumentException
	{
		return getAttribute("set", required);
	}

	public String getMetadataPrefix(boolean required) throws BadArgumentException
	{
		return getAttribute("metadataPrefix", required);
	}

	public String getResumptionToken() throws BadArgumentException
	{
		String token = getAttribute("resumptionToken", false);
		if( token != null && getAttributes().size() > 2 )
		{
			throw new BadArgumentException(getVerb(),
				"argument 'resumptionToken' is exclusive, no others may be specified with it.");
		}
		return token;
	}
}
