/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
