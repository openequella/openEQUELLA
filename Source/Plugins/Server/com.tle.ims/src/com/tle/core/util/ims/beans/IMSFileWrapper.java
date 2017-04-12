package com.tle.core.util.ims.beans;

import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.NodeMapping;
import com.tle.common.Utils;

public class IMSFileWrapper extends IMSChild
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	protected String href = "";
	protected String originalName;

	public String getFullHref()
	{
		return (getFullBase() + getHref()).replace('\\', '/');
	}

	public String getHref()
	{
		return href;
	}

	public void setHref(String href)
	{
		this.href = href;
	}

	@Override
	public String toString()
	{
		return href;
	}

	/**
	 * Ads XML representation of the file to the StringBuilder.
	 * 
	 * @param sbuf
	 * @dytech.jira see Jira Defect TLE-924 :
	 *              http://apps.dytech.com.au/jira/browse/TLE-924
	 */
	public void addToXMLString(StringBuilder sbuf)
	{
		sbuf.append("<file>" + Utils.ent(getFullHref()) + "</file>\n");
	}

	/**
	 * Compares the object with this file. If the object is an IMSFileWrapper
	 * and it has the same entity encoded href, then true is returned, false
	 * otherwise.
	 * 
	 * @return true if and only if obh is an IMSFileWrapper with the same entity
	 *         encoded href.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if( !(obj instanceof IMSFileWrapper) )
		{
			return false;
		}
		else
		{
			IMSFileWrapper file = (IMSFileWrapper) obj;
			return Utils.ent(file.href).equals(Utils.ent(href));
		}
	}

	@Override
	public int hashCode()
	{
		return href.hashCode();
	}

	public String getOriginalName()
	{
		return originalName;
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings(super.getMappings());
			mappings.addNodeMapping(new NodeMapping("href", "@href"));
			mappings.addNodeMapping(new NodeMapping("originalName", "metadata/originalname"));
		}
		return mappings;
	}
}