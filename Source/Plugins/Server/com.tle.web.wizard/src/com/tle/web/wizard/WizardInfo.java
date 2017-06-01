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
