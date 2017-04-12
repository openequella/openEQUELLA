package com.tle.common.item;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CollaboratorsHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx itemxml, Item bean)
	{
		for( String collab : bean.getCollaborators() )
		{
			itemxml.createNode("collaborativeowners/collaborator", collab);
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		final PropBagEx collabs = xml.getSubtree("collaborativeowners");
		if( collabs != null )
		{
			item.setCollaborators(iterate(collabs, "collaborator", new HashSet<String>()));
		}
		handled.add("collaborativeowners");
	}
}