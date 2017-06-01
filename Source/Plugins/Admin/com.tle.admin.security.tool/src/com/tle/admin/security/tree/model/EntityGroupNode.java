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

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class EntityGroupNode extends AbstractLazyNode
{
	private final Node childNode;
	private final RemoteAbstractEntityService<? extends BaseEntity> service;

	public EntityGroupNode(String displayName, Node privNode, Node childNode,
		RemoteAbstractEntityService<? extends BaseEntity> service)
	{
		super(displayName, privNode);

		this.childNode = childNode;
		this.service = service;
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
		List<BaseEntityLabel> entities = service.listAllIncludingSystem();
		BundleCache.ensureCached(entities);

		List<SecurityTreeNode> results = new ArrayList<SecurityTreeNode>(entities.size());
		for( BaseEntityLabel label : entities )
		{
			results.add(createNode(label, childNode));
		}
		return results;
	}

	protected SecurityTreeNode createNode(BaseEntityLabel entity, Node nodeType)
	{
		return new BaseEntityLeafNode(entity, childNode);
	}
}
