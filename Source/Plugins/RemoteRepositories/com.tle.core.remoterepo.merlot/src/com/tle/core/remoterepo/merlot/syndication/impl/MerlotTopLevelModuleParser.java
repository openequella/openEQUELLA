package com.tle.core.remoterepo.merlot.syndication.impl;

import org.jdom.Element;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;
import com.tle.common.Utils;
import com.tle.core.remoterepo.merlot.syndication.MerlotTopLevelModule;

/**
 * @author aholland
 */
public class MerlotTopLevelModuleParser implements ModuleParser
{
	@Override
	public String getNamespaceUri()
	{
		return MerlotTopLevelModule.URI;
	}

	@Override
	@SuppressWarnings("nls")
	public Module parse(Element element)
	{
		MerlotTopLevelModule merlot = new MerlotTopLevelModuleImpl();
		boolean foundSomething = false;

		String totalCount = getNodeValue(element, "totalCount");
		if( totalCount != null )
		{
			foundSomething = true;
			merlot.setTotalCount(Utils.parseInt(totalCount, 0));
		}
		final String resultCount = getNodeValue(element, "resultCount");
		if( resultCount != null )
		{
			foundSomething = true;
			merlot.setResultCount(Utils.parseInt(resultCount, 0));
		}
		final String lastRecNumber = getNodeValue(element, "lastRecNumber");
		if( lastRecNumber != null )
		{
			foundSomething = true;
			merlot.setLastRecNumber(Utils.parseInt(lastRecNumber, 0));
		}
		/*
		 * Element queryElem = element.getChild("query",
		 * MerlotTopLevelModule.NAMESPACE); if( queryElem != null ) { String
		 * query = queryElem.getAttributeValue("searchTerms"); if( query != null
		 * ) { foundSomething = true; open.setQuery(query); } }
		 */

		if( !foundSomething )
		{
			return null;
		}
		return merlot;
	}

	private String getNodeValue(Element element, String nodeName)
	{
		Element e = element.getChild(nodeName, MerlotTopLevelModule.NAMESPACE);
		if( e != null )
		{
			return e.getText();
		}
		return null;
	}
}
