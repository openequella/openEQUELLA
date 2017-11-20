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

package com.tle.web.cloud.view;

import java.net.URI;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.PathUtils;
import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.sections.Bookmark;
import com.tle.web.viewable.ViewableItem;

/**
 * @author Aaron
 */
public class CloudViewableItem implements ViewableItem<CloudItem>
{
	private final CloudItem item;
	private final boolean integration;
	private boolean fromRequest;

	public CloudViewableItem(CloudItem item)
	{
		this(item, false);
	}

	public CloudViewableItem(CloudItem item, boolean integration)
	{
		this.item = item;
		this.integration = integration;
	}

	@Override
	public CloudAttachment getAttachmentByUuid(String uuid)
	{
		return (CloudAttachment) new UnmodifiableAttachments(item).getAttachmentByUuid(uuid);
	}

	@Override
	public CloudItem getItem()
	{
		return item;
	}

	@Override
	public PropBagEx getItemxml()
	{
		return new PropBagEx(item.getMetadata());
	}

	@Override
	public FileHandle getFileHandle()
	{
		// Cannot have a local file handle
		return null;
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		// Cannot be in workflow
		return null;
	}

	@Override
	public String getItemdir()
	{
		return PathUtils.filePath("cloud", getItemId().toString());
	}

	@Override
	public URI getServletPath()
	{
		// Only used by standard Item ViewDefaultSection. Might be
		// best to get rid of this method on the ViewableItem interface?
		return null;
	}

	@Override
	public ItemKey getItemId()
	{
		return new ItemId(item.getUuid(), item.getVersion());
	}

	@Override
	public boolean isItemForReal()
	{
		// There are no draft/preview cloud items
		return true;
	}

	@Override
	public void update(ItemPack<CloudItem> pack, WorkflowStatus status)
	{
		// Cloud items are read-only
	}

	@Override
	public void refresh()
	{
		// Cloud items are read-only
	}

	@Override
	public Set<String> getPrivileges()
	{
		// Cloud items are guest viewable
		return null;
	}

	@Override
	public boolean isDRMApplicable()
	{
		// Maybe in future, but not for the MVP
		return false;
	}

	@Override
	public Bookmark createStableResourceUrl(String path)
	{
		// Cloud items have no real viewing location beyond the cloud servlet
		// Not sure if this is going to get called...
		return null;
	}

	@Override
	public Attachment getAttachmentByFilepath(String filepath)
	{
		// This is only used by the standard Item AttachmentsSection. Might be
		// best to get rid of this method on the ViewableItem interface?
		return null;
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
		return CloudConstants.ITEM_EXTENSION;
	}

	public boolean isIntegration()
	{
		return integration;
	}
}
