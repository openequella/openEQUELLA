package com.tle.mypages.web;

import java.util.UUID;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.web.wizard.WizardStateInterface;

/*
 * @author aholland
 */
public class MyPagesState implements WizardStateInterface
{
	private static final long serialVersionUID = 1L;

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
		return new ItemFile(item);
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
}
