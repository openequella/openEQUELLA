package com.tle.web.wizard;

import java.io.Serializable;
import java.util.Date;

public class WizardInfo implements Serializable, Comparable<WizardInfo>
{
	private static final long serialVersionUID = 1L;

	private String uuid;
	private String collectionName;
	private Date startedDate;
	private String itemUuid;
	private int itemVersion;
	private boolean isNewItem;

	public WizardInfo()
	{
		startedDate = new Date();
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getCollectionName()
	{
		return collectionName;
	}

	public void setCollectionName(String collectionName)
	{
		this.collectionName = collectionName;
	}

	public Date getStartedDate()
	{
		return startedDate;
	}

	public void setStartedDate(Date startedDate)
	{
		this.startedDate = startedDate;
	}

	public String getItemUuid()
	{
		return itemUuid;
	}

	public void setItemUuid(String itemUuid)
	{
		this.itemUuid = itemUuid;
	}

	public int getItemVersion()
	{
		return itemVersion;
	}

	public void setItemVersion(int itemVersion)
	{
		this.itemVersion = itemVersion;
	}

	public boolean isNewItem()
	{
		return isNewItem;
	}

	public void setNewItem(boolean isNewItem)
	{
		this.isNewItem = isNewItem;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof WizardInfo )
		{
			if( ((WizardInfo) obj).uuid.equals(uuid) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return uuid.hashCode();
	}

	@Override
	public int compareTo(WizardInfo o)
	{
		return startedDate.compareTo(o.startedDate);
	}
}
