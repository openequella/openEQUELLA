/*
 * Created on Jun 14, 2005
 */
package com.tle.core.util.ims.beans;

import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.NodeMapping;

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
