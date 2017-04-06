package com.tle.beans.item.attachments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ItemNavigationTree
{
	private final List<ItemNavigationNode> rootNodes;
	private final Map<String, ItemNavigationNode> navigationMap;
	private final Map<ItemNavigationNode, List<ItemNavigationNode>> childMap;
	private final List<ItemNavigationNode> allNodes;

	public ItemNavigationTree(List<ItemNavigationNode> allNavigation)
	{
		this.allNodes = allNavigation;
		navigationMap = new HashMap<String, ItemNavigationNode>();
		rootNodes = new ArrayList<ItemNavigationNode>();
		childMap = new IdentityHashMap<ItemNavigationNode, List<ItemNavigationNode>>(allNodes.size());
		List<ItemNavigationNode> treeNodes = allNavigation;
		for( ItemNavigationNode node : treeNodes )
		{
			addNodePrivate(node, false);
		}
	}

	private void addNodePrivate(ItemNavigationNode node, boolean ignoreIndex)
	{
		List<ItemNavigationNode> parentList = rootNodes;
		ItemNavigationNode parent = node.getParent();
		if( parent != null )
		{
			parentList = childMap.get(parent);
			if( parentList == null )
			{
				parentList = new ArrayList<ItemNavigationNode>();
				childMap.put(parent, parentList);
			}
		}
		navigationMap.put(node.getUuid(), node);
		if( ignoreIndex )
		{
			int index = parentList.size();
			node.setIndex(index);
			parentList.add(node);
		}
		else
		{
			int ind = node.getIndex();
			// ouch... dangerous...
			while( parentList.size() <= ind )
			{
				parentList.add(null);
			}
			parentList.set(ind, node);
		}
	}

	/**
	 * @param node
	 * @param parent
	 * @param ignoreIndex if you EVER specify false, you must call
	 *            fixNodeIndices before attempting to save
	 */
	public void addNodeToParent(ItemNavigationNode node, String parent, boolean ignoreIndex)
	{
		if( parent != null )
		{
			ItemNavigationNode parentNode = navigationMap.get(parent);
			node.setParent(parentNode);
		}
		allNodes.add(node);
		addNodePrivate(node, ignoreIndex);
	}

	/**
	 * Ensures sequential index numbers and NO null entries
	 */
	public void fixNodeIndices()
	{
		fixNodeIndices(rootNodes);
		for( List<ItemNavigationNode> nodeList : childMap.values() )
		{
			fixNodeIndices(nodeList);
		}
	}

	private void fixNodeIndices(List<ItemNavigationNode> nodeList)
	{
		int index = 0;
		Iterator<ItemNavigationNode> it = nodeList.iterator();
		while( it.hasNext() )
		{
			ItemNavigationNode node = it.next();
			if( node == null )
			{
				it.remove();
			}
			else
			{
				node.setIndex(index);
				index++;
			}
		}
	}

	public List<ItemNavigationNode> getRootNodes()
	{
		return rootNodes;
	}

	public Map<ItemNavigationNode, List<ItemNavigationNode>> getChildMap()
	{
		return childMap;
	}

	public Map<String, ItemNavigationNode> getNavigationMap()
	{
		return navigationMap;
	}
}
