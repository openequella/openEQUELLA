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

import java.util.ArrayList;
import java.util.List;

import com.tle.common.Utils;
import com.tle.core.xstream.XMLDataMappings;
import com.tle.core.xstream.mapping.DataMapping;
import com.tle.core.xstream.mapping.ListMapping;
import com.tle.core.xstream.mapping.NodeMapping;

public class IMSResource extends IMSFileWrapper
{
	private static final long serialVersionUID = 1L;

	private String identifier;
	private String type;
	private List<IMSFileWrapper> files = new ArrayList<IMSFileWrapper>();
	private IMSMetadata metadata;
	private static XMLDataMappings mappings;

	@Override
	public void addToXMLString(StringBuilder sbuf)
	{
		// List principal file first:
		sbuf.append("<file principal=\"true\">"); //$NON-NLS-1$
		sbuf.append(Utils.ent(getFullHref()));
		sbuf.append("</file>\n"); //$NON-NLS-1$

		// All other files:
		// see Jira Defect TLE-924 :
		// http://apps.dytech.com.au/jira/browse/TLE-924
		for( IMSFileWrapper file : files )
		{
			if( !href.equals(file.href) ) // ignore principal files here
			{
				file.addToXMLString(sbuf);
			}
		}
	}

	public IMSMetadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(IMSMetadata metadata)
	{
		this.metadata = metadata;
	}

	public List<IMSFileWrapper> getFiles()
	{
		return files;
	}

	public void setFiles(List<IMSFileWrapper> files)
	{
		this.files = files;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public String getHref()
	{
		if( href.length() == 0 && !files.isEmpty() )
		{
			return files.get(0).getHref();
		}
		return href;
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings(super.getMappings());
			// Presumably the intent is to return the implementation class, so
			// we ignore Sonar's "loose coupling" warning
			mappings.addNodeMapping(new ListMapping("files", "file", ArrayList.class, IMSFileWrapper.class)); // NOSONAR
			mappings.addNodeMapping(new NodeMapping("identifier", "@identifier"));
			mappings.addNodeMapping(new NodeMapping("type", "@type"));
			mappings.addNodeMapping(new DataMapping("metadata", "metadata", IMSMetadata.class));
		}
		return mappings;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}

}