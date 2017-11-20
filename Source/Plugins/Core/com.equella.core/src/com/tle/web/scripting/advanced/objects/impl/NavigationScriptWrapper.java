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

package com.tle.web.scripting.advanced.objects.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.web.scripting.advanced.objects.NavigationScriptObject;
import com.tle.web.scripting.advanced.types.NavigationNodeScriptType;
import com.tle.web.scripting.advanced.types.NavigationTabScriptType;
import com.tle.web.scripting.impl.AbstractScriptWrapper;
import com.tle.web.scripting.impl.AttachmentsScriptWrapper.AttachmentScriptTypeImpl;
import com.tle.web.viewurl.attachments.ItemNavigationService;

/**
 * @author aholland
 */
public class NavigationScriptWrapper extends AbstractScriptWrapper implements NavigationScriptObject
{
	private static final long serialVersionUID = 1L;

	private final Item item;
	private final ItemNavigationService itemNavService;
	private final Map<ItemNavigationNode, NavigationNodeScriptType> itemNodeToScriptNode;
	private ItemNavigationTree tree;

	public NavigationScriptWrapper(ItemNavigationService itemNavService, Item item)
	{
		this.itemNavService = itemNavService;
		this.item = item;
		this.itemNodeToScriptNode = new IdentityHashMap<ItemNavigationNode, NavigationNodeScriptType>();

		rebuildTree();
	}

	@Override
	public NavigationNodeScriptType addNode(String description, NavigationNodeScriptType parentNode)
	{
		// create real node
		ItemNavigationNode node = new ItemNavigationNode(item);
		node.setName(description);

		// update real tree
		String realParentUuid = null;
		if( parentNode != null )
		{
			realParentUuid = resolveRealNode(parentNode).getUuid();
		}
		tree.addNodeToParent(node, realParentUuid, true);
		tree.fixNodeIndices();

		// create a script node and map it to the real tree
		NavigationNodeScriptTypeImpl scriptNode = new NavigationNodeScriptTypeImpl(node);
		itemNodeToScriptNode.put(node, scriptNode);
		return scriptNode;
	}

	@Override
	public void removeNode(NavigationNodeScriptType node)
	{
		if( node != null )
		{
			final ItemNavigationNode realNode = resolveRealNode(node);
			removeNodeInternal(realNode);
			rebuildTree();
		}
	}

	@Override
	public NavigationTabScriptType addTab(String description, AttachmentScriptType attachment,
		NavigationNodeScriptType node)
	{
		if( attachment == null )
		{
			throw new RuntimeException("com.tle.web.scripting.advanced.navigation.error.nullattach"); //$NON-NLS-1$
		}

		// create real tab
		ItemNavigationTab tab = new ItemNavigationTab();
		tab.setName(description);
		if( attachment instanceof AttachmentScriptTypeImpl )
		{
			tab.setAttachment(((AttachmentScriptTypeImpl) attachment).getWrapped());
		}

		// create script tab
		NavigationTabScriptTypeImpl scriptTab = new NavigationTabScriptTypeImpl(tab, attachment, node);

		// update script node (which updates real node)
		NavigationNodeScriptTypeImpl n = (NavigationNodeScriptTypeImpl) node;
		n.addTab(scriptTab);

		return scriptTab;
	}

	@Override
	public NavigationNodeScriptType getNodeByDescription(String description)
	{
		for( NavigationNodeScriptType node : itemNodeToScriptNode.values() )
		{
			if( node.getDescription().equals(description) )
			{
				return node;
			}
		}
		return null;
	}

	@Override
	public void autocreate()
	{
		itemNavService.populateTreeNavigationFromAttachments(item, item.getTreeNodes(), item.getAttachments(), null);
		rebuildTree();
	}

	@Override
	public boolean isAllowSplitOption()
	{
		return item.getNavigationSettings().isShowSplitOption();
	}

	@Override
	public void setAllowSplitOption(boolean allowSplit)
	{
		item.getNavigationSettings().setShowSplitOption(allowSplit);
	}

	private void rebuildTree()
	{
		List<ItemNavigationNode> treeNodes = item.getTreeNodes();
		tree = new ItemNavigationTree(treeNodes);
		itemNodeToScriptNode.clear();
		for( ItemNavigationNode node : treeNodes )
		{
			itemNodeToScriptNode.put(node, new NavigationNodeScriptTypeImpl(node));
		}
		tree.fixNodeIndices();
	}

	@Override
	public void removeAll()
	{
		item.getTreeNodes().clear();
		tree = new ItemNavigationTree(item.getTreeNodes());
		itemNodeToScriptNode.clear();
	}

	@Override
	public List<NavigationNodeScriptType> listChildNodes(NavigationNodeScriptType parentNode)
	{
		final List<NavigationNodeScriptType> children = new ArrayList<NavigationNodeScriptType>();
		final ItemNavigationNode realNode = resolveRealNode(parentNode);

		final List<ItemNavigationNode> realChildren = (realNode == null ? tree.getRootNodes() : tree.getChildMap().get(
			realNode));
		if( realChildren != null )
		{
			for( ItemNavigationNode realChild : realChildren )
			{
				children.add(getScriptNode(realChild));
			}
		}

		return children;
	}

	/** Must rebuild tree after calling */
	private int removeNodeInternal(ItemNavigationNode node)
	{
		int removed = 1;
		List<ItemNavigationNode> children = tree.getChildMap().get(node);
		if( children != null )
		{
			for( ItemNavigationNode childNode : children )
			{
				removed += removeNodeInternal(childNode);
			}
		}
		item.getTreeNodes().remove(node);
		return removed;
	}

	@Override
	public int removeChildNodes(NavigationNodeScriptType parentNode)
	{
		List<ItemNavigationNode> realChildren = null;
		if( parentNode == null )
		{
			realChildren = tree.getRootNodes();
		}
		else
		{
			final ItemNavigationNode realNode = resolveRealNode(parentNode);
			if( realNode != null )
			{
				realChildren = tree.getChildMap().get(realNode);
			}
		}
		if( realChildren != null )
		{
			int removed = 0;
			for( ItemNavigationNode realChild : realChildren )
			{
				removed += removeNodeInternal(realChild);
			}
			rebuildTree();
			return removed;
		}
		return 0;
	}

	private ItemNavigationNode resolveRealNode(NavigationNodeScriptType scriptNode)
	{
		if( scriptNode != null )
		{
			final NavigationNodeScriptTypeImpl n = (NavigationNodeScriptTypeImpl) scriptNode;
			// Ensure it's still in the tree
			ItemNavigationNode realNode = n.getWrapped();
			if( itemNodeToScriptNode.containsKey(realNode) )
			{
				return realNode;
			}
		}
		return null;
	}

	@Override
	public void scriptExit()
	{
		super.scriptExit();

		final Set<Attachment> itemAttachments = new HashSet<Attachment>(item.getAttachments());

		// validate the tabs with the actual attachments (prevents Hibernate
		// 'unsaved transient' blah errors)
		for( ItemNavigationNode node : itemNodeToScriptNode.keySet() )
		{
			for( Iterator<ItemNavigationTab> tabs = node.getTabs().iterator(); tabs.hasNext(); )
			{
				final ItemNavigationTab tab = tabs.next();
				final Attachment attach = tab.getAttachment();
				if( attach != null && !itemAttachments.contains(attach) )
				{
					// throw error? log warning? silently remove?
					// throw new RuntimeException(CurrentLocale.get(
					//	"com.tle.web.scripting.advanced.navigation.error.badtab", attach //$NON-NLS-1$
					// .getDescription()));
					tabs.remove();
				}
			}
		}

		// cleanup node indices, null nodes etc.
		tree.fixNodeIndices();
	}

	protected NavigationNodeScriptType getScriptNode(ItemNavigationNode realNode)
	{
		final NavigationNodeScriptType scriptNode = itemNodeToScriptNode.get(realNode);
		if( scriptNode == null )
		{
			throw new RuntimeException(CurrentLocale.get(
				"com.tle.web.scripting.advanced.navigation.error.noscriptnode", realNode //$NON-NLS-1$
					.getName()));
		}
		return scriptNode;
	}

	public class NavigationNodeScriptTypeImpl implements NavigationNodeScriptType
	{
		private static final long serialVersionUID = 1L;

		protected final ItemNavigationNode wrapped;
		protected final List<NavigationTabScriptType> tabs;

		protected NavigationNodeScriptTypeImpl(ItemNavigationNode wrapped)
		{
			this.wrapped = wrapped;
			this.tabs = new ArrayList<NavigationTabScriptType>();
			for( ItemNavigationTab tab : wrapped.ensureTabs() )
			{
				if( tab.getAttachment() != null )
				{
					tabs.add(new NavigationTabScriptTypeImpl(tab, new AttachmentScriptTypeImpl(tab.getAttachment(),
						null), this));
				}
				else
				{
					tabs.add(new NavigationTabScriptTypeImpl(tab, this));
				}
			}
		}

		@Override
		public String getDescription()
		{
			return wrapped.getName();
		}

		@Override
		public void setDescription(String description)
		{
			wrapped.setName(description);
		}

		@Override
		public List<NavigationTabScriptType> getTabs()
		{
			return Collections.unmodifiableList(tabs);
		}

		protected void addTab(NavigationTabScriptTypeImpl tab)
		{
			// update the wrapped
			wrapped.ensureTabs().add(tab.wrapped);
			tabs.add(tab);
		}

		/**
		 * Internal use only!
		 * 
		 * @return
		 */
		protected ItemNavigationNode getWrapped()
		{
			return wrapped;
		}
	}

	public class NavigationTabScriptTypeImpl implements NavigationTabScriptType
	{
		private static final long serialVersionUID = 1L;

		protected final ItemNavigationTab wrapped;
		protected final AttachmentScriptType attachment;
		protected final NavigationNodeScriptType parentNode;

		protected NavigationTabScriptTypeImpl(ItemNavigationTab wrapped, NavigationNodeScriptType parentNode)
		{
			this(wrapped, null, parentNode);
		}

		protected NavigationTabScriptTypeImpl(ItemNavigationTab wrapped, AttachmentScriptType attachment,
			NavigationNodeScriptType parentNode)
		{
			this.wrapped = wrapped;
			this.attachment = attachment;
			this.parentNode = parentNode;
			this.wrapped.setNode(((NavigationNodeScriptTypeImpl) parentNode).getWrapped());
		}

		@Override
		public String getDescription()
		{
			return wrapped.getName();
		}

		@Override
		public void setDescription(String description)
		{
			wrapped.setName(description);
		}

		@Override
		public AttachmentScriptType getAttachment()
		{
			return attachment;
		}

		@Override
		public NavigationNodeScriptType getNode()
		{
			return getScriptNode(wrapped.getNode());
		}

		@Override
		public String getViewerId()
		{
			return wrapped.getViewer();
		}

		@Override
		public void setViewerId(String viewerId)
		{
			wrapped.setViewer(viewerId);
		}
	}
}
