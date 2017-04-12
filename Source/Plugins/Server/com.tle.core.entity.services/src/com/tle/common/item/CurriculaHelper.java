/*
 * Created on 7/06/2006
 */
package com.tle.common.item;

import java.util.Set;

import javax.inject.Singleton;

import com.dytech.common.xml.TLEXStream;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.Item;
import com.tle.beans.item.curricula.Curricula;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CurriculaHelper extends AbstractHelper
{
	private static TLEXStream xstream = new TLEXStream();

	@Override
	public void load(PropBagEx item, Item bean)
	{
		if( bean.getCurricula() != null )
		{
			item.append(Constants.XML_ROOT, xstream.toPropBag(bean.getCurricula(), "curricula"));
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		final PropBagEx curric = xml.getSubtree("curricula");
		if( curric != null )
		{
			if( curric.nodeExists("curriculum/@uuid") )
			{
				item.setCurricula((Curricula) xstream.fromXML(curric, Curricula.class));
			}
		}
		handled.add("curricula");
	}
}