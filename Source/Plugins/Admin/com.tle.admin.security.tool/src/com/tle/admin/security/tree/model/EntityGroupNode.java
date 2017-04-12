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
