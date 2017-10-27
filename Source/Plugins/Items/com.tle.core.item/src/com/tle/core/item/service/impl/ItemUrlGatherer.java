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

package com.tle.core.item.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.PropBagWalker;
import com.dytech.edge.common.PropBagWalker.NodeValueVisitorCallback;
import com.dytech.edge.common.UrlExtractor;
import com.google.inject.Singleton;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class ItemUrlGatherer
{
	@SuppressWarnings("nls")
	public Collection<String> gatherURLs(Item item, PropBagEx metadata)
	{
		final Set<String> urls = new HashSet<String>();

		// TODO: The following is hardcoded, but it should really be
		// plug-in-in-ated. For example, Youtube and Kaltura URLs should
		// probably be here too.

		// URLs embedded in metadata
		final UrlExtractor extractor = new UrlExtractor(true);
		final PropBagWalker walker = new PropBagWalker(metadata);
		walker.walk(true, new NodeValueVisitorCallback()
		{
			@Override
			public void visitNodeValue(String value)
			{
				urls.addAll(extractor.grabUrls(value));
			}
		});

		// Link attachments
		List<LinkAttachment> links = new UnmodifiableAttachments(item).getList(AttachmentType.LINK);
		for( LinkAttachment a : links )
		{
			String aUrl = a.getUrl();
			boolean httpUrl = false;
			try
			{
				URI uri = new URI(aUrl);
				String scheme = uri.getScheme();
				if( "http".equals(scheme) || "https".equals(scheme) )
				{
					httpUrl = true;
				}
			}
			catch( URISyntaxException e )
			{
				// ignore
			}
			if( !Check.isEmpty(aUrl) && httpUrl )
			{
				urls.add(aUrl);
			}
		}
		return urls;
	}
}
