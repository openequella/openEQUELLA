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
