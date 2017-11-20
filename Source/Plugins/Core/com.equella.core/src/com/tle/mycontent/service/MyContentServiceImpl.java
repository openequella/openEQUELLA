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

package com.tle.mycontent.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;

import com.dytech.devlib.PropBagEx;
import com.google.inject.Provider;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.web.section.ContributeMyContentAction;
import com.tle.mycontent.web.section.MyContentContributeSection;
import com.tle.mycontent.workflow.operations.OperationFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.result.util.KeyLabel;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(MyContentService.class)
@Singleton
public class MyContentServiceImpl implements MyContentService
{
	private PluginTracker<ContentHandler> handlerTracker;

	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private ItemService itemService;
	@Inject
	private Provider<ContributeMyContentAction> contributeProvider;
	@Inject
	private OperationFactory editOpFactory;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	public boolean isMyContentContributionAllowed()
	{
		return !Check.isEmpty(
			itemDefinitionService.getMatchingCreatableUuid(Collections.singleton(MyContentConstants.MY_CONTENT_UUID)));
	}

	@Override
	public ItemDefinition getMyContentItemDef()
	{
		ItemDefinition itemdef = itemDefinitionService.getByUuid(MyContentConstants.MY_CONTENT_UUID);
		if( itemdef == null )
		{
			throw new RuntimeException(
				"My Content collection is missing or does not have a UUID of " + MyContentConstants.MY_CONTENT_UUID
					+ ".  This collection and UUID are mandatory for the Scrapbook to function correctly.");
		}
		return itemdef;
	}

	@Override
	public void forwardToEditor(SectionInfo info, ItemId itemId)
	{
		PropBagEx itemxml = itemService.getItemXmlPropBag(itemId);
		String handlerId = itemxml.getNode(MyContentConstants.CONTENT_TYPE_NODE);
		MyContentContributeSection.forwardToEdit(info, handlerId, itemId);
	}

	@Override
	public boolean returnFromContribute(SectionInfo info)
	{
		MyContentContributeSection myContribute = info.lookupSection(MyContentContributeSection.class);
		if( myContribute != null )
		{
			myContribute.contributionFinished(info);
		}

		// FIXME: is this true??? possibly not
		SectionInfo fwd = info.createForward("/access/myresources.do");
		info.forwardAsBookmark(fwd);
		return true;
	}

	@Override
	public Set<String> getContentHandlerIds()
	{
		return handlerTracker.getExtensionMap().keySet();
	}

	@Override
	public String getContentHandlerNameKey(String handlerId)
	{
		Extension extension = handlerTracker.getExtension(handlerId);
		return extension.getParameter("nameKey").valueAsString();
	}

	@Override
	public ContentHandler getHandlerForId(String handlerId)
	{
		return handlerTracker.getBeanMap().get(handlerId);
	}

	@Override
	public WorkflowOperation getEditOperation(MyContentFields fields, String filename, InputStream inputStream,
		String stagingUuid, boolean removeExistingAttachments, boolean useExistingAttachments)
	{
		return editOpFactory.create(fields, filename, inputStream, stagingUuid, removeExistingAttachments,
			useExistingAttachments);
	}

	@Override
	public MyContentFields getFieldsForItem(ItemId itemId)
	{
		MyContentFields fields = new MyContentFields();
		PropBagEx itemXml = itemService.getItemXmlPropBag(itemId);
		fields.setTitle(itemXml.getNode(MyContentConstants.NAME_NODE));
		fields.setTags(itemXml.getNode(MyContentConstants.KEYWORDS_NODE));
		fields.setResourceId(itemXml.getNode(MyContentConstants.CONTENT_TYPE_NODE));
		return fields;
	}

	@Override
	public void delete(ItemId itemId)
	{
		// always unlock locked MyContent (you are the only one that can lock it
		// after all...)
		itemService.forceUnlock(itemService.get(itemId));
		itemService.operation(itemId, workflowFactory.delete(), workflowFactory.save());
	}

	@Override
	public void restore(ItemId itemId)
	{
		// always unlock locked MyContent (you are the only one that can lock it
		// after all...)
		itemService.forceUnlock(itemService.get(itemId));
		itemService.operation(itemId, workflowFactory.restore(), workflowFactory.save());
	}

	@Override
	public boolean isMyContentItem(Item item)
	{
		return item.getItemDefinition().getUuid().equals(MyContentConstants.MY_CONTENT_UUID);
	}

	@Override
	public ContributeMyContentAction createActionForHandler(String handlerId)
	{
		ContributeMyContentAction action = contributeProvider.get();
		action.setHandlerId(handlerId);
		action.setButtonLabel(new KeyLabel(getContentHandlerNameKey(handlerId)));
		return action;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		handlerTracker = new PluginTracker<ContentHandler>(pluginService, "com.tle.mycontent", "contentHandler", "id",
			new PluginTracker.ExtensionParamComparator("order"));
		handlerTracker.setBeanKey("contributeBean");
	}

	@Override
	public void forwardToContribute(SectionInfo info, String handlerId)
	{
		SectionInfo forward = MyContentContributeSection.createForForward(info);
		MyContentContributeSection contributeSection = forward.lookupSection(MyContentContributeSection.class);
		contributeSection.contribute(forward, handlerId);
		info.forwardAsBookmark(forward);
	}
}
