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

package com.tle.core.quota.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.quota.dao.QuotaDao;

/**
 * @author Aaron
 *
 */
@Bind(QuotaDao.class)
@Singleton
public class QuotaDaoImpl extends GenericInstitionalDaoImpl<Item, Long> implements QuotaDao
{
	public QuotaDaoImpl()
	{
		super(Item.class);
	}

	@Override
	public long calculateUserFileSize(UserBean user)
	{
		String hql = "select sum(i.totalFileSize) from Item i where i.owner = :owner";
		List<Long> sum = getHibernateTemplate().findByNamedParam(hql, "owner", user.getUniqueID());
		if( sum.size() > 0 )
		{
			Long l = sum.get(0);
			if( l != null )
			{
				return l;
			}
		}
		return 0;
	}
}
