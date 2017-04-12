package com.tle.core.remoterepo.parser.mods.impl;

import org.w3c.dom.Node;

import com.tle.common.util.XmlDocument;

/**
 * @author aholland
 */
public class ModsTitleInfo extends ModsPart
{
	ModsTitleInfo(XmlDocument xml, Node context)
	{
		super(xml, xml.node("titleInfo", context));
	}

	String getTitle()
	{
		// TODO: could be multiple titles with different "type" attributes. If
		// only we were using PropBagEX like the rest of the code in EQUELLA,
		// then we could use the LangUtils method to capture a LanguageBundle
		// rather than rewriting it again.
		return xml.nodeValue("title", context);
	}

	String getSubTitle()
	{
		return xml.nodeValue("subTitle", context);
	}
}
