package com.tle.core.quota;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.system.QuotaSettings.UserQuota;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;

@Bind(QuotaPolicy.class)
@Singleton
public class DefaultQuotaPolicy implements QuotaPolicy
{
	@Inject
	private ItemService itemService;

	@Override
	public long getLimit(List<UserQuota> quotas)
	{
		long l = -1;
		for( UserQuota q : quotas )
		{
			l = Math.max(l, q.getSize());
		}
		if( l == -1 )
		{
			l = Long.MAX_VALUE;
		}
		return l;
	}

	@Override
	public long calculateUserFileSize(UserBean userBean)
	{
		return itemService.getUserFileSize("where i.owner = :owner", //$NON-NLS-1$
			new String[]{"owner"}, new Object[]{userBean.getUniqueID()}); //$NON-NLS-1$
	}

	public void setItemService(ItemService itemService)
	{
		this.itemService = itemService;
	}
}
