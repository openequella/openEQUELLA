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

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class Header
{
	private String identifier;
	private String datestamp;
	private Collection specs;
	private String status;

	public Header()
	{
		//
	}

	public String getDatestamp()
	{
		return datestamp;
	}

	public void setDatestamp(String datestamp)
	{
		this.datestamp = datestamp;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public void addSpec(String spec)
	{
		Collection specss = getSpecs();
		specss.add(spec);
	}

	public Collection getSpecs()
	{
		if( specs == null )
		{
			specs = new ArrayList();
		}
		return specs;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
