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

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;

/**
 * @author Nicholas Read
 */
public class ItemMetadataRuleParentNode extends AbstractLazyNode
{
	private final ItemDefinition itemDefinition;
	private final RemoteItemDefinitionService service;

	public ItemMetadataRuleParentNode(RemoteItemDefinitionService service, ItemDefinition itemDefinition)
	{
		super(CurrentLocale.get("com.tle.admin.security.tree.model.itemmetadataruleparentnode.name"), null); //$NON-NLS-1$

		this.service = service;
		this.itemDefinition = itemDefinition;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getTargetObject()
	 */
	@Override
	public Object getTargetObject()
	{
		return null;
	}

	@Override
	protected List<SecurityTreeNode> getChildren()
	{
		List<SecurityTreeNode> results = new ArrayList<SecurityTreeNode>();

		ItemDefinition fullItemDef = service.get(itemDefinition.getId());
		if( fullItemDef.getItemMetadataRules() != null )
		{
			for( ItemMetadataRule rule : fullItemDef.getItemMetadataRules() )
			{
				results.add(new ItemMetadataRuleLeafNode(itemDefinition, rule));
			}
		}

		return results;
	}
}
