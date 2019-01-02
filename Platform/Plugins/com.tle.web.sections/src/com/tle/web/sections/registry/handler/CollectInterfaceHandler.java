/*
 * Copyright 2019 Apereo
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
