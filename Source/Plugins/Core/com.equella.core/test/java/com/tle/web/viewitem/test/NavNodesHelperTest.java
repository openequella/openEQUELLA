package com.tle.web.viewitem.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.google.common.collect.ImmutableList;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.core.item.navigation.NavigationNodeHelper;

@SuppressWarnings("nls")
public class NavNodesHelperTest
{
	@Test
	public void testNavNodeLoad()
	{
		NavigationNodeHelper nodeHelper = new NavigationNodeHelper();
		PropBagEx itemxml = new PropBagEx();
		Item item = new Item();

		FileAttachment file1 = new FileAttachment();
		FileAttachment file2 = new FileAttachment();
		String file1uuid = UUID.randomUUID().toString();
		String file2uuid = UUID.randomUUID().toString();
		String node1uuid = UUID.randomUUID().toString();
		String node2uuid = UUID.randomUUID().toString();
		String node3uuid = UUID.randomUUID().toString();
		file1.setUuid(file1uuid);
		file2.setUuid(file2uuid);

		List<ItemNavigationNode> nodes = item.getTreeNodes();

		ItemNavigationNode node1 = new ItemNavigationNode();
		node1.setName("Node 1");
		node1.setIcon("ims.gif");
		node1.setIdentifier("ident");
		node1.setUuid(node1uuid);
		ItemNavigationTab tab1 = new ItemNavigationTab();
		tab1.setName("Tab 1");
		tab1.setAttachment(file1);
		ItemNavigationTab tab2 = new ItemNavigationTab();
		tab2.setName("Tab 2");
		tab2.setAttachment(file2);
		node1.setTabs(ImmutableList.of(tab1, tab2));
		nodes.add(node1);

		ItemNavigationNode node2 = new ItemNavigationNode();
		node2.setName("Node 2");
		node2.setIcon("ims2.gif");
		node2.setIdentifier("ident2");
		node2.setUuid(node2uuid);
		tab1 = new ItemNavigationTab();
		tab1.setName("Tab 1");
		tab1.setAttachment(file1);
		tab2 = new ItemNavigationTab();
		tab2.setName("Tab 2");
		tab2.setAttachment(file2);
		node2.setParent(node1);
		node2.setTabs(ImmutableList.of(tab1, tab2));
		nodes.add(node2);

		ItemNavigationNode node3 = new ItemNavigationNode();
		node3.setName("Node 3");
		node3.setIcon("ims3.gif");
		node3.setIdentifier("ident3");
		node3.setUuid(node3uuid);
		tab1 = new ItemNavigationTab();
		tab1.setName("Tab 1");
		tab1.setAttachment(file1);
		tab2 = new ItemNavigationTab();
		tab2.setName("Tab 2");
		tab2.setAttachment(file2);
		node3.setParent(node1);
		node3.setIndex(1);
		node3.setTabs(ImmutableList.of(tab1, tab2));
		nodes.add(node3);

		nodeHelper.load(itemxml, item);

		String[][] tabs = new String[][]{{"Tab 1", file1uuid}, {"Tab 2", file2uuid}};
		PropBagEx rootNode = itemxml.getSubtree("navigationNodes/node");
		assertNode(rootNode, node1uuid, "Node 1", "ims.gif", "ident", tabs);
		assertNode(rootNode.getSubtree("node[0]"), node2uuid, "Node 2", "ims2.gif", "ident2", tabs);
		assertNode(rootNode.getSubtree("node[1]"), node3uuid, "Node 3", "ims3.gif", "ident3", tabs);
	}

	private void assertNode(PropBagEx nodexml, String uuid, String name, String icon, String ident, String[][] tabs)
	{
		assertEquals(uuid, nodexml.getNode("uuid"));
		assertEquals(name, nodexml.getNode("name"));
		assertEquals(icon, nodexml.getNode("icon"));
		assertEquals(ident, nodexml.getNode("identifier"));
		PropBagThoroughIterator iter = nodexml.iterateAll("tab");
		int i = 0;
		while( iter.hasNext() )
		{
			PropBagEx tab = iter.next();
			String[] tabExpect = tabs[i];
			assertEquals(tabExpect[0], tab.getNode("name"));
			assertEquals(tabExpect[1], tab.getNode("@attachment"));
			i++;
		}
	}

	private void assertNodeSaved(ItemNavigationNode node, String uuid, String name, String icon, String ident,
		String parentUuid, int index, String[][] tabs)
	{
		assertEquals(uuid, node.getUuid());
		assertEquals(name, node.getName());
		assertEquals(icon, node.getIcon());
		assertEquals(ident, node.getIdentifier());
		Iterator<ItemNavigationTab> iter = node.getTabs().iterator();
		int i = 0;
		while( iter.hasNext() )
		{
			ItemNavigationTab tab = iter.next();
			String[] tabExpect = tabs[i];
			assertEquals(tabExpect[0], tab.getName());
			assertEquals(tabExpect[1], tab.getAttachment().getUuid());
			i++;
		}
		if( parentUuid == null )
		{
			assertNull(node.getParent());
		}
		else
		{
			assertEquals(parentUuid, node.getParent().getUuid());
			assertEquals(index, node.getIndex());
		}
	}

	@Test
	public void testAttachmentsSave()
	{
		PropBagEx nodexml = new PropBagEx(new UnicodeReader(getClass().getResourceAsStream("nodes.xml"), "UTF-8"));
		Item item = new Item();
		FileAttachment file1 = new FileAttachment();
		file1.setUuid("2c3fa601-dcdc-44e7-8016-8034ee5cabf8");
		FileAttachment file2 = new FileAttachment();
		file2.setUuid("fae0f46d-f8a8-497e-8f47-07518c9da153");
		item.getAttachments().add(file1);
		item.getAttachments().add(file2);
		NavigationNodeHelper nodeHelper = new NavigationNodeHelper();
		nodeHelper.save(nodexml, item, new HashSet<String>());
		String[][] tabs = new String[][]{{"Tab 1", "2c3fa601-dcdc-44e7-8016-8034ee5cabf8"},
				{"Tab 2", "fae0f46d-f8a8-497e-8f47-07518c9da153"}};
		List<ItemNavigationNode> treeNodes = item.getTreeNodes();
		assertNodeSaved(treeNodes.get(0), "2cbde4ac-9d2e-4e9f-acf6-4035b91eaa8e", "Node 1", "ims.gif", "ident", null,
			0, tabs);
		assertNodeSaved(treeNodes.get(1), "579fa85f-21fc-4d12-bcdf-6a6025a1b29c", "Node 2", "ims2.gif", "ident2",
			"2cbde4ac-9d2e-4e9f-acf6-4035b91eaa8e", 0, tabs);
		assertNodeSaved(treeNodes.get(2), "d3572b6f-c38b-41b6-9dbe-a977375746a1", "Node 3", "ims3.gif", "ident3",
			"2cbde4ac-9d2e-4e9f-acf6-4035b91eaa8e", 1, tabs);

	}

}
