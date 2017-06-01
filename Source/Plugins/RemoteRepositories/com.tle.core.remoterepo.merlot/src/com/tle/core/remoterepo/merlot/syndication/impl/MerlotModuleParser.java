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
import com.tle.core.remoterepo.merlot.syndication.MerlotModule;

/**
 * @author aholland
 */
public class MerlotModuleParser implements ModuleParser
{
	@Override
	public String getNamespaceUri()
	{
		return MerlotModule.URI;
	}

	@Override
	@SuppressWarnings("nls")
	public Module parse(Element element, Locale locale)
	{
		MerlotModule merlot = new MerlotModuleImpl();
		boolean foundSomething = false;

		final String title = getNodeValue(element, "title");
		if( title != null )
		{
			foundSomething = true;
			merlot.setTitle(title);
		}
		final String url = getNodeValue(element, "URL");
		if( url != null )
		{
			foundSomething = true;
			merlot.setUrl(url);
		}

		if( !foundSomething )
		{
			return null;
		}
		return merlot;
	}

	private String getNodeValue(Element element, String nodeName)
	{
		Element e = element.getChild(nodeName, MerlotModule.NAMESPACE);
		if( e != null )
		{
			return e.getText();
		}
		return null;
	}
}
