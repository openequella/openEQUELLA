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

package com.tle.web.viewitem.treeviewer;

import static com.tle.common.collection.AttachmentConfigConstants.METADATA_TARGET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.attachments.ItemNavigationService;
import com.tle.web.viewurl.attachments.ItemNavigationService.NodeAddedCallback;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class NewTreeNavigationSection extends TreeNavigationSection
{
	@Inject
	private ItemNavigationService itemNavService;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	private PluginTracker<ModifyNavigationExtension> modifyNavigation;

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		String uuid = resource.getFilenameWithoutPath();
		getModel(info).setAttachmentControlId(uuid);
		return super.view(info, resource);
	}

	@SuppressWarnings("nls")
	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		if( "download".equals(event.getParameter("viewMethod", false)) )
		{
			getModel(info).setDownload(true);
		}
	}

	private List<Attachment> getDisplayAttachmentList(SectionInfo info, Item item, ViewableItem<Item> vitem)
	{
		String attachmentSectionUuid = getModel(info).getAttachmentControlId();
		List<Attachment> attachments = item.getAttachments();

		if( !Check.isValidUuid(attachmentSectionUuid) )
		{
			return attachments;
		}

		List<String> metadataTargets = Collections.emptyList();
		ItemDefinition collection = item.getItemDefinition();

		for( SummarySectionsConfig displaySection : collection.getItemSummaryDisplayTemplate().getConfigList() )
		{
			String uuid = displaySection.getUuid();
			if( attachmentSectionUuid.equals(uuid) )
			{
				String configuration = displaySection.getConfiguration();
				if( !Check.isEmpty(configuration) )
				{
					PropBagEx xml = new PropBagEx(configuration);
					metadataTargets = xml.getNodeList(METADATA_TARGET);
					break;
				}
			}
		}

		List<Attachment> displayAttachmentList = new ArrayList<Attachment>();

		if( !metadataTargets.isEmpty() )
		{
			PropBagEx itemxml = vitem.getItemxml();

			for( Attachment attachment : attachments )
			{
				String attachmentUuid = attachment.getUuid();
				for( String target : metadataTargets )
				{
					for( String val : itemxml.getNodeList(target) )
					{
						if( val.equals(attachmentUuid) )
						{
							displayAttachmentList.add(attachment);
						}
					}
				}
			}
		}
		else
		{
			return attachments;
		}

		return displayAttachmentList;
	}

	@Override
	protected List<ItemNavigationNode> getTreeNodes(SectionInfo info)
	{
		final ViewableItem<Item> vitem = getModel(info).getResource().getViewableItem();
		final Item item = vitem.getItem();
		final List<ItemNavigationNode> itemNodes = item.getTreeNodes();
		// clone it so that any changes don't accidentally get persisted...
		final List<ItemNavigationNode> treeNodes = new ArrayList<ItemNavigationNode>();
		treeNodes.addAll(itemNodes);

		List<Attachment> displayAttachmentList = getDisplayAttachmentList(info, item, vitem);
		final List<Attachment> nodedAttachments = new ArrayList<Attachment>();
		for( ItemNavigationNode node : treeNodes )
		{
			for( ItemNavigationTab tab : node.getTabs() )
			{
				Attachment attachment = tab.getAttachment();
				if( attachment != null && displayAttachmentList.contains(attachment) )
				{
					nodedAttachments.add(attachment);
				}
				else
				{
					// don't show the attachment if it is not in this attachment
					// section
					tab.setAttachment(null);
				}
			}
		}

		List<ModifyNavigationExtension> list = modifyNavigation.getBeanList();
		for( ModifyNavigationExtension ext : list )
		{
			ext.process(treeNodes, nodedAttachments);
		}

		// TODO: this is more or less duplicated from attachments section...
		// attachments with no tree nodes (only if not manually modified
		// navigation)
		if( !item.getNavigationSettings().isManualNavigation() )
		{
			final List<Attachment> attachmentsToMakeNodesFor = new ArrayList<Attachment>();
			for( Attachment attachment : displayAttachmentList )
			{
				if( !nodedAttachments.contains(attachment) )
				{
					final ViewableResource viewableResource = attachmentResourceService.getViewableResource(info,
						vitem, attachment);
					if( !viewableResource.getBooleanAttribute(ViewableResource.KEY_HIDDEN) )
					{
						attachmentsToMakeNodesFor.add(attachment);
					}
				}
			}
			itemNavService.populateTreeNavigationFromAttachments(item, treeNodes, attachmentsToMakeNodesFor,
				new NodeAddedCallback()
				{
					@Override
					public void execute(int index, ItemNavigationNode node)
					{
						// we have to fake up a reproducable UUID as this node
						// is never
						// persisted to the database
						node.setUuid("uid" + index); //$NON-NLS-1$
					}
				});
		}

		return treeNodes;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "newtree";
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	protected void registerPathMappings(RootItemFileSection rootSection)
	{
		rootSection.addViewerMapping(Type.FULL, this, "viewcontent");
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		modifyNavigation = new PluginTracker<ModifyNavigationExtension>(pluginService,
			pluginService.getPluginIdForObject(getClass()), "modifyNavigation", null, //$NON-NLS-1$
			new PluginTracker.ExtensionParamComparator("order")); //$NON-NLS-1$
		modifyNavigation.setBeanKey("class"); //$NON-NLS-1$
	}

}
