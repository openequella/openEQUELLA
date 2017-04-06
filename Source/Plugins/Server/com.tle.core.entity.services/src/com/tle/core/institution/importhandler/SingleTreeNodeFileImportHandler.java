package com.tle.core.institution.importhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.tree.TreeNodeInterface;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.institution.XmlHelper;

public class SingleTreeNodeFileImportHandler<T extends TreeNodeInterface<T>> extends AbstractImportHandler<T>
{
	private final List<T> results = new ArrayList<T>();

	public SingleTreeNodeFileImportHandler(SubTemporaryFile folder, String path, XmlHelper xmlHelper,
		NodeCreator<T> creator, XStream xstream)
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

	public interface NodeCreator<T>
	{
		T createNode();
	}
}