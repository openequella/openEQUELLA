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

package com.tle.core.institution.convert.importhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.institution.TreeNodeInterface;
import com.tle.core.institution.convert.TreeNodeCreator;
import com.tle.core.institution.convert.XmlHelper;

public class SingleTreeNodeFileImportHandler<T extends TreeNodeInterface<T>> extends AbstractImportHandler<T>
{
	private final List<T> results = new ArrayList<T>();

	public SingleTreeNodeFileImportHandler(SubTemporaryFile folder, String path, XmlHelper xmlHelper,
		TreeNodeCreator<T> creator, XStream xstream)
	{
		super(xmlHelper, xstream);

		Multimap<Long, T> parent2Nodes = ArrayListMultimap.create();

		List<T> nodes = xmlHelper.readXmlFile(folder, path, getXStream());
		for( T node : nodes )
		{
			Long parentId = (node.getParent() != null ? node.getParent().getId() : null);
			if( parentId != null )
			{
				try
				{
					T newParent = creator.createNode();
					newParent.setId(parentId);
					node.setParent(newParent);
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
			}
			parent2Nodes.get(parentId).add(node);
		}

		addToResultsTopDown(parent2Nodes, null);
	}

	private void addToResultsTopDown(Multimap<Long, T> parent2Nodes, Long parentId)
	{
		if( !parent2Nodes.containsKey(parentId) )
		{
			return;
		}

		Collection<T> nodes = parent2Nodes.get(parentId);
		results.addAll(nodes);

		for( T node : nodes )
		{
			// Recurse on child nodes
			addToResultsTopDown(parent2Nodes, node.getId());
		}
	}

	@Override
	public int getNodeCount()
	{
		return results.size();
	}

	@Override
	public Iterator<T> iterateNodes()
	{
		return results.iterator();
	}
}