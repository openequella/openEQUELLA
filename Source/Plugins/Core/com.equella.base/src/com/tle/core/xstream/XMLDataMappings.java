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

package com.tle.core.xstream;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.tle.core.xstream.mapping.AbstractMapping;

/**
 * @author jmaginnis
 */
public class XMLDataMappings
{
	private SortedMap<String, SortedSet<AbstractMapping>> nodeMapping;
	private boolean ignoreNS;

	public XMLDataMappings(XMLDataMappings pareMappings)
	{
		this(pareMappings, true);
	}

	public XMLDataMappings()
	{
		this(null, true);
	}

	public XMLDataMappings(XMLDataMappings pareMappings, boolean ignoreNS)
	{
		nodeMapping = new TreeMap<String, SortedSet<AbstractMapping>>();
		if( pareMappings != null )
		{
			for( Entry<String, SortedSet<AbstractMapping>> entry : pareMappings.nodeMapping.entrySet() )
			{
				nodeMapping.put(entry.getKey(), new TreeSet<AbstractMapping>(entry.getValue()));
			}
		}

		this.ignoreNS = ignoreNS;
	}

	public void addNodeMapping(AbstractMapping node)
	{
		XMLPath xpath = node.getNodePath();
		String snode = xpath.getNode();
		SortedSet<AbstractMapping> col = nodeMapping.get(snode);
		if( col == null )
		{
			// Need to evaluate attributes first
			col = new TreeSet<AbstractMapping>();
			nodeMapping.put(snode, col);
		}
		col.add(node);

	}

	public SortedMap<String, SortedSet<AbstractMapping>> getMappings()
	{
		return nodeMapping;
	}

	public boolean isIgnoreNS()
	{
		return ignoreNS;
	}

	/**
	 * @param b
	 */
	public void setIgnoreNS(boolean ignoreNS)
	{
		this.ignoreNS = ignoreNS;
	}
}
