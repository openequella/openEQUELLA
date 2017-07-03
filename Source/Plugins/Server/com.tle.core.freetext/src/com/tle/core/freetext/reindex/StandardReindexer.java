package com.tle.core.freetext.reindex;

import javax.inject.Singleton;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class StandardReindexer implements ReindexHandler
{

	@Override
	public ReindexFilter getReindexFilter(Node node, Object domainObject)
	{
		switch( node )
		{
			case ITEM:
				return new ItemFilter((Item) domainObject);
			case GLOBAL_ITEM_STATUS:
			case ITEM_STATUS:
				ItemStatusTarget ist = (ItemStatusTarget) domainObject;
				if( ist.getItemDefinition() == null )
				{
					return new GlobalItemStatusFilter(ist.getItemStatus());
				}
				else
				{
					return new ItemStatusFilter(ist.getItemStatus(), ist.getItemDefinition());
				}
			case ITEM_METADATA:
				return new ItemMetadataFilter(((ItemMetadataTarget) domainObject).getId());

			case COLLECTION:
				if( domainObject instanceof ItemDefinition )
				{
					return new ItemdefFilter((ItemDefinition) domainObject);
				}
				else if( domainObject instanceof BaseEntityLabel )
				{
					BaseEntityLabel bel = (BaseEntityLabel) domainObject;
					return new ItemdefFilter(new ItemDefinition(bel.getId()));
				}
				return null;
			case ALL_COLLECTIONS:
				return new InstitutionFilter();
		}
		return null;
	}
}
