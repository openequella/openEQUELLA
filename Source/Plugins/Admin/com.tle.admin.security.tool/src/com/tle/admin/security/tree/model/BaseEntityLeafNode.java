package com.tle.admin.security.tree.model;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class BaseEntityLeafNode extends AbstractLeafNode<BaseEntityLabel>
{
	public BaseEntityLeafNode(BaseEntityLabel entity, Node privNode)
	{
		super(entity, privNode);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getDisplayName()
	 */
	@Override
	public String getDisplayName()
	{
		return BundleCache.getString(getEntity());
	}
}
