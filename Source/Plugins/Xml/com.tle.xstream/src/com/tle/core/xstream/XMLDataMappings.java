/*
 * Created on Jun 8, 2005
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
