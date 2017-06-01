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

package com.tle.admin.security.tree.model;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class ItemStatusLeafNode extends AbstractLeafNode<ItemStatus>
{
	private final ItemStatus status;
	private final ItemDefinition itemDefinition;
	private final String displayName;

	public ItemStatusLeafNode(ItemDefinition itemDefinition, ItemStatus status)
	{
		super(status, itemDefinition == null ? Node.GLOBAL_ITEM_STATUS : Node.ITEM_STATUS);

		this.itemDefinition = itemDefinition;
		this.status = status;

		String temp = status.toString();
		displayName = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
	}

	@Override
	public Object getTargetObject()
	{
		return new ItemStatusTarget(status, itemDefinition);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getDisplayName()
	 */
	@Override
	public String getDisplayName()
	{
		return displayName;
	}
}
