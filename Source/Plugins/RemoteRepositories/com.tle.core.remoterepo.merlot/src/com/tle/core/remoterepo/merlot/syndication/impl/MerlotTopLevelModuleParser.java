/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.remoterepo.merlot.syndication.impl;

import java.util.Locale;

import org.jdom2.Element;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
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
	public Module parse(Element element, Locale locale)
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
