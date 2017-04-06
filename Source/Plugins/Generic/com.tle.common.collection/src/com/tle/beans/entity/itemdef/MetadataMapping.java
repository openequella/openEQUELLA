/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.tle.beans.entity.itemdef.mapping.HTMLMapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping;
import com.tle.beans.entity.itemdef.mapping.LiteralMapping;

public class MetadataMapping implements Serializable
{
	private static final long serialVersionUID = 1;

	private Collection<IMSMapping> imsMapping = new ArrayList<IMSMapping>();
	private Collection<HTMLMapping> htmlMapping = new ArrayList<HTMLMapping>();
	private Collection<LiteralMapping> literalMapping = new ArrayList<LiteralMapping>();

	public Collection<HTMLMapping> getHtmlMapping()
	{
		return htmlMapping;
	}

	public Collection<IMSMapping> getImsMapping()
	{
		return imsMapping;
	}

	public Collection<LiteralMapping> getLiteralMapping()
	{
		return literalMapping;
	}
}
