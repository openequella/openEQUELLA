package com.tle.core.remoterepo.parser.mods.impl;

import org.w3c.dom.Node;

import com.tle.common.util.XmlDocument;

/**
 * @author aholland
 */
public abstract class ModsPart
{
	protected final XmlDocument xml;
	protected final Node context;

	protected ModsPart(XmlDocument xml, Node context)
	{
		this.xml = xml;
		this.context = context;
	}
}
