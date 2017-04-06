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
