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

package com.tle.core.item.edit.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.NavigationSettings;
import com.tle.common.Check;
import com.tle.core.item.edit.ItemEditorChangeTracker;
import com.tle.core.item.edit.NavigationEditor;
import com.tle.core.item.edit.NavigationNodeEditor;

@SuppressWarnings("nls")
public class NavigationEditorImpl implements NavigationEditor
{
	private final Map<String, Attachment> attachmentMap;
	private final Map<String, ItemNavigationNode> nodeMap = Maps.newHashMap();
	private final ListMultimap<String, String> childMap = ArrayListMultimap.create();
	private final Item item;
	private final ItemEditorChangeTracker changeTracker;

	public NavigationEditorImpl(ItemEditorChangeTracker changeTracker, Map<String, Attachment> attachmentMap, Item item)
	{
		this.changeTracker = changeTracker;
		this.item = item;
		List<ItemNavigationNode> treeNodes = item.getTreeNodes();
		for( ItemNavigationNode navNode : treeNodes )
		{
			String childUuid = navNode.getUuid();
			nodeMap.put(childUuid, navNode);
			ItemNavigationNode parentNode = navNode.getParent();
			List<String> parentList = childMap.get(getIdForParent(parentNode));
			int ind = navNode.getIndex();
			while( ind >= parentList.size() )
			{
				parentList.add(null);
			}
			parentList.set(ind, childUuid);
		}
		this.attachmentMap = attachmentMap;
	}

	@Override
	public void editManualNavigation(boolean manualNavigation)
	{
		NavigationSettings navSettings = item.getNavigationSettings();
		if( changeTracker.hasBeenEdited(navSettings.isManualNavigation(), manualNavigation) )
		{
			navSettings.setManualNavigation(manualNavigation);
		}
	}

	@Override
	public void editShowSplitOption(boolean showSplitOption)
	{
		NavigationSettings navSettings = item.getNavigationSettings();
		if( changeTracker.hasBeenEdited(navSettings.isShowSplitOption(), showSplitOption) )
		{
			navSettings.setShowSplitOption(showSplitOption);
		}
	}

	private String getIdForParent(ItemNavigationNode parent)
	{
		if( parent != null )
		{
			return parent.getUuid();
		}
		return "";
	}

	@Override
	public NavigationNodeEditor getNavigationNodeEditor(String uuid)
	{
		ItemNavigationNode navNode = null;
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
		}
		else
		{
			navNode = nodeMap.get(uuid);
			if( navNode == null )
			{
				ItemEditorImpl.checkValidUuid(uuid);
			}
		}
		if( navNode == null )
		{
			navNode = new ItemNavigationNode();
			navNode.setUuid(uuid);
			item.getTreeNodes().add(navNode);
			nodeMap.put(uuid, navNode);
		}
		return new NavigationNodeEditorImpl(navNode);
	}

	public class NavigationNodeEditorImpl implements NavigationNodeEditor
	{
		private ItemNavigationNode navNode;
		private int origTabSize;

		public NavigationNodeEditorImpl(ItemNavigationNode navNode)
		{
			this.navNode = navNode;
			origTabSize = navNode.ensureTabs().size();
		}

		@Override
		public String getUuid()
		{
			return navNode.getUuid();
		}

		@Override
		public void editName(String name)
		{
			if( changeTracker.hasBeenEdited(navNode.getName(), name) )
			{
				navNode.setName(name);
			}
		}

		@Override
		public void editIcon(String icon)
		{
			if( changeTracker.hasBeenEdited(navNode.getIcon(), icon) )
			{
				navNode.setIcon(icon);
			}
		}

		@Override
		public void editImsId(String imsId)
		{
			if( changeTracker.hasBeenEdited(navNode.getIdentifier(), imsId) )
			{
				navNode.setIdentifier(imsId);
			}
		}

		@Override
		public void editTab(int index, String name, String attachmentUuid, String viewer)
		{
			ItemNavigationTab tab;
			List<ItemNavigationTab> tabs = navNode.ensureTabs();
			boolean tabAdded = false;
			if( index == tabs.size() )
			{
				tab = new ItemNavigationTab();
				tabs.add(tab);
				tabAdded = true;
			}
			else
			{
				tab = tabs.get(index);
			}
			if( tabAdded || changeTracker.hasBeenEdited(tab.getName(), name) )
			{
				tab.setName(name);
			}
			Attachment attachment = attachmentMap.get(attachmentUuid);
			if( attachment == null )
			{
				throw new ItemEditingException(
					"Trying to associate navigation node tab with attachment which doesn't exist: node:"
						+ navNode.getUuid() + " attachment:" + attachmentUuid);
			}
			Attachment origAttachment = tab.getAttachment();
			if( tabAdded
				|| changeTracker
					.hasBeenEdited(origAttachment == null ? null : origAttachment.getUuid(), attachmentUuid) )
			{
				tab.setAttachment(attachment);
			}
			if( tabAdded || changeTracker.hasBeenEdited(tab.getViewer(), viewer) )
			{
				tab.setViewer(viewer);
			}
		}

		@Override
		public void editTabCount(int numTabs)
		{
			List<ItemNavigationTab> tabs = navNode.ensureTabs();
			int tabSize = tabs.size();
			if( tabSize < numTabs )
			{
				throw new ItemEditingException("Requested size of tab list too many: wanted " + numTabs + " but only "
					+ tabSize + " available");
			}
			if( origTabSize != numTabs )
			{
				changeTracker.editDetected();
			}
			while( tabs.size() > numTabs )
			{
				tabs.remove(tabs.size() - 1);
			}
		}

		@Override
		public void editChildrenList(List<String> childUuids)
		{
			if( changeTracker.hasBeenEdited(childMap.get(navNode.getUuid()), childUuids) )
			{
				childMap.replaceValues(navNode.getUuid(), childUuids);
			}
		}

	}

	@Override
	public void editRootNodes(List<String> topLevelUuids)
	{
		if( changeTracker.hasBeenEdited(childMap.get(""), topLevelUuids) )
		{
			childMap.replaceValues("", topLevelUuids);
		}
	}

	public void finishedEditing(Map<String, Attachment> linkedAttachmentMap)
	{
		// Remove detached nodes, fix indexes and parents
		List<ItemNavigationNode> toRetain = Lists.newArrayList();
		Set<String> alreadySeen = Sets.newHashSet();
		recurseNode(null, toRetain, alreadySeen, linkedAttachmentMap);
		item.getTreeNodes().retainAll(toRetain);
	}

	private void recurseNode(ItemNavigationNode parent, List<ItemNavigationNode> toRetain, Set<String> alreadySeen,
		Map<String, Attachment> linkedAttachmentMap)
	{
		String parentUuid = getIdForParent(parent);
		if( alreadySeen.contains(parentUuid) )
		{
			throw new ItemEditingException("Node referenced twice:" + parentUuid);
		}
		if( parent != null )
		{
			toRetain.add(parent);
		}
		alreadySeen.add(parentUuid);
		List<String> children = childMap.get(parentUuid);
		int i = 0;
		for( String childUuid : children )
		{
			ItemNavigationNode navNode = nodeMap.get(childUuid);
			if( navNode == null )
			{
				throw new ItemEditingException("Node with uuid '" + childUuid + "' doesn't exist");
			}
			navNode.setIndex(i);
			navNode.setParent(parent);
			// Relink/check attachments
			List<ItemNavigationTab> tabs = navNode.getTabs();
			if( !Check.isEmpty(tabs) )
			{
				for( ItemNavigationTab tab : tabs )
				{
					Attachment attachment = tab.getAttachment();
					if( attachment != null )
					{
						String attachUuid = attachment.getUuid();
						attachment = linkedAttachmentMap.get(attachUuid);
						if( attachment == null )
						{
							throw new ItemEditingException("Navigation node '" + navNode.getUuid()
								+ "' references non-existent attachment '" + attachUuid + "'");
						}
						tab.setAttachment(attachment);
					}
				}
			}
			recurseNode(navNode, toRetain, alreadySeen, linkedAttachmentMap);
			i++;
		}
	}

}