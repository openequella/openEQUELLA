/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nicholas Read
 */
public class DisplayTemplate implements Serializable
{
	private static final long serialVersionUID = 1;

	public enum DisplayType
	{
		DEFAULT, XSLT, DISPLAY_NODES;
	}

	// Done because of Java 1.5 <-> 1.4 serialisation.
	private transient DisplayType typeEnum;
	private String type;

	private String xsltFilename;
	private List<DisplayNode> displayNodes;

	public DisplayType getType()
	{
		if( typeEnum == null && type != null )
		{
			typeEnum = DisplayType.valueOf(type);
		}
		return typeEnum;
	}

	public void setType(DisplayType type)
	{
		typeEnum = type;
		this.type = typeEnum.name();
	}

	public String getXsltFilename()
	{
		return xsltFilename;
	}

	public void setXsltFilename(String xsltFilename)
	{
		this.xsltFilename = xsltFilename;
	}

	public List<DisplayNode> getDisplayNodes()
	{
		return displayNodes;
	}

	public void setDisplayNodes(List<DisplayNode> displayNodes)
	{
		this.displayNodes = displayNodes;
	}
}
