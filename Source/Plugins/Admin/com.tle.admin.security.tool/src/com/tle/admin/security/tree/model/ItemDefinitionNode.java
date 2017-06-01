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

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class ItemDefinitionNode extends AbstractLazyNode
{
	private final RemoteItemDefinitionService service;
	private final BaseEntityLabel label;

	public ItemDefinitionNode(BaseEntityLabel label, RemoteItemDefinitionService service)
	{
		super(null, Node.COLLECTION);
		this.label = label;

		this.service = service;
	}

	@Override
	public String getDisplayName()
	{
		return BundleCache.getString(label);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getTargetObject()
	 */
	@Override
	public Object getTargetObject()
	{
		return label;
	}

	@Override
	protected List<SecurityTreeNode> getChildren()
	{
		ItemDefinition itemDefinition = new ItemDefinition(label.getId());

		List<SecurityTreeNode> results = new ArrayList<SecurityTreeNode>();
		results.add(new ItemStatusParentNode(itemDefinition));
		results.add(new ItemMetadataRuleParentNode(service, itemDefinition));
		return results;
	}
}
