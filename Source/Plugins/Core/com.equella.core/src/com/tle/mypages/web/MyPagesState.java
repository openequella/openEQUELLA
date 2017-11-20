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

package com.tle.mypages.web;

import java.io.ObjectStreamException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.item.service.ItemFileService;
import com.tle.web.wizard.WizardStateInterface;

/*
 * @author aholland
 */
public class MyPagesState implements WizardStateInterface
{
	private static final long serialVersionUID = 1L;

	@Inject
	private static Provider<MyPagesState> provider;
	@Inject
	private transient ItemFileService itemFileService;

	private final String wizid;

	private ItemId itemId;
	private Item item;
	private String xml;
	private transient volatile PropBagEx xmlBag;
	private String stagingId;
	private boolean newItem;
	private WorkflowStatus workflowStatus;

	public MyPagesState()
	{
		wizid = UUID.randomUUID().toString();
	}

	@Override
	public String getWizid()
	{
		return wizid;
	}

	public boolean isNewItem()
	{
		return newItem;
	}

	public void setNewItem(boolean newItem)
	{
		this.newItem = newItem;
	}

	@Override
	public ItemId getItemId()
	{
		return itemId;
	}

	public void setItemId(ItemId itemId)
	{
		this.itemId = itemId;
	}

	@Override
	public ItemPack<Item> getItemPack()
	{
		return new ItemPack<>(item, getItemxml(), stagingId);
	}

	@Override
	public void setItemPack(ItemPack<Item> itemPack)
	{
		item = itemPack.getItem();
		xmlBag = itemPack.getXml();
		xml = (xmlBag == null ? null : xmlBag.toString());
		stagingId = itemPack.getStagingID();
	}

	@Override
	public String getStagingId()
	{
		return stagingId;
	}

	@Override
	public ModifiableAttachments getAttachments()
	{
		return new ModifiableAttachments(item);
	}

	@Override
	public FileHandle getFileHandle()
	{
		String staging = getStagingId();
		if( staging != null && staging.length() > 0 )
		{
			return new StagingFile(staging);
		}
		return itemFileService.getItemFile(item);
	}

	@Override
	public Item getItem()
	{
		return item;
	}

	@Override
	public PropBagEx getItemxml()
	{
		if( xmlBag == null )
		{
			xmlBag = new PropBagEx(xml);
		}
		return xmlBag;
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		return workflowStatus;
	}

	public void setWorkflowStatus(WorkflowStatus workflowStatus)
	{
		this.workflowStatus = workflowStatus;
	}

	@Override
	public void onSessionSave()
	{
		if( xmlBag != null )
		{
			xml = xmlBag.toString();
		}
		xmlBag = null;
	}

	@Override
	public MyPagesState clone()
	{
		try
		{
			final MyPagesState other = (MyPagesState) super.clone();
			other.xmlBag = null;

			return other;
		}
		catch( CloneNotSupportedException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private Object readResolve() throws ObjectStreamException
	{
		return provider.get();
	}
}
