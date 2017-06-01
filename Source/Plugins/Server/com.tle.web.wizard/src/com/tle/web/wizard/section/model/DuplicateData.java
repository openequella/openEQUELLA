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

package com.tle.web.wizard.section.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;

/**
 * @author Nicholas Read
 */
public class DuplicateData implements Serializable
{
	private static final long serialVersionUID = 1;

	private final String identifier;
	private final String value;
	private final List<ItemId> items;
	private boolean canAccept;

	private boolean visible;
	private boolean accepted;

	public DuplicateData(String identifier, String value, List<? extends ItemKey> items, boolean canAccept)
	{
		this.identifier = identifier;
		this.value = value;
		this.items = new ArrayList<ItemId>();
		for( ItemKey itemKey : items )
		{
			this.items.add(new ItemId(itemKey.getUuid(), itemKey.getVersion()));
		}
		this.canAccept = canAccept;

		visible = true;
	}

	public List<ItemId> getItems()
	{
		return items;
	}

	public String getValue()
	{
		return value;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public boolean isAccepted()
	{
		return accepted;
	}

	public void setAccepted(boolean accepted)
	{
		this.accepted = accepted;
	}

	public boolean isCanAccept()
	{
		return canAccept;
	}

	public void setCanAccept(boolean canAccept)
	{
		this.canAccept = canAccept;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
}
