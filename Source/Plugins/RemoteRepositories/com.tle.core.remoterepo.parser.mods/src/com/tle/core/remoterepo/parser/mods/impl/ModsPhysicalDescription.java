package com.tle.core.remoterepo.parser.mods.impl;

import org.w3c.dom.Node;

import com.tle.common.util.XmlDocument;

/**
 * @author aholland
 */
public class ModsPhysicalDescription extends ModsPart
{
	public ModsPhysicalDescription(XmlDocument xml, Node context)
	{
		super(xml, xml.node("physicalDescription", context));
	}

	public String getExtent()
	{
		return xml.nodeValue("extent", context);
	}
}
