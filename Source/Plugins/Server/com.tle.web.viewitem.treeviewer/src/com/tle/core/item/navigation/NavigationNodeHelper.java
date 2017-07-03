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

package com.tle.core.item.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.AbstractHelper;

@Bind
@Singleton
@SuppressWarnings("nls")
public class NavigationNodeHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx item, Item bean)
	{
		PropBagEx navNodes = item.aquireSubtree("navigationNodes");
		ItemNavigationTree tree = new ItemNavigationTree(bean.getTreeNodes());
		Map<ItemNavigationNode, List<ItemNavigationNode>> childMap = tree.getChildMap();
		List<ItemNavigationNode> rootNodes = tree.getRootNodes();
		writeXmlNodes(rootNodes, childMap, navNodes);
	}

	private void writeXmlNodes(List<ItemNavigationNode> nodeList,
		Map<ItemNavigationNode, List<ItemNavigationNode>> childMap, PropBagEx parentXml)
	{
		Collections.sort(nodeList, new Comparator<ItemNavigationNode>()
		{
			@Override
			public int compare(ItemNavigationNode o1, ItemNavigationNode o2)
			{
				return o1.getIndex() - o2.getIndex();
			}
		});
		for( ItemNavigationNode node : nodeList )
		{
			PropBagEx nodeXml = parentXml.newSubtree("node");
			nodeXml.setNode("uuid", node.getUuid());
			nodeXml.setNode("name", node.getName() == null ? node.getUuid() : node.getName());
			nodeXml.setIfNotNull("icon", node.getIcon());
			nodeXml.setIfNotNull("identifier", node.getIdentifier());
			List<ItemNavigationTab> tabs = node.getTabs();
			for( ItemNavigationTab tab : tabs )
			{
				PropBagEx tabXml = nodeXml.newSubtree("tab");
				Attachment attachment = tab.getAttachment();
				if( attachment != null )
				{
					String attachmentUuid = attachment.getUuid();
					tabXml.setNode("@attachment", attachmentUuid);
				}
				tabXml.setIfNotNull("@viewer", tab.getViewer());
				tabXml.setIfNotNull("name", tab.getName());
			}
			List<ItemNavigationNode> childList = childMap.get(node);
			if( childList != null )
			{
				writeXmlNodes(childList, childMap, nodeXml);
			}
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		PropBagEx navNodes = xml.getSubtree("navigationNodes");
		if( navNodes != null )
		{
			Map<String, Attachment> attachMap = UnmodifiableAttachments
				.convertToMapUuid(item.getAttachmentsUnmodifiable());
			List<ItemNavigationNode> allNodes = new ArrayList<ItemNavigationNode>();
			readXmlNodes(navNodes, null, allNodes, attachMap, item);
			item.setTreeNodes(allNodes);
		}
		handled.add("navigationNodes");
	}

	private void readXmlNodes(PropBagEx navNodes, ItemNavigationNode parentNode, List<ItemNavigationNode> allNodes,
		Map<String, Attachment> attachMap, Item item)
	{
		PropBagThoroughIterator iter = navNodes.iterateAll("node");
		int index = 0;
		while( iter.hasNext() )
		{
			PropBagEx nodeXml = iter.next();
			ItemNavigationNode node = new ItemNavigationNode();
			node.setName(nodeXml.getNode("name"));
			node.setIcon(nodeXml.getNode("icon", null));
			node.setUuid(nodeXml.getNode("uuid"));
			node.setIdentifier(nodeXml.getNode("identifier", null));
			node.setIndex(index);
			node.setParent(parentNode);
			allNodes.add(node);
			List<ItemNavigationTab> tabs = new ArrayList<ItemNavigationTab>();
			PropBagThoroughIterator tabiter = nodeXml.iterateAll("tab");
			while( tabiter.hasNext() )
			{
				PropBagEx tabXml = tabiter.next();
				ItemNavigationTab tab = new ItemNavigationTab();
				tab.setAttachment(attachMap.get(tabXml.getNode("@attachment")));
				tab.setName(tabXml.getNode("name", null));
				tab.setViewer(tabXml.getNode("@viewer", null));
				tab.setNode(node);
				tabs.add(tab);
			}
			node.setTabs(tabs);
			readXmlNodes(nodeXml, node, allNodes, attachMap, item);
			index++;
		}
	}
}
