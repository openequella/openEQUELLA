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

package com.tle.core.util.ims.beans;

import com.tle.core.xstream.XMLData;
import com.tle.core.xstream.XMLDataMappings;
import com.tle.core.xstream.mapping.NodeMapping;

@SuppressWarnings("nls")
public class IMSWrapper implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private String title;
	private String base;
	private String fullBase;

	public String getBase()
	{
		return base != null ? base : "";
	}

	public String getTitle()
	{
		return title != null ? title : "";
	}

	@Override
	public String toString()
	{
		return title;
	}

	protected String getFullBase()
	{
		if( fullBase == null )
		{
			fullBase = getBase();
			// base is written to by getMappings() below via reflection
			if( fullBase.length() > 0 && !base.endsWith("/") ) // NOSONAR
			{
				fullBase += '/';
			}
		}
		return fullBase;
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new NodeMapping("title", "title"));
			mappings.addNodeMapping(new NodeMapping("base", "@xml:base"));
		}
		return mappings;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
}