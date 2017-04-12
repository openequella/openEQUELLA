package com.tle.core.remoterepo.parser.mods.impl.loose;

import org.w3c.dom.Node;

import com.tle.common.util.XmlDocument;
import com.tle.core.remoterepo.parser.mods.impl.ModsPart;

/**
 * @author aholland
 */
public class LoosePhysicalDescription extends ModsPart
{
	public LoosePhysicalDescription(XmlDocument xml, Node context)
	{
		super(xml, xml.node("formAndPhysicalDescription", context));
	}

	public String getExtent()
	{
		return xml.nodeValue("extent", context);
	}
}
