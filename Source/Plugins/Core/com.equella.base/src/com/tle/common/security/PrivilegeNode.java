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

package com.tle.common.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
class PrivilegeNode
{
	private final List<PrivilegeNode> children = new ArrayList<PrivilegeNode>();
	private final Set<String> privileges = new HashSet<String>();

	private final Node nodeType;

	public PrivilegeNode(Node nodeType)
	{
		this.nodeType = nodeType;
	}

	public List<PrivilegeNode> getChildren()
	{
		return children;
	}

	public Set<String> getPrivileges()
	{
		return privileges;
	}

	public Node getNodeType()
	{
		return nodeType;
	}

	public void registerPrivilege(String privilege)
	{
		boolean added = privileges.add(privilege);
		if( !added )
		{
			throw new RuntimeException("Privilege already registered");
		}
	}
}
