package com.tle.admin.security.tree.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ItemStatusParentNode extends AbstractLazyNode
{
	private final ItemDefinition itemDefinition;

	public ItemStatusParentNode(ItemDefinition itemDefinition)
	{
		super(CurrentLocale.get("com.tle.admin.security.tree.model.itemstatusparentnode.name"), //$NON-NLS-1$
			null);
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
		for( ItemStatus status : ItemStatus.values() )
		{
			results.add(new ItemStatusLeafNode(itemDefinition, status));
		}
		return results;
	}
}
