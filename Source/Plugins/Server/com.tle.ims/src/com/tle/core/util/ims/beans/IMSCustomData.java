package com.tle.core.util.ims.beans;

import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.PropBagMapping;
import com.dytech.devlib.PropBagEx;

/**
 * @author Aaron
 */
public class IMSCustomData implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private PropBagEx xml;

	public PropBagEx getXml()
	{
		return xml;
	}

	public void setXml(PropBagEx xml)
	{
		this.xml = xml;
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new PropBagMapping("xml", "custom", true));
		}
		return mappings;
	}
}
