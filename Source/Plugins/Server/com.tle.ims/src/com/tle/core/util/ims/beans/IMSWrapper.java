package com.tle.core.util.ims.beans;

import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.NodeMapping;

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