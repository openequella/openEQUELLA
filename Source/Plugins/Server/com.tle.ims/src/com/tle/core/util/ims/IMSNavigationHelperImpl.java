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

package com.tle.core.util.ims;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.util.ims.beans.IMSItem;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSOrganisation;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.core.util.ims.extension.IMSAttachmentExporter;

/**
 * @author Aaron
 */
@Bind(IMSNavigationHelper.class)
@Singleton
public class IMSNavigationHelperImpl implements IMSNavigationHelper
{
	private PluginTracker<IMSAttachmentExporter> attachmentImporters;
	public static final String HIDE_RESOURCE_KEY = "$HIDE_RESOURCE$"; //$NON-NLS-1$

	@Override
	public Collection<Attachment> createTree(IMSManifest manifest, Item item, FileHandle root, String packageFolder,
		boolean scorm, boolean expand)
	{
		List<ItemNavigationNode> all = new ArrayList<ItemNavigationNode>();
		item.setTreeNodes(all);

		TreeCreationInfo treeInfo = new TreeCreationInfo(item, packageFolder, root, scorm, expand);
		linearise(manifest, null, treeInfo);
		return treeInfo.getCreatedAttachments();
	}

	private void linearise(IMSManifest man, ItemNavigationNode parent, TreeCreationInfo treeInfo)
	{
		Map<String, IMSResource> resources = man.getResourceMap();

		for( IMSOrganisation org : man.getOrganisations() )
		{
			lineariseOrgs(org, parent, resources, treeInfo);
		}

		for( IMSManifest subMan : man.getSubManifests() )
		{
			linearise(subMan, parent, treeInfo);
		}
	}

	private void lineariseOrgs(IMSOrganisation org, ItemNavigationNode parent, Map<String, IMSResource> resources,
		TreeCreationInfo treeInfo)
	{
		List<IMSItem> items = org.getItems();

		ItemNavigationNode node = new ItemNavigationNode(treeInfo.getItem());
		node.setName(org.getTitle());
		node.setParent(parent);
		//		node.setIcon("icons/organization.gif"); //$NON-NLS-1$
		node.ensureTabs();

		boolean valid = false;
		for( IMSItem imsitem : items )
		{
			if( imsitem.isVisible() )
			{
				valid |= lineariseItems(imsitem, node, resources, treeInfo);
			}
		}

		if( valid )
		{
			addNode(node, treeInfo);
		}
	}

	private boolean lineariseItems(IMSItem imsitem, ItemNavigationNode parent, Map<String, IMSResource> resources,
		TreeCreationInfo treeInfo)
	{
		IMSResource res = resources.get(imsitem.getIdentifierRef());

		List<IMSItem> items = imsitem.getItems();
		ItemNavigationNode node = new ItemNavigationNode(treeInfo.getItem());
		node.setName(imsitem.getTitle());
		node.setParent(parent);
		node.setIdentifier(imsitem.getIdentifier());
		//		node.setIcon("icons/IMSitem.gif"); //$NON-NLS-1$
		node.ensureTabs();

		boolean haveurl = false;
		String url = null;
		if( res != null && res.getHref().length() > 0 )
		{
			url = res.getFullHref();
			haveurl = true;
		}
		if( url != null )
		{
			try
			{
				url = URLDecoder.decode(url, Constants.UTF8);
			}
			catch( UnsupportedEncodingException un )
			{
				throw new RuntimeException(un);
			}

			Attachment fattach = null;
			for( IMSAttachmentExporter attachmentImporter : attachmentImporters.getBeanList() )
			{
				fattach = attachmentImporter.importAttachment(treeInfo.getItem(), res, treeInfo.getRootFolder(),
					treeInfo.getPackageFolder());
				if( fattach != null )
				{
					break;
				}
			}
			boolean created = false;
			if( fattach == null )
			{
				// look for existing attachment
				final String actualUrl = isAbsoluteUrl(url) ? url : treeInfo.getPackageFolder() + url;
				final UnmodifiableAttachments attachments = new UnmodifiableAttachments(treeInfo.getItem());
				fattach = (Attachment) attachments.getAttachmentByFilename(actualUrl);
				if( fattach == null )
				{
					if( treeInfo.isScorm() )
					{
						CustomAttachment attachment = new CustomAttachment();
						attachment.setType("scormres");
						fattach = attachment;
					}
					else
					{
						fattach = new IMSResourceAttachment();
					}
					fattach.setUrl(actualUrl);
					fattach.setDescription(url);
					fattach.setData(HIDE_RESOURCE_KEY, true);
					created = true;
				}
			}
			if( created )
			{
				treeInfo.getCreatedAttachments().add(fattach);
			}

			ItemNavigationTab tab = new ItemNavigationTab();
			tab.setNode(node);
			tab.setAttachment(fattach);
			tab.setName("Auto Created By IMS Viewer");
			node.getTabs().add(tab);
		}

		for( IMSItem oitem : items )
		{
			if( oitem.isVisible() )
			{
				haveurl |= lineariseItems(oitem, node, resources, treeInfo);
			}
		}

		if( haveurl )
		{
			addNode(node, treeInfo);
		}

		return haveurl;
	}

	private void addNode(ItemNavigationNode node, TreeCreationInfo treeInfo)
	{
		ItemNavigationNode parent = node.getParent();
		String id = parent == null ? "" : parent.getUuid(); //$NON-NLS-1$
		Integer count = treeInfo.getKidCount().get(id);
		if( count == null )
		{
			count = 0;
		}
		node.setIndex(count);
		if( treeInfo.isScorm() || !treeInfo.isExpand() )
		{
			node.setUuid("uid" + treeInfo.getItem().getTreeNodes().size());
		}

		treeInfo.getKidCount().put(id, count + 1);
		treeInfo.getItem().getTreeNodes().add(node);

		final List<Attachment> attachments = treeInfo.getItem().getAttachments();
		List<ItemNavigationTab> tabs = node.getTabs();
		if( !Check.isEmpty(tabs) )
		{
			for( ItemNavigationTab tab : tabs )
			{
				final Attachment tabAttachment = tab.getAttachment();
				if( tabAttachment != null && !attachments.contains(tabAttachment) )
				{
					attachments.add(tabAttachment);
				}
			}
		}
	}

	public static boolean isAbsoluteUrl(String url)
	{
		try
		{
			new URL(url);
		}
		catch( MalformedURLException mal )
		{
			return false;
		}
		return true;
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		attachmentImporters = new PluginTracker<IMSAttachmentExporter>(pluginService, "com.tle.ims",
			"imsAttachmentExporter", "id");
		attachmentImporters.setBeanKey("class");
	}

	public static class TreeCreationInfo
	{
		private final Map<String, Integer> kidCount = new HashMap<String, Integer>();
		private final Item item;
		private final String packageFolder;
		private final Collection<Attachment> createdAttachments;
		private final FileHandle root;
		private final boolean scorm;
		private boolean expand;

		// private final SubTemporaryFile imsRoot;

		protected TreeCreationInfo(Item item, String packageFolder, FileHandle root, boolean scorm, boolean expand)
		{
			this.packageFolder = packageFolder + '/';
			this.item = item;
			this.createdAttachments = new ArrayList<Attachment>();
			this.root = root;
			this.scorm = scorm;
			this.expand = expand;
			// this.imsRoot = new SubTemporaryFile(staging, base);
		}

		public boolean isExpand()
		{
			return expand;
		}

		public void setExpand(boolean expand)
		{
			this.expand = expand;
		}

		public Collection<Attachment> getCreatedAttachments()
		{
			return createdAttachments;
		}

		public String getPackageFolder()
		{
			return packageFolder;
		}

		public Map<String, Integer> getKidCount()
		{
			return kidCount;
		}

		public Item getItem()
		{
			return item;
		}

		public FileHandle getRootFolder()
		{
			return root;
		}

		public boolean isScorm()
		{
			return scorm;
		}
	}
}
