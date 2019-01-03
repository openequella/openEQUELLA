/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.viewable;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.web.sections.Bookmark;
import com.tle.web.viewurl.FilestoreBookmark;

@Bind
public class NewDefaultViewableItem implements ViewableItem<Item>
{
	@Inject
	private ItemService itemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private Provider<StatusOperation> statusOpFactpry;

	private boolean fromRequest;
	private Item item;
	private PropBagEx itemxml;
	private WorkflowStatus status;
	private Map<String, Attachment> attachmentMap;
	private NewViewableItemState state = new NewViewableItemState();

	@Override
	public Item getItem()
	{
		getWorkflowStatus();
		return item;
	}

	@Override
	public Attachment getAttachmentByFilepath(String filepath)
	{
		if( item == null )
		{
			try
			{
				return itemService.getAttachmentForFilepath(state.getItemId(), filepath);
			}
			catch( AttachmentNotFoundException anfe )
			{
				anfe.setFromRequest(fromRequest);
				throw anfe;
			}
		}
		Map<String, Attachment> map = getAttachmentMap();
		for( Attachment attachment : map.values() )
		{
			if( attachment.getUrl().equals(filepath) )
			{
				return attachment;
			}
		}
		return null;
	}

	@Override
	public Attachment getAttachmentByUuid(String uuid)
	{
		if( item == null )
		{
			try
			{
				return itemService.getAttachmentForUuid(state.getItemId(), uuid);
			}
			catch( AttachmentNotFoundException anfe )
			{
				anfe.setFromRequest(fromRequest);
				throw anfe;
			}
		}
		Map<String, Attachment> tempAttachmentMap = getAttachmentMap();
		return tempAttachmentMap.get(uuid);
	}

	private Map<String, Attachment> getAttachmentMap()
	{
		if( attachmentMap == null )
		{
			attachmentMap = UnmodifiableAttachments.convertToMapUuid(getItem().getAttachmentsUnmodifiable());
		}
		return attachmentMap;
	}

	@Override
	public PropBagEx getItemxml()
	{
		getWorkflowStatus();
		return itemxml;
	}

	@Override
	public FileHandle getFileHandle()
	{
		ItemKey itemKey = getItemId();
		return itemFileService.getItemFile(getItem());
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		if( status == null )
		{
			try
			{
				StatusOperation statusop = statusOpFactpry.get();
				ItemKey itemId = getItemId();
				ItemPack<Item> pack = itemService.operation(itemId, statusop);
				if( pack == null )
				{
					throw new ItemNotFoundException(itemId, fromRequest);
				}
				item = pack.getItem();
				itemxml = pack.getXml();
				status = statusop.getStatus();
			}
			catch( LockedException e )
			{
				throw new RuntimeException(e);
			}
			catch( WorkflowException e )
			{
				throw new RuntimeException(e);
			}
		}
		return status;
	}

	@Override
	public ItemKey getItemId()
	{
		return state.getItemId();
	}

	@Override
	public String getItemdir()
	{
		return state.getItemdir(institutionService);
	}

	@Override
	public URI getServletPath()
	{
		return state.getServletPath();
	}

	@Override
	public boolean isItemForReal()
	{
		return state.isItemForReal();
	}

	@Override
	public void update(ItemPack<Item> pack, WorkflowStatus status)
	{
		if( pack != null )
		{
			this.item = pack.getItem();
			this.itemxml = pack.getXml();
			this.status = status;
		}
	}

	@Override
	public Set<String> getPrivileges()
	{
		if( status == null )
		{
			Set<String> cachedPriviliges = itemService.getCachedPrivileges(getItemId());
			if( cachedPriviliges != null )
			{
				return cachedPriviliges;
			}
		}
		return getWorkflowStatus().getSecurityStatus().getAllowedPrivileges();
	}

	@Override
	public boolean isDRMApplicable()
	{
		return state.isRequireDRM();
	}

	@Override
	public void refresh()
	{
		status = null;
	}

	public void setState(NewViewableItemState state)
	{
		this.state = state;
	}

	public NewViewableItemState getState()
	{
		return state;
	}

	public String getIntegrationType()
	{
		return state.getIntegrationType();
	}

	@Override
	public Bookmark createStableResourceUrl(final String path)
	{
		return new FilestoreBookmark(institutionService, getItemId(), path);
	}

	@Override
	public boolean isFromRequest()
	{
		return fromRequest;
	}

	@Override
	public void setFromRequest(boolean fromRequest)
	{
		this.fromRequest = fromRequest;
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
