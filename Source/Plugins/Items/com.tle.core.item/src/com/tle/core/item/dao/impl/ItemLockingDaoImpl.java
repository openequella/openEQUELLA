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

package com.tle.core.item.dao.impl;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemLock;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.item.dao.ItemLockingDao;

/**
 * @author Nicholas Read
 */
@Bind(ItemLockingDao.class)
@Singleton
@SuppressWarnings("nls")
public class ItemLockingDaoImpl extends GenericDaoImpl<ItemLock, String> implements ItemLockingDao
{
	public ItemLockingDaoImpl()
	{
		super(ItemLock.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteForItem(Item item)
	{
		getHibernateTemplate().bulkUpdate("delete from ItemLock where item = ?", item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll()
	{
		getHibernateTemplate().bulkUpdate("delete from ItemLock where institution = ?", CurrentInstitution.get());
	}
}
