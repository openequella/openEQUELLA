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

package com.tle.core.item.standard.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.exceptions.OperationException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.AttachmentDao;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author aholland
 */
@SecureOnCall(priv = SecurityConstants.EDIT_ITEM)
@Bind
public class EditExistingItemMetadataOperation extends AbstractEditMetadataOperation
{
	@Inject
	private AttachmentDao attachmentDao;
	@Inject
	private ItemDao itemDao;

	@Override
	protected void checkExistence()
	{
		if( !params.isUpdate() )
		{
			throw new OperationException("com.tle.core.workflow.operations.editmeta.error.itemdoesnotexist", params //$NON-NLS-1$
				.getItemKey());
		}
	}

	@Override
	protected void ensureItemInternal(Item newItem)
	{
		ItemPack<Item> pack = params.getItemPack();
		pack.setStagingID(newPack.getStagingID());
		Item oldItem = pack.getItem();
		pack.setXml(newPack.getXml());

		Map<String, Attachment> attachmentMap = mergeAttachments(oldItem, newItem);
		mergeNodes(oldItem, newItem, attachmentMap);
		oldItem.setCollaborators(newItem.getCollaborators());
		oldItem.setDrmSettings(itemDao.mergeTwo(oldItem.getDrmSettings(), newItem.getDrmSettings()));
		oldItem.setNavigationSettings(newItem.getNavigationSettings());
		oldItem.setThumb(newItem.getThumb());
		attachmentDao.flush();
	}

	private void mergeNodes(Item oldItem, Item newItem, Map<String, Attachment> attachmentMap)
	{
		List<ItemNavigationNode> oldNodes = oldItem.getTreeNodes();
		oldNodes.clear();

		List<ItemNavigationNode> newNodes = newItem.getTreeNodes();
		for( ItemNavigationNode newNode : newNodes )
		{
			List<String> attachUuids = new ArrayList<String>();
			List<ItemNavigationTab> tabs = newNode.getTabs();
			if( !Check.isEmpty(tabs) )
			{
				for( ItemNavigationTab tab : tabs )
				{
					Attachment attachment = tab.getAttachment();
					attachUuids.add(attachment != null ? attachment.getUuid() : null);
					tab.setAttachment(null);
				}
			}
			if( newNode.getId() != 0 )
			{
				newNode = attachmentDao.mergeAny(newNode);
			}
			tabs = newNode.getTabs();
			if( !Check.isEmpty(tabs) )
			{
				int i = 0;
				for( ItemNavigationTab tab : tabs )
				{
					tab.setAttachment(attachmentMap.get(attachUuids.get(i++)));
				}
			}
			oldNodes.add(newNode);
		}

	}

	private Map<String, Attachment> mergeAttachments(Item oldItem, Item newItem)
	{
		Map<String, Attachment> attachMap = new HashMap<String, Attachment>();
		List<Attachment> oldAttachments = oldItem.getAttachments();
		List<Attachment> newAttachments = newItem.getAttachments();
		boolean addToOld = false;
		if( !oldAttachments.equals(newAttachments) )
		{
			oldAttachments.clear();
			addToOld = true;
		}
		for( Attachment newAttach : newAttachments )
		{
			if( newAttach.getId() != 0 )
			{
				newAttach = attachmentDao.merge(newAttach);
			}
			if( addToOld )
			{
				oldAttachments.add(newAttach);
			}
			attachMap.put(newAttach.getUuid(), newAttach);
		}
		return attachMap;
	}

}
