package com.tle.admin.security.tree.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ItemSearchNode extends AbstractLazyNode
{
	public ItemSearchNode()
	{
		super(CurrentLocale.get("com.tle.admin.security.tree.model.itemsearchnode.name"), null); //$NON-NLS-1$
	}

	@Override
	public Object getTargetObject()
	{
		return null;
	}

	@Override
	protected List<SecurityTreeNode> getChildren()
	{
		List<SecurityTreeNode> results = new ArrayList<SecurityTreeNode>();
		for( ItemStatus status : ItemStatus.values() )
		{
			results.add(new ItemStatusLeafNode(null, status));
		}
		return results;
	}
}
