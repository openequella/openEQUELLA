package com.tle.core.services.item.impl;

import static com.tle.common.security.SecurityConstants.TARGET_ITEM_METADATA;
import static com.tle.common.security.SecurityConstants.TARGET_ITEM_STATUS;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;
import com.tle.core.services.entity.impl.BaseEntitySecurityTargetHandler;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ItemStatusAndMetadataSecurityTargetHandler implements SecurityTargetHandler
{
	@Inject
	private BaseEntitySecurityTargetHandler baseEntityHandler;

	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		ItemDefinition itemDefinition = null;
		if( target instanceof ItemStatusTarget )
		{
			ItemStatusTarget itemStatus = (ItemStatusTarget) target;
			String name = itemStatus.getItemStatus().name();
			itemDefinition = itemStatus.getItemDefinition();
			labels.add(TARGET_ITEM_STATUS + ":" + name);
			labels.add(TARGET_ITEM_STATUS + ":" + itemDefinition.getId() + ":" + name);
		}
		else
		{
			ItemMetadataTarget imt = (ItemMetadataTarget) target;
			itemDefinition = imt.getItemDefinition();
			labels.add(getItemMetadataLabel(imt));
		}

		baseEntityHandler.gatherAllLabels(labels, itemDefinition);
	}

	private String getItemMetadataLabel(ItemMetadataTarget imt)
	{
		return TARGET_ITEM_METADATA + ":" + imt.getItemDefinition().getId() + ":" + imt.getId();
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		if( target instanceof ItemStatusTarget )
		{
			final ItemStatusTarget ist = (ItemStatusTarget) target;

			final StringBuilder rv = new StringBuilder(TARGET_ITEM_STATUS);
			rv.append(':');
			if( ist.getItemDefinition() != null )
			{
				rv.append(ist.getItemDefinition().getId());
				rv.append(':');
			}
			rv.append(ist.getItemStatus().name());
			return rv.toString();
		}

		return getItemMetadataLabel((ItemMetadataTarget) target);
	}

	@Override
	public Object transform(Object target)
	{
		return ((ItemMetadataTarget) target).getItemDefinition();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		throw new UnsupportedOperationException();
	}
}
