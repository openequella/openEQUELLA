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
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class ItemMetadataRuleLeafNode extends AbstractLeafNode<ItemMetadataRule>
{
	private final ItemMetadataRule rule;
	private final ItemDefinition itemDefinition;

	public ItemMetadataRuleLeafNode(ItemDefinition itemDefinition, ItemMetadataRule rule)
	{
		super(rule, Node.ITEM_METADATA);

		this.itemDefinition = itemDefinition;
		this.rule = rule;
	}

	@Override
	public Object getTargetObject()
	{
		return new ItemMetadataTarget(rule.getId(), itemDefinition);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getDisplayName()
	 */
	@Override
	public String getDisplayName()
	{
		return rule.getName();
	}
}
