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

/**
 * @author jmaginnis
 */
public class IMSActivity implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private String file;
	private String type;
	private String name;
	private String keywords;
	private String description;
	private String startdate;
	private String enddate;
	private boolean applydaterange;
	private boolean excludefromexport;
	private String notes;

	public String getFile()
	{
		return file;
	}

	public String getType()
	{
		return type;
	}

	public boolean isApplydaterange()
	{
		return applydaterange;
	}

	public String getDescription()
	{
		return description;
	}

	public String getEnddate()
	{
		return enddate;
	}

	public boolean isExcludefromexport()
	{
		return excludefromexport;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public String getName()
	{
		return name;
	}

	public String getNotes()
	{
		return notes;
	}

	public String getStartdate()
	{
		return startdate;
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new NodeMapping("file", "@file"));
			mappings.addNodeMapping(new NodeMapping("type", "@type"));
			mappings.addNodeMapping(new NodeMapping("name", "name"));
			mappings.addNodeMapping(new NodeMapping("keywords", "keywords"));
			mappings.addNodeMapping(new NodeMapping("description", "description"));
			mappings.addNodeMapping(new NodeMapping("startdate", "startdate"));
			mappings.addNodeMapping(new NodeMapping("enddate", "enddate"));
			mappings.addNodeMapping(new NodeMapping("applydaterange", "applydaterange"));
			mappings.addNodeMapping(new NodeMapping("excludefromexport", "excludefromexport"));
			mappings.addNodeMapping(new NodeMapping("notes", "notes"));
			// TODO curriculum
		}
		return mappings;
	}
}
