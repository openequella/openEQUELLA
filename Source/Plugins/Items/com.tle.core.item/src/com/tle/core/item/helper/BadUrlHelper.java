/*
 * Created on 7/06/2006
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