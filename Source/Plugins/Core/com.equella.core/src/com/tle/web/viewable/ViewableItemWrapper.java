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

package com.tle.web.viewable;

import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.web.sections.Bookmark;

public abstract class ViewableItemWrapper implements ViewableItem<Item>
{
	protected ViewableItem<Item> inner;

	public ViewableItemWrapper()
	{
		// For serialization only...
	}

	public ViewableItemWrapper(ViewableItem<Item> def)
	{
		this.inner = def;
	}

	public void setInner(ViewableItem<Item> inner)
	{
		this.inner = inner;
	}

	@Override
	public Item getItem()
	{
		return inner.getItem();
	}

	@Override
	public PropBagEx getItemxml()
	{
		return inner.getItemxml();
	}

	@Override
	public FileHandle getFileHandle()
	{
		return inner.getFileHandle();
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		return inner.getWorkflowStatus();
	}

	@Override
	public String getItemdir()
	{
		return inner.getItemdir();
	}

	@Override
	public ItemKey getItemId()
	{
		return inner.getItemId();
	}

	@Override
	public boolean isItemForReal()
	{
		return inner.isItemForReal();
	}

	@Override
	public void update(ItemPack pack, WorkflowStatus status)
	{
		inner.update(pack, status);
	}

	@Override
	public Set<String> getPrivileges()
	{
		return inner.getPrivileges();
	}

	@Override
	public boolean isDRMApplicable()
	{
		return inner.isDRMApplicable();
	}

	@Override
	public void refresh()
	{
		inner.refresh();
	}

	@Override
	public IAttachment getAttachmentByUuid(String uuid)
	{
		return inner.getAttachmentByUuid(uuid);
	}

	@Override
	public Bookmark createStableResourceUrl(String path)
	{
		return inner.createStableResourceUrl(path);
	}
}
