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

package com.tle.core.lti.consumers.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Session;

import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.dao.LtiConsumerDao;

@Bind(LtiConsumerDao.class)
@Singleton
public class LtiConsumerDaoImpl extends AbstractEntityDaoImpl<LtiConsumer> implements LtiConsumerDao
{
	public LtiConsumerDaoImpl()
	{
		super(LtiConsumer.class);
	}

	@Override
	public LtiConsumer findByConsumerKey(String consumerKey)
	{
		return (LtiConsumer) getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Object doInHibernate(Session session)
			{
				List<LtiConsumer> consumers = session
					.createQuery("FROM LtiConsumer WHERE consumerKey = :key AND institution = :institution")
					.setParameter("key", consumerKey).setParameter("institution", CurrentInstitution.get()).list();
				return Check.isEmpty(consumers) ? null : consumers.get(0);
			}
		});
	}
}
