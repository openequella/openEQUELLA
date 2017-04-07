package com.tle.web.sections.registry.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tle.web.sections.RegistrationHandler;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public class CollectInterfaceHandler<T> implements RegistrationHandler
{
	private final String key;
	private final Class<T> clazz;
	private Comparator<? super T> comparator;

	public CollectInterfaceHandler(Class<T> clazz)
	{
		this.clazz = clazz;
		key = "$INT-" + clazz.getName(); //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		// later
	}

	public void setComparator(Comparator<? super T> comparator)
	{
		this.comparator = comparator;
	}

	@Override
	public void treeFinished(SectionTree tree)
	{
		List<T> list = new ArrayList<T>();
		registerNow(tree.getRootId(), tree, list);
		if( comparator != null )
		{
			Collections.sort(list, comparator);
		}
		tree.setAttribute(key, list);
	}

	@SuppressWarnings("unchecked")
	private void registerNow(String id, SectionTree tree, List<T> list)
	{
		SectionId section = tree.getSectionForId(id);
		if( clazz.isAssignableFrom(section.getClass()) )
		{
			list.add((T) section);
		}
		List<SectionId> children = tree.getAllChildIds(id);
		for( SectionId child : children )
		{
			registerNow(child.getSectionId(), tree, list);
		}
	}

	public List<T> getAllImplementors(SectionTree tree)
	{
		return tree.getAttribute(key);
	}

	public List<T> getAllImplementors(SectionInfo info)
	{
		return info.getTreeAttribute(key);
	}
}
