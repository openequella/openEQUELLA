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

package com.tle.core.filesystem.staging.dao.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Staging;
import com.tle.core.filesystem.staging.dao.StagingDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;

@NonNullByDefault
@Bind(StagingDao.class)
@Singleton
public class StagingDaoImpl extends GenericDaoImpl<Staging, String> implements StagingDao
{
	public StagingDaoImpl()
	{
		super(Staging.class);
	}

	@Override
	public void deleteAllForUserSession(String userSession)
	{
		Criterion[] cs = {Restrictions.eq("userSession", userSession)};
		for( Staging s : findAllByCriteria(cs) )
		{
			getHibernateTemplate().delete(s);
		}
	}
}
