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
