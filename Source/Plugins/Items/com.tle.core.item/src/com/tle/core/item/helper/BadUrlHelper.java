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

package com.tle.core.item.helper;

import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.ReferencedURL;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class BadUrlHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx itemxml, Item bean)
	{
		PropBagEx badXml = itemxml.aquireSubtree("badurls");
		badXml.deleteAll(Constants.XML_WILD);
		for( ReferencedURL badurl : bean.getReferencedUrls() )
		{
			if( !badurl.isSuccess() )
			{
				PropBagEx xml = badXml.newSubtree("url");
				setNode(xml, "@message", badurl.getMessage());
				setNode(xml, "@status", badurl.getStatus());
				setNode(xml, "@tries", badurl.getTries());
				setNode(xml, "@url", badurl.getUrl());
			}
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		// nothing
	}
}